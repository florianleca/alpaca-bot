package fr.flolec.alpacabot.strategies.strategy1;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Strategy1TicketRepository extends MongoRepository<Strategy1TicketModel, String> {
}
