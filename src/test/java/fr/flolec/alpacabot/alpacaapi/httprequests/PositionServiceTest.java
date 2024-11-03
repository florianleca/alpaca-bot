package fr.flolec.alpacabot.alpacaapi.httprequests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.client.RestClientTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;

import java.util.List;

import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
@RestClientTest(PositionService.class)
@ContextConfiguration(classes = {
        RestClientConfiguration.class,
        PositionService.class})
public class PositionServiceTest {

    public static final String RESPONSE_GET_A_POSITION_NOMINAL = "{\"asset_id\":\"64bbff51-59d6-4b3c-9351-13ad85e3c752\",\"symbol\":\"BTCUSD\",\"exchange\":\"CRYPTO\",\"asset_class\":\"crypto\",\"asset_marginable\":false,\"qty\":\"0.000014071\",\"avg_entry_price\":\"69673.01\",\"side\":\"long\",\"market_value\":\"0.977982341399999887432\",\"cost_basis\":\"0.980368924\",\"unrealized_pl\":\"-0.002386582600000112568\",\"unrealized_plpc\":\"-0.0024343719405779\",\"unrealized_intraday_pl\":\"-0.002386582310000112568\",\"unrealized_intraday_plpc\":\"-0.002434371645491\",\"current_price\":\"69503.399999999992\",\"lastday_price\":\"69341.7\",\"change_today\":\"0.0023319301372766\",\"qty_available\":\"0.000014071\"}";
    public static final String RESPONSE_GET_A_POSITION_ERROR_SYMBOL = "{\"code\": 40410000,\"message\": \"symbol not found: BTCdUSD\"}";
    public static final String RESPONSE_GET_A_POSITION_ERROR_NO_POSITION = "{\"code\": 40410000, \"message\": \"position does not exist\"}";
    public static final String RESPONSE_GET_ALL_POSITIONS_0 = "[]";
    public static final String RESPONSE_GET_ALL_POSITIONS_1 = "[{\"asset_id\":\"3530c688-9af2-45ab-98f5-ea31926a64e2\",\"symbol\":\"MKRUSD\",\"exchange\":\"CRYPTO\",\"asset_class\":\"crypto\",\"asset_marginable\":false,\"qty\":\"0.00040091\",\"avg_entry_price\":\"2443.8\",\"side\":\"long\",\"market_value\":\"0.9789363130052\",\"cost_basis\":\"0.979743858\",\"unrealized_pl\":\"-0.0008075449948\",\"unrealized_plpc\":\"-0.0008242409362468\",\"unrealized_intraday_pl\":\"-0.0008075449948\",\"unrealized_intraday_plpc\":\"-0.0008242409362468\",\"current_price\":\"2441.78572\",\"lastday_price\":\"2506.72\",\"change_today\":\"-0.0259040818280462\",\"qty_available\":\"0.00040091\"}]";
    public static final String RESPONSE_GET_ALL_POSITIONS_3 = "[{\"asset_id\":\"64bbff51-59d6-4b3c-9351-13ad85e3c752\",\"symbol\":\"BTCUSD\",\"exchange\":\"CRYPTO\",\"asset_class\":\"crypto\",\"asset_marginable\":false,\"qty\":\"0.000028164\",\"avg_entry_price\":\"69625.566969323\",\"side\":\"long\",\"market_value\":\"1.957493757599999774688\",\"cost_basis\":\"1.960934468\",\"unrealized_pl\":\"-0.003440710400000225312\",\"unrealized_plpc\":\"-0.0017546279369088\",\"unrealized_intraday_pl\":\"-0.003440710524013197312\",\"unrealized_intraday_plpc\":\"-0.0017546280000396\",\"current_price\":\"69503.399999999992\",\"lastday_price\":\"69341.7\",\"change_today\":\"0.0023319301372766\",\"qty_available\":\"0.000028164\"},{\"asset_id\":\"35f33a69-f5d6-4dc9-b158-4485e5e92e4b\",\"symbol\":\"ETHUSD\",\"exchange\":\"CRYPTO\",\"asset_class\":\"crypto\",\"asset_marginable\":false,\"qty\":\"0.000265462\",\"avg_entry_price\":\"3694.2\",\"side\":\"long\",\"market_value\":\"0.981446727674\",\"cost_basis\":\"0.98066972\",\"unrealized_pl\":\"0.000777007674\",\"unrealized_plpc\":\"0.0007923235092851\",\"unrealized_intraday_pl\":\"0.000777007274\",\"unrealized_intraday_plpc\":\"0.0007923231010774\",\"current_price\":\"3697.127\",\"lastday_price\":\"3689.94\",\"change_today\":\"0.0019477281473411\",\"qty_available\":\"0.000265462\"},{\"asset_id\":\"3530c688-9af2-45ab-98f5-ea31926a64e2\",\"symbol\":\"MKRUSD\",\"exchange\":\"CRYPTO\",\"asset_class\":\"crypto\",\"asset_marginable\":false,\"qty\":\"0.000401914\",\"avg_entry_price\":\"2437.53\",\"side\":\"long\",\"market_value\":\"0.98138786586808\",\"cost_basis\":\"0.979677432\",\"unrealized_pl\":\"0.00171043386808\",\"unrealized_plpc\":\"0.0017459153515338\",\"unrealized_intraday_pl\":\"0.00171043344808\",\"unrealized_intraday_plpc\":\"0.0017459149220728\",\"current_price\":\"2441.78572\",\"lastday_price\":\"2506.72\",\"change_today\":\"-0.0259040818280462\",\"qty_available\":\"0.000401914\"}]";
    public static final String RESPONSE_DELETE_POSITION_NOMINAL = "{\"id\":\"903a4f07-25b8-49e9-9d1f-d357ec560140\",\"client_order_id\":\"7c397c98-da39-4934-9289-02a6bcde0f52\",\"created_at\":\"2024-06-09T19:16:01.664950196Z\",\"updated_at\":\"2024-06-09T19:16:01.665600336Z\",\"submitted_at\":\"2024-06-09T19:16:01.664950196Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.000002804\",\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"sell\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}";
    public static final String RESPONSE_DELETE_POSITION_ERROR = "{\"code\":40010001,\"message\":\"percentage must be in between 0 and 100\"}";

    public static final String RESPONSE_DELETE_ALL_POSITIONS_NOMINAL_0 = "[]";
    public static final String RESPONSE_DELETE_ALL_POSITIONS_NOMINAL_1 = "[{\"symbol\":\"BTCUSD\",\"status\":200,\"body\":{\"id\":\"54886451-7398-4ca1-afae-6aad030ac7cb\",\"client_order_id\":\"05000581-ae7a-40cb-a921-9da50520607e\",\"created_at\":\"2024-06-12T14:30:46.324245953Z\",\"updated_at\":\"2024-06-12T14:30:46.325122583Z\",\"submitted_at\":\"2024-06-12T14:30:46.324245953Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.000014016\",\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"sell\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}}]";
    public static final String RESPONSE_DELETE_ALL_POSITIONS_NOMINAL_3 = "[{\"symbol\":\"BTCUSD\",\"status\":200,\"body\":{\"id\":\"410fbd82-7fbc-4940-9953-de7d0772fa47\",\"client_order_id\":\"ecfab9a9-3a4b-4079-a527-1dccd3cbbef7\",\"created_at\":\"2024-06-12T14:31:54.650475435Z\",\"updated_at\":\"2024-06-12T14:31:54.652423835Z\",\"submitted_at\":\"2024-06-12T14:31:54.650475435Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"276e2673-764b-4ab6-a611-caf665ca6340\",\"symbol\":\"BTC/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.000013997\",\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"sell\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}},{\"symbol\":\"ETHUSD\",\"status\":200,\"body\":{\"id\":\"94819895-0d54-48cc-97e5-c95f4be33846\",\"client_order_id\":\"6f6c4a68-cfe0-4db0-962f-4608a596bda1\",\"created_at\":\"2024-06-12T14:31:54.652449525Z\",\"updated_at\":\"2024-06-12T14:31:54.652943475Z\",\"submitted_at\":\"2024-06-12T14:31:54.652449525Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"a1733398-6acc-4e92-af24-0d0667f78713\",\"symbol\":\"ETH/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.000269088\",\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"sell\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}},{\"symbol\":\"MKRUSD\",\"status\":200,\"body\":{\"id\":\"8cf21d4b-a323-4a60-bbdd-07c2a7b5060f\",\"client_order_id\":\"0dcbdfb6-5844-4a19-9ab1-7d8726865ca2\",\"created_at\":\"2024-06-12T14:31:54.652968385Z\",\"updated_at\":\"2024-06-12T14:31:54.653394145Z\",\"submitted_at\":\"2024-06-12T14:31:54.652968385Z\",\"filled_at\":null,\"expired_at\":null,\"canceled_at\":null,\"failed_at\":null,\"replaced_at\":null,\"replaced_by\":null,\"replaces\":null,\"asset_id\":\"90a83e2d-e574-404b-9344-74130736b71c\",\"symbol\":\"MKR/USD\",\"asset_class\":\"crypto\",\"notional\":null,\"qty\":\"0.000415137\",\"filled_qty\":\"0\",\"filled_avg_price\":null,\"order_class\":\"\",\"order_type\":\"market\",\"type\":\"market\",\"side\":\"sell\",\"time_in_force\":\"gtc\",\"limit_price\":null,\"stop_price\":null,\"status\":\"pending_new\",\"extended_hours\":false,\"legs\":null,\"trail_percent\":null,\"trail_price\":null,\"hwm\":null,\"subtag\":null,\"source\":null}}]";
    public static final String RESPONSE_DELETE_ALL_POSITIONS_ERROR = "{\"code\":40010001,\"message\":\"un message d'erreur\"}";

    @Value("${ALPACA_API_POSITIONS_URI}")
    private String uri;

    @Autowired
    private PositionService positionService;

    @Autowired
    private MockRestServiceServer mockRestServiceServer;

    @Autowired
    private ObjectMapper objectMapper;

    private PositionService spiedPositionService;

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(positionService, "logger", logger);
        this.spiedPositionService = spy(positionService);
    }

    @Test
    @DisplayName("takeSlashOutOfSymbol: symbol with slash -> slash taken out")
    void takeSlashOutOfSymbol_symbolWithSlash_slashTakenOut() {
        assertEquals("BTCUSD", positionService.takeSlashOutOfSymbol("BTC/USD"));
    }

    @Test
    @DisplayName("takeSlashOutOfSymbol: symbol without slash -> nothing changes")
    void takeSlashOutOfSymbol_symbolWithoutSlash_nothingChanges() {
        assertEquals("BTCUSD", positionService.takeSlashOutOfSymbol("BTCUSD"));
    }

    @Test
    @DisplayName("getAnOpenPosition: nominal -> position serialized")
    void getAnOpenPosition_nominal_positionSerialized() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri + "/BTCUSD"))
                .andRespond(withSuccess(RESPONSE_GET_A_POSITION_NOMINAL, MediaType.APPLICATION_JSON));

        PositionModel positionModel = positionService.getAnOpenPosition("BTC/USD");

        assertNotNull(positionModel);
        assertEquals("64bbff51-59d6-4b3c-9351-13ad85e3c752", positionModel.getAssetId());
        assertEquals("BTCUSD", positionModel.getSymbol());
        assertEquals("CRYPTO", positionModel.getExchange());
        assertEquals("crypto", positionModel.getAssetClass());
        assertEquals("0.000014071", positionModel.getQuantity());
        assertEquals("69673.01", positionModel.getAverageEntryPrice());
        assertEquals("long", positionModel.getSide());
        assertEquals("0.977982341399999887432", positionModel.getMarketValue());
        assertEquals("0.980368924", positionModel.getCostBasis());
        assertEquals("69503.399999999992", positionModel.getCurrentPrice());
        assertEquals("69341.7", positionModel.getLastDayPrice());
        assertEquals("0.0023319301372766", positionModel.getChangeToday());
        assertEquals("0.000014071", positionModel.getQuantityAvailable());
    }

    @Test
    @DisplayName("getAnOpenPosition: symbol not found -> logged error")
    void getAnOpenPosition_symbolNotFound_loggedError() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri + "/BTCdUSD"))
                .andRespond(withResourceNotFound()
                        .body(RESPONSE_GET_A_POSITION_ERROR_SYMBOL)
                        .contentType(MediaType.APPLICATION_JSON));

        PositionModel positionModel = positionService.getAnOpenPosition("BTCd/USD");

        assertNull(positionModel);
        verify(logger).warn("{} position could not be retrieved: {}", "BTCdUSD", "404 Not Found: \"{\"code\": 40410000,\"message\": \"symbol not found: BTCdUSD\"}\"");
    }

    @Test
    @DisplayName("getAnOpenPosition: position does not exist -> logged error")
    void getAnOpenPosition_positionDoesNotExist_loggedError() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri + "/MKRUSD"))
                .andRespond(withResourceNotFound()
                        .body(RESPONSE_GET_A_POSITION_ERROR_NO_POSITION)
                        .contentType(MediaType.APPLICATION_JSON));

        PositionModel positionModel = positionService.getAnOpenPosition("MKR/USD");

        assertNull(positionModel);
        verify(logger).warn("{} position could not be retrieved: {}", "MKRUSD", "404 Not Found: \"{\"code\": 40410000, \"message\": \"position does not exist\"}\"");
    }

    @Test
    @DisplayName("getAllOpenPositions: 0 open position -> empty list retrieved")
    void getAllOpenPositions_zeroOpenPosition_emptyListRetrieved() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andRespond(withSuccess(RESPONSE_GET_ALL_POSITIONS_0, MediaType.APPLICATION_JSON));

        List<PositionModel> positionModels = positionService.getAllOpenPositions();

        assertNotNull(positionModels);
        assertEquals(0, positionModels.size());
    }

    @Test
    @DisplayName("getAllOpenPositions: 1 open position -> 1 element retrieved")
    void getAllOpenPositions_oneOpenPosition_oneElementRetrieved() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andRespond(withSuccess(RESPONSE_GET_ALL_POSITIONS_1, MediaType.APPLICATION_JSON));

        List<PositionModel> positionModels = positionService.getAllOpenPositions();

        assertNotNull(positionModels);
        assertEquals(1, positionModels.size());
        assertNotNull(positionModels.get(0));
        assertEquals("MKRUSD", positionModels.get(0).getSymbol());
    }

    @Test
    @DisplayName("getAllOpenPositions: 3 open positions -> 3 elements retrieved")
    void getAllOpenPositions_threeOpenPositions_threeElementsRetrieved() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andRespond(withSuccess(RESPONSE_GET_ALL_POSITIONS_3, MediaType.APPLICATION_JSON));

        List<PositionModel> positionModels = positionService.getAllOpenPositions();

        assertNotNull(positionModels);
        assertEquals(3, positionModels.size());
        assertNotNull(positionModels.get(0));
        assertEquals("BTCUSD", positionModels.get(0).getSymbol());
        assertNotNull(positionModels.get(1));
        assertEquals("ETHUSD", positionModels.get(1).getSymbol());
        assertNotNull(positionModels.get(2));
        assertEquals("MKRUSD", positionModels.get(2).getSymbol());
    }

    @Test
    @DisplayName("getAllOpenPositions: error -> no retrieved position & logged error")
    void getAllOpenPositions_error_noRetrievedPositionAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.GET))
                .andExpect(requestTo(uri))
                .andRespond(withStatus(HttpStatus.FORBIDDEN).body("{error body}"));

        List<PositionModel> positionModels = positionService.getAllOpenPositions();

        assertNotNull(positionModels);
        assertEquals(0, positionModels.size());
        verify(logger).warn("All open positions could not be retrieved: {}", "403 Forbidden: \"{error body}\"");
    }

    @Test
    @DisplayName("getCurrentQtyOfAsset: position exists -> quantity retrieved")
    void getCurrentQtyOfAsset_positionExists_quantityRetrieved() {
        PositionModel positionModel = new PositionModel();
        positionModel.setQuantity("123.4");
        doReturn(positionModel).when(spiedPositionService).getAnOpenPosition("TEST");
        assertEquals(123.4, spiedPositionService.getCurrentQtyOfAsset("TEST"));
    }

    @Test
    @DisplayName("getCurrentQtyOfAsset: no position -> 0 quantity")
    void getCurrentQtyOfAsset_noPosition_zeroQuantity() {
        doReturn(null).when(spiedPositionService).getAnOpenPosition("TEST");
        assertEquals(0, spiedPositionService.getCurrentQtyOfAsset("TEST"));
    }

    @Test
    @DisplayName("getCurrentQtyOfAsset: null quantity -> 0 quantity")
    void getCurrentQtyOfAsset_nullQuantity_zeroQuantity() {
        PositionModel positionModel = new PositionModel();
        doReturn(positionModel).when(spiedPositionService).getAnOpenPosition("TEST");
        assertEquals(0, spiedPositionService.getCurrentQtyOfAsset("TEST"));
    }

    @Test
    @DisplayName("liquidatePositionByPercentage: nominal -> sell order created")
    void liquidatePositionByPercentage_nominal_sellOrderCreated() throws JsonProcessingException {
        OrderModel expectedOrderModel = new OrderModel();
        expectedOrderModel.setSymbol("BTC/USD");
        expectedOrderModel.setSide("sell");
        String jsonResponse = objectMapper.writeValueAsString(expectedOrderModel);

        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(startsWith(uri + "/BTCUSD")))
                .andExpect(queryParam("percentage", "66.600000000"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        OrderModel orderModel = positionService.liquidatePositionByPercentage("BTC/USD", 66.6);

        assertNotNull(orderModel);
        assertEquals("BTC/USD", orderModel.getSymbol());
        assertEquals("sell", orderModel.getSide());
    }

    @Test
    @DisplayName("liquidatePositionByPercentage: error -> no sell order created & logged error")
    void liquidatePositionByPercentage_error_noSellOrderCreatedAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(startsWith(uri + "/BTCUSD")))
                .andExpect(queryParam("percentage", "66.600000000"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("{error body}")
                        .contentType(MediaType.APPLICATION_JSON));

        OrderModel orderModel = positionService.liquidatePositionByPercentage("BTC/USD", 66.6);

        assertNull(orderModel);
        verify(logger).warn("{} position could not be liquidated by percentage: {}", "BTCUSD", "422 Unprocessable Entity: \"{error body}\"");
    }

    @Test
    @DisplayName("liquidatePositionByQuantity: nominal -> sell order created")
    void liquidatePositionByQuantity_nominal_sellOrderCreated() throws JsonProcessingException {
        OrderModel expectedOrderModel = new OrderModel();
        expectedOrderModel.setSymbol("BTC/USD");
        expectedOrderModel.setSide("sell");
        String jsonResponse = objectMapper.writeValueAsString(expectedOrderModel);

        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(startsWith(uri + "/BTCUSD")))
                .andExpect(queryParam("qty", "12.340000000"))
                .andRespond(withSuccess(jsonResponse, MediaType.APPLICATION_JSON));

        OrderModel orderModel = positionService.liquidatePositionByQuantity("BTC/USD", 12.34);

        assertNotNull(orderModel);
        assertEquals("BTC/USD", orderModel.getSymbol());
        assertEquals("sell", orderModel.getSide());
    }

    @Test
    @DisplayName("liquidatePositionByQuantity: error -> no sell order created & logged error")
    void liquidatePositionByQuantity_error_noSellOrderCreatedAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(startsWith(uri + "/BTCUSD")))
                .andExpect(queryParam("qty", "12.340000000"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body("{error body}")
                        .contentType(MediaType.APPLICATION_JSON));

        OrderModel orderModel = positionService.liquidatePositionByQuantity("BTC/USD", 12.34);

        assertNull(orderModel);
        verify(logger).warn("{} position could not be liquidated by quantity: {}", "BTCUSD", "422 Unprocessable Entity: \"{error body}\"");
    }

    @Test
    @DisplayName("liquidatePosition: nominal -> sell order created")
    void liquidatePosition_nominal_sellOrderCreated() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(uri + "/BTCUSD"))
                .andRespond(withSuccess(RESPONSE_DELETE_POSITION_NOMINAL, MediaType.APPLICATION_JSON));

        OrderModel orderModel = positionService.liquidatePosition("BTC/USD");

        assertNotNull(orderModel);
        assertEquals("BTC/USD", orderModel.getSymbol());
        assertEquals("sell", orderModel.getSide());
    }

    @Test
    @DisplayName("liquidatePosition: error -> no sell order & logged error")
    void liquidatePosition_error_noSellOrderAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(uri + "/BTCUSD"))
                .andRespond(withStatus(HttpStatus.UNPROCESSABLE_ENTITY)
                        .body(RESPONSE_DELETE_POSITION_ERROR)
                        .contentType(MediaType.APPLICATION_JSON));

        OrderModel orderModel = positionService.liquidatePosition("BTC/USD");

        assertNull(orderModel);
        verify(logger).warn("{} position could not be liquidated: {}", "BTCUSD", "422 Unprocessable Entity: \"{\"code\":40010001,\"message\":\"percentage must be in between 0 and 100\"}\"");
    }

    @Test
    @DisplayName("liquidateAllPositions: 0 open position -> 0 sell order")
    void liquidateAllPositions_zeroOpenPosition_zeroSellOrder() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(uri + "?cancel_orders=false"))
                .andRespond(withSuccess(RESPONSE_DELETE_ALL_POSITIONS_NOMINAL_0, MediaType.APPLICATION_JSON));

        List<OrderModel> orderModels = positionService.liquidateAllPositions(false);

        assertNotNull(orderModels);
        assertEquals(0, orderModels.size());
    }

    @Test
    @DisplayName("liquidateAllPositions: 1 open position -> 1 sell order")
    void liquidateAllPositions_oneOpenPosition_oneSellOrder() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(uri + "?cancel_orders=false"))
                .andRespond(withSuccess(RESPONSE_DELETE_ALL_POSITIONS_NOMINAL_1, MediaType.APPLICATION_JSON));

        List<OrderModel> orderModels = positionService.liquidateAllPositions(false);

        assertNotNull(orderModels);
        assertEquals(1, orderModels.size());
        assertNotNull(orderModels.get(0));
        assertEquals("BTC/USD", orderModels.get(0).getSymbol());
        assertEquals("sell", orderModels.get(0).getSide());
        assertEquals("pending_new", orderModels.get(0).getStatus());
    }

    @Test
    @DisplayName("liquidateAllPositions: 3 open positions -> 3 sell orders")
    void liquidateAllPositions_threeOpenPositions_threeSellOrders() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(uri + "?cancel_orders=false"))
                .andRespond(withSuccess(RESPONSE_DELETE_ALL_POSITIONS_NOMINAL_3, MediaType.APPLICATION_JSON));

        List<OrderModel> orderModels = positionService.liquidateAllPositions(false);

        assertNotNull(orderModels);
        assertEquals(3, orderModels.size());
        assertNotNull(orderModels.get(0));
        assertEquals("BTC/USD", orderModels.get(0).getSymbol());
        assertEquals("sell", orderModels.get(0).getSide());
        assertEquals("pending_new", orderModels.get(0).getStatus());
        assertNotNull(orderModels.get(1));
        assertEquals("ETH/USD", orderModels.get(1).getSymbol());
        assertEquals("sell", orderModels.get(1).getSide());
        assertEquals("pending_new", orderModels.get(1).getStatus());
        assertNotNull(orderModels.get(2));
        assertEquals("MKR/USD", orderModels.get(2).getSymbol());
        assertEquals("sell", orderModels.get(2).getSide());
        assertEquals("pending_new", orderModels.get(2).getStatus());
    }

    @Test
    @DisplayName("liquidateAllPositions: error -> no sell order & logged error")
    void liquidateAllPositions_error_noSellOrderAndLoggedError() {
        mockRestServiceServer.expect(method(HttpMethod.DELETE))
                .andExpect(requestTo(uri + "?cancel_orders=false"))
                .andRespond(withBadRequest()
                        .body(RESPONSE_DELETE_ALL_POSITIONS_ERROR)
                        .contentType(MediaType.APPLICATION_JSON));

        List<OrderModel> orderModels = positionService.liquidateAllPositions(false);

        assertEquals(0, orderModels.size());
        verify(logger).warn("All positions could not be liquidated: {}", "400 Bad Request: \"{\"code\":40010001,\"message\":\"un message d'erreur\"}\"");
    }

}