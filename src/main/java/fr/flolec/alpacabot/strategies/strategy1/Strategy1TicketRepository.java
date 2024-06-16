package fr.flolec.alpacabot.strategies.strategy1;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface Strategy1TicketRepository extends MongoRepository<Strategy1TicketModel, String> {

    @Query("{ $or: [ { 'buy_order_id' : ?0 }, { 'sell_order_id' : ?0 } ] }")
    Strategy1TicketModel findByOrder(String orderId);

    @Query("{ 'status' :  {$ne: 'COMPLETE'} }")
    List<Strategy1TicketModel> findUncompletedTickets();

    @Query("{ 'status' :  {$ne: 'COMPLETE'}, 'symbol' :  ?0 }")
    List<Strategy1TicketModel> findUncompletedTickets(String symbol);
}
