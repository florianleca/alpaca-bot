package fr.flolec.alpacabot.alpacaapi.bar;

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
        Query query = Query.query(Criteria.where("begin_time").is(barModel.getBeginTime()).and("symbol").is(barModel.getSymbol()));
        mongoTemplate.replace(query, barModel, ReplaceOptions.replaceOptions().upsert());
    }

    public void deleteAll() {
        mongoTemplate.dropCollection(BarModel.class);
    }

}
