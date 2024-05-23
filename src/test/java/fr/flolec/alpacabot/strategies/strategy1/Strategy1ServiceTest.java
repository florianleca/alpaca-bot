package fr.flolec.alpacabot.strategies.strategy1;

import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderModel;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderService;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderSide;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.TimeInForce;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class Strategy1ServiceTest {

    private final AssetModel asset1 = new AssetModel();
    private final AssetModel asset2 = new AssetModel();

    @Mock
    private OrderService orderService;

    @Mock
    private Strategy1TicketRepository strategy1TicketRepository;

    @Mock
    private Strategy1OpportunityChecker strategy1OpportunityChecker;

    @Mock
    private Logger logger;

    @Spy
    @InjectMocks
    private Strategy1Service strategy1Service;

    @BeforeEach
    void setUp() {
        strategy1Service.setNotional(1.);
        strategy1Service.setGainPercentage(2.);
        strategy1Service.setLogger(logger);
    }

    @Test
    @DisplayName("Check buy opportunities and buy")
    void checkBuyOpportunitiesAndBuy() throws IOException {
        when(strategy1OpportunityChecker.checkBuyOpportunities()).thenReturn(List.of(asset1, asset2));
        doNothing().when(strategy1Service).createBuyOrder(any());

        strategy1Service.checkBuyOpportunitiesAndBuy();

        verify(strategy1Service, times(1)).createBuyOrder(asset1);
        verify(strategy1Service, times(1)).createBuyOrder(asset2);
    }

    @Test
    @DisplayName("Create buy order - nominal")
    void createBuyOrder() throws IOException {
        asset1.setSymbol("BTC/USD");
        OrderModel buyOrder = new OrderModel();
        buyOrder.setId("buyOrderId");

        when(orderService.createMarketNotionalOrder(any(), any(), any(), any())).thenReturn(buyOrder);

        strategy1Service.createBuyOrder(asset1);

        verify(logger, times(1)).info("Trying to buy some {}...", "BTC/USD");
        verify(orderService, times(1)).createMarketNotionalOrder(
                "BTC/USD",
                "1.0",
                OrderSide.BUY,
                TimeInForce.GTC);
        verify(logger, times(0)).error(anyString());
    }

    @Test
    @DisplayName("Create buy order - error")
    void createBuyOrderError() throws IOException {
        asset1.setSymbol("BTC/USD");
        doThrow(new IOException("error message")).when(orderService).createMarketNotionalOrder(
                "BTC/USD",
                "1.0",
                OrderSide.BUY,
                TimeInForce.GTC);

        strategy1Service.createBuyOrder(asset1);

        verify(logger, times(1)).info("Trying to buy some {}...", "BTC/USD");
        verify(orderService, times(1)).createMarketNotionalOrder(
                "BTC/USD",
                "1.0",
                OrderSide.BUY,
                TimeInForce.GTC);
        verify(logger, times(1)).error(
                "Failed to buy some {}: {}",
                "BTC/USD",
                "error message"
        );
    }

    @Test
    @DisplayName("Create sell order - nominal")
    void createSellOrder() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setSymbol("BTC/USD");
        ticket.setBoughtQuantity(32);
        ticket.setAverageFilledBuyPrice(100);

        OrderModel sellOrder = new OrderModel();
        sellOrder.setId("sellOrderId");
        when(orderService.createLimitQuantityOrder(any(), any(), any(), any(), any())).thenReturn(sellOrder);

        strategy1Service.createSellOrder(ticket);

        assertEquals("sellOrderId", ticket.getSellOrderId());
        assertEquals(Strategy1TicketStatus.SELL_UNFILLED, ticket.getStatus());
        verify(orderService, times(1)).createLimitQuantityOrder(
                "BTC/USD",
                "32.0",
                OrderSide.SELL,
                TimeInForce.GTC,
                "102.0");
        verify(strategy1TicketRepository, times(1)).save(ticket);
        verify(logger, times(1)).info("Successfully created a SELL order for {} ticket", "BTC/USD");
    }

    @Test
    @DisplayName("Create sell order - error")
    void createSellOrderError() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setSymbol("BTC/USD");
        ticket.setBoughtQuantity(32);
        ticket.setAverageFilledBuyPrice(100);

        doThrow(new IOException("error message")).when(orderService).createLimitQuantityOrder(any(), any(), any(), any(), any());

        strategy1Service.createSellOrder(ticket);

        verify(orderService, times(1)).createLimitQuantityOrder(
                "BTC/USD",
                "32.0",
                OrderSide.SELL,
                TimeInForce.GTC,
                "102.0");
        verify(logger, times(1)).error(
                "Failed to create a sell order for {}: {}",
                "BTC/USD",
                "error message"
        );
    }

    @Test
    @DisplayName("Process filled order - buy")
    void processFilledOrderBuy() {
        OrderModel order = new OrderModel();
        order.setSide("buy");

        doNothing().when(strategy1Service).processFilledBuyOrder(order);

        strategy1Service.processFilledOrder(order);

        verify(strategy1Service, times(1)).processFilledBuyOrder(order);
        verify(strategy1Service, times(0)).processFilledSellOrder(any());
    }

    @Test
    @DisplayName("Process filled order - sell")
    void processFilledOrderSell() {
        OrderModel order = new OrderModel();
        order.setSide("sell");

        doNothing().when(strategy1Service).processFilledSellOrder(order);

        strategy1Service.processFilledOrder(order);

        verify(strategy1Service, times(1)).processFilledSellOrder(order);
        verify(strategy1Service, times(0)).processFilledBuyOrder(any());
    }

    @Test
    @DisplayName("Process filled buy order - nominal")
    void processFilledBuyOrder() {
        OrderModel order = new OrderModel();
        order.setSide("buy");
        order.setStatus("filled");
        order.setSymbol("BTC/USD");
        order.setId("buyOrderId");
        order.setFilledQuantity(32);
        order.setFilledAvgPrice(100);

        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setStatus(Strategy1TicketStatus.BUY_UNFILLED);

        when(strategy1TicketRepository.findByOrder("buyOrderId")).thenReturn(ticket);
        doNothing().when(strategy1Service).createSellOrder(any());

        strategy1Service.processFilledBuyOrder(order);

        verify(logger, times(1)).info("[💸] Bought ${} worth of {}!", 1., "BTC");
        verify(strategy1Service, times(1)).createSellOrder(any());
    }

    @Test
    @DisplayName("Process filled buy order - no ticket in database")
    void processFilledBuyOrderNoTicket() {
        OrderModel order = new OrderModel();
        order.setSymbol("BTC/USD");
        order.setId("buyOrderId");

        when(strategy1TicketRepository.findByOrder("buyOrderId")).thenReturn(null);

        strategy1Service.processFilledBuyOrder(order);

        verify(logger, times(1)).error("Trying to process a filled buy order that was not linked to any ticket in the database: {}", "buyOrderId");
        verify(strategy1Service, times(0)).createSellOrder(any());
    }

    @Test
    @DisplayName("Process filled buy order - ticket status not BUY_UNFILLED")
    void processFilledBuyOrderWrongStatus() {
        OrderModel order = new OrderModel();
        order.setSymbol("BTC/USD");
        order.setId("buyOrderId");

        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setStatus(Strategy1TicketStatus.SELL_UNFILLED);

        when(strategy1TicketRepository.findByOrder("buyOrderId")).thenReturn(ticket);

        strategy1Service.processFilledBuyOrder(order);

        verify(logger, times(1)).error("Trying to process a filled buy order which ticket was not \"BUY_UNFILLED\" but {}", Strategy1TicketStatus.SELL_UNFILLED);
        verify(strategy1Service, times(0)).createSellOrder(any());
    }

    @Test
    @DisplayName("Process filled sell order - nominal")
    void processFilledSellOrder() {
        OrderModel order = new OrderModel();
        order.setSide("sell");
        order.setSymbol("BTC/USD");
        order.setId("sellOrderId");
        order.setFilledQuantity(32);
        order.setFilledAvgPrice(100);

        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setSymbol("BTC/USD");
        ticket.setStatus(Strategy1TicketStatus.SELL_UNFILLED);

        when(strategy1TicketRepository.findByOrder("sellOrderId")).thenReturn(ticket);

        strategy1Service.processFilledSellOrder(order);

        assertEquals(Strategy1TicketStatus.COMPLETE, ticket.getStatus());
        assertEquals(32, ticket.getSoldQuantity());
        assertEquals(100, ticket.getAverageFilledSellPrice());
        verify(strategy1TicketRepository, times(1)).save(ticket);
        verify(logger, times(1)).info("This {} ticket is now completed!", "BTC/USD");
    }

    @Test
    @DisplayName("Process filled sell order - no ticket in database")
    void processFilledSellOrderNoTicket() {
        OrderModel order = new OrderModel();
        order.setSymbol("BTC/USD");
        order.setId("sellOrderId");

        when(strategy1TicketRepository.findByOrder("sellOrderId")).thenReturn(null);

        strategy1Service.processFilledSellOrder(order);

        verify(logger, times(1)).error("Trying to process a filled sell order that was not linked to any ticket in the database: {}", "sellOrderId");
    }

    @Test
    @DisplayName("Process filled sell order - ticket status not SELL_UNFILLED")
    void processFilledSellOrderWrongStatus() {
        OrderModel order = new OrderModel();
        order.setSymbol("BTC/USD");
        order.setId("sellOrderId");

        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setStatus(Strategy1TicketStatus.BUY_UNFILLED);

        when(strategy1TicketRepository.findByOrder("sellOrderId")).thenReturn(ticket);

        strategy1Service.processFilledSellOrder(order);

        verify(logger, times(1)).error("Trying to process a filled sell order which ticket was not \"SELL_UNFILLED\" but {}", Strategy1TicketStatus.BUY_UNFILLED);
    }

    @Test
    @DisplayName("Update uncompleted tickets")
    void updateUncompletedTickets() {
        Strategy1TicketModel ticket1 = new Strategy1TicketModel();
        Strategy1TicketModel ticket2 = new Strategy1TicketModel();
        List<Strategy1TicketModel> uncompletedTickets = List.of(ticket1, ticket2);

        when(strategy1TicketRepository.findUncompletedTickets()).thenReturn(uncompletedTickets);
        doNothing().when(strategy1Service).updateUncompletedTicket(any());

        strategy1Service.updateUncompletedTickets();

        verify(strategy1Service, times(1)).updateUncompletedTicket(ticket1);
        verify(strategy1Service, times(1)).updateUncompletedTicket(ticket2);
    }

    @Test
    @DisplayName("Update uncompleted ticket - buy unfilled")
    void updateUncompletedTicketBuyUnfilled() {
        Strategy1TicketModel ticketBuyUnfilled = new Strategy1TicketModel();
        ticketBuyUnfilled.setStatus(Strategy1TicketStatus.BUY_UNFILLED);

        doNothing().when(strategy1Service).updateBuyUnfilledTicket(any());

        strategy1Service.updateUncompletedTicket(ticketBuyUnfilled);

        verify(strategy1Service, times(1)).updateBuyUnfilledTicket(ticketBuyUnfilled);
        verify(strategy1Service, times(0)).updateSellUnfilledTicket(any());
    }

    @Test
    @DisplayName("Update uncompleted ticket - sell unfilled")
    void updateUncompletedTicketSellUnfilled() {
        Strategy1TicketModel ticketSellUnfilled = new Strategy1TicketModel();
        ticketSellUnfilled.setStatus(Strategy1TicketStatus.SELL_UNFILLED);

        doNothing().when(strategy1Service).updateSellUnfilledTicket(any());

        strategy1Service.updateUncompletedTicket(ticketSellUnfilled);

        verify(strategy1Service, times(0)).updateBuyUnfilledTicket(ticketSellUnfilled);
        verify(strategy1Service, times(1)).updateSellUnfilledTicket(any());
    }

    @Test
    @DisplayName("Update buy unfilled ticket - nominal - was filled")
    void updateBuyUnfilledTicketWasFilled() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setBuyOrderId("buyOrderId");

        OrderModel order = new OrderModel();
        order.setStatus("filled");

        when(orderService.getOrderById("buyOrderId")).thenReturn(order);
        doNothing().when(strategy1Service).processFilledBuyOrder(order);

        strategy1Service.updateBuyUnfilledTicket(ticket);

        verify(orderService, times(1)).getOrderById("buyOrderId");
        verify(strategy1Service, times(1)).processFilledBuyOrder(order);
    }

    @Test
    @DisplayName("Update buy unfilled ticket - nominal - was not filled")
    void updateBuyUnfilledTicketWasNotFilled() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setBuyOrderId("buyOrderId");

        OrderModel order = new OrderModel();
        order.setStatus("pending_new");

        when(orderService.getOrderById("buyOrderId")).thenReturn(order);

        strategy1Service.updateBuyUnfilledTicket(ticket);

        verify(orderService, times(1)).getOrderById("buyOrderId");
        verify(strategy1Service, times(0)).processFilledBuyOrder(order);
    }

    @Test
    @DisplayName("Update buy unfilled ticket - error")
    void updateBuyUnfilledTicketError() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setBuyOrderId("buyOrderId");

        doThrow(new IOException("error message")).when(orderService).getOrderById("buyOrderId");

        strategy1Service.updateBuyUnfilledTicket(ticket);

        verify(orderService, times(1)).getOrderById("buyOrderId");
        verify(logger, times(1)).error(
                "Failed to retrieve buy order of ticket {}: {}",
                ticket.getId(),
                "error message"
        );
    }

    @Test
    @DisplayName("Update sell unfilled ticket - nominal - was filled")
    void updateSellUnfilledTicketWasFilled() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setSellOrderId("sellOrderId");

        OrderModel order = new OrderModel();
        order.setStatus("filled");

        when(orderService.getOrderById("sellOrderId")).thenReturn(order);
        doNothing().when(strategy1Service).processFilledSellOrder(order);

        strategy1Service.updateSellUnfilledTicket(ticket);

        verify(orderService, times(1)).getOrderById("sellOrderId");
        verify(strategy1Service, times(1)).processFilledSellOrder(order);
    }

    @Test
    @DisplayName("Update sell unfilled ticket - nominal - was not filled")
    void updateSellUnfilledTicketWasNotFilled() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setSellOrderId("sellOrderId");

        OrderModel order = new OrderModel();
        order.setStatus("pending_new");

        when(orderService.getOrderById("sellOrderId")).thenReturn(order);

        strategy1Service.updateSellUnfilledTicket(ticket);

        verify(orderService, times(1)).getOrderById("sellOrderId");
        verify(strategy1Service, times(0)).processFilledSellOrder(order);
    }

    @Test
    @DisplayName("Update sell unfilled ticket - error")
    void updateSellUnfilledTicketError() throws IOException {
        Strategy1TicketModel ticket = new Strategy1TicketModel();
        ticket.setSellOrderId("sellOrderId");

        doThrow(new IOException("error message")).when(orderService).getOrderById("sellOrderId");

        strategy1Service.updateSellUnfilledTicket(ticket);

        verify(orderService, times(1)).getOrderById("sellOrderId");
        verify(logger, times(1)).error(
                "Failed to retrieve sell order of ticket {}: {}",
                ticket.getId(),
                "error message"
        );
    }

}