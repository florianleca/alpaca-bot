package fr.flolec.alpacabot.alpacaapi.bar;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

@Repository
public class BarModelRepository {

    private final MongoOperations mongoTemplate;

    public BarModelRepository(MongoOperations mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void insertOrReplace(BarModel barModel) {
        // Cherche un document avec le même symbole et la même date de début
        Query upsertQuery = Query.query(Criteria.where("symbol").is(barModel.getSymbol()).and("begin_time").is(barModel.getBeginTime()));

        // Remplace le document s'il existe, sinon insère un nouveau document
        mongoTemplate.replace(upsertQuery, barModel, ReplaceOptions.replaceOptions().upsert());
    }

    public void cleanExcessBars(String symbol, int maxBarsPerSymbol) {
        // Sélectionne les documents excédentaires
        Query cleanQuery = Query.query((Criteria.where("symbol").is(symbol)))
                .with(Sort.by(Sort.Direction.DESC, "begin_time"))
                .skip(maxBarsPerSymbol);

        // Supprimer les documents excédentaires en un seul batch
        mongoTemplate.remove(cleanQuery, BarModel.class);
    }

    public void deleteAll() {
        mongoTemplate.dropCollection(BarModel.class);
    }

}
