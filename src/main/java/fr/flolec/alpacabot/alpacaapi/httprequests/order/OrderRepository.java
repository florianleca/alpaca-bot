package fr.flolec.alpacabot.alpacaapi.httprequests.order;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends MongoRepository<OrderModel, String> {

    @Query(value = "{ 'asset_id' : ?0, 'side' : 'buy', dual_order_id : null }")
    List<OrderModel> findUnsoldOrders(String assetId);

    @Query(value = "{ 'filled_at' :  null }")
    List<OrderModel> findUnfilledOrders();
}
