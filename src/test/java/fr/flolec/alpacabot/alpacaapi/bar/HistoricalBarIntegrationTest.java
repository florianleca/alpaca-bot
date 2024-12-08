package fr.flolec.alpacabot.alpacaapi.bar;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class HistoricalBarIntegrationTest {

    @Autowired
    private BarModelRepository barModelRepository;

    @Autowired
    private MongoOperations mongoTemplate;

    private final BarModel barModel1 = new BarModel("AAL", "2021-01-01T00:00:00Z", 1.0, 2.0, 0.5, 1.5, 1000.0);
    private final BarModel barModel2 = new BarModel("AAL", "2021-01-01T01:00:00Z", 2.0, 3.0, 1.5, 2.5, 2000.0);
    private final BarModel barModel3 = new BarModel("DAL", "2021-01-01T00:00:00Z", 3.0, 4.0, 2.5, 3.5, 3000.0);
    private final BarModel barModel4 = new BarModel("AAL", "2021-01-01T00:00:00Z", 2.2, 3.0, 1.5, 2.5, 2000.0);
    private final BarModel barModel5 = new BarModel("AAL", "2021-01-01T02:00:00Z", 2.0, 3.0, 1.5, 2.5, 2000.0);
    private final BarModel barModel6 = new BarModel("AAL", "2021-04-04T00:00:00Z", 2.0, 3.0, 1.5, 2.5, 2000.0);
    private final BarModel barModel7 = new BarModel("AAL", "2022-01-01T00:00:00Z", 2.0, 3.0, 1.5, 2.5, 2000.0);


    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.0.10");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @BeforeEach
    void setUp() {
        mongoTemplate.dropCollection(BarModel.class);
    }

    @Test
    void insertOrReplace_noDuplicate_barInserted() {
        barModelRepository.insertOrReplace(barModel1);
        barModelRepository.insertOrReplace(barModel2);
        barModelRepository.insertOrReplace(barModel3);

        List<BarModel> bars = mongoTemplate.findAll(BarModel.class);
        assertEquals(3, bars.size());
        bars.forEach(bar -> assertNotNull(bar.getId()));
    }

    @Test
    void insertOrReplace_duplicate_barReplaced() {
        barModelRepository.insertOrReplace(barModel1);
        barModelRepository.insertOrReplace(barModel4);

        List<BarModel> bars = mongoTemplate.findAll(BarModel.class);
        assertEquals(1, bars.size());
        assertEquals(2.2, bars.get(0).getClose());
    }

    @Test
    void deleteAll_nominal_collectionEmptied() {
        barModelRepository.insertOrReplace(barModel1);
        barModelRepository.insertOrReplace(barModel2);
        barModelRepository.insertOrReplace(barModel3);

        barModelRepository.deleteAll();

        List<BarModel> bars = mongoTemplate.findAll(BarModel.class);
        assertTrue(bars.isEmpty());
    }

    @Test
    void cleanExcessBars_sameSymbolOnly_excessBarsDeleted() {
        barModelRepository.insertOrReplace(barModel1);
        barModelRepository.insertOrReplace(barModel2);
        barModelRepository.insertOrReplace(barModel5);
        barModelRepository.insertOrReplace(barModel6);
        barModelRepository.insertOrReplace(barModel7);

        barModelRepository.cleanExcessBars("AAL", 3);

        List<BarModel> bars = mongoTemplate.findAll(BarModel.class);
        assertEquals(3, bars.size());
        assertEquals(Instant.parse("2021-01-01T02:00:00Z"), bars.get(0).getBeginTime());
        assertEquals(Instant.parse("2021-04-04T00:00:00Z"), bars.get(1).getBeginTime());
        assertEquals(Instant.parse("2022-01-01T00:00:00Z"), bars.get(2).getBeginTime());
    }

    @Test
    void cleanExcessBars_differentSymbols_excessBarsDeleted() {
        barModelRepository.insertOrReplace(barModel1);
        barModelRepository.insertOrReplace(barModel2);
        barModelRepository.insertOrReplace(barModel3); // barModel3 is a different symbol
        barModelRepository.insertOrReplace(barModel5);
        barModelRepository.insertOrReplace(barModel6);
        barModelRepository.insertOrReplace(barModel7);

        barModelRepository.cleanExcessBars("AAL", 2);

        List<BarModel> bars = mongoTemplate.findAll(BarModel.class);
        assertEquals(3, bars.size());
        assertEquals("DAL", bars.get(0).getSymbol());
        assertEquals("AAL", bars.get(1).getSymbol());
        assertEquals(Instant.parse("2021-04-04T00:00:00Z"), bars.get(1).getBeginTime());
        assertEquals("AAL", bars.get(2).getSymbol());
        assertEquals(Instant.parse("2022-01-01T00:00:00Z"), bars.get(2).getBeginTime());
    }

}
