package fr.flolec.alpacabot.alpacaapi.bar;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.ReplaceOptions;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;

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

    public Date findOldestBeginTimeAmongLatestBySymbol() {
        Aggregation aggregation = Aggregation.newAggregation(
                // Group by symbol and search for most recent begin_time for each symbol
                group("symbol").max("begin_time").as("latestBeginTime"),
                // Search for oldest begin_time among these results
                group().min("latestBeginTime").as("oldestBeginTime")
        );
        return mongoTemplate.aggregate(aggregation, BarModel.class, Date.class).getUniqueMappedResult();
    }

}
