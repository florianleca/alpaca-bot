package fr.flolec.alpacabot;

import fr.flolec.alpacabot.alpacaapi.httprequests.accountdetails.AccountDetailsServiceTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.asset.AssetServiceTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.bar.BarServiceTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteServiceTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.order.OrderServiceTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.position.PositionServiceTest;
import fr.flolec.alpacabot.alpacaapi.websocket.AlpacaWebSocketListenerTest;
import fr.flolec.alpacabot.strategies.strategy1.Strategy1ServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;


@Suite
@SelectClasses({
        AccountDetailsServiceTest.class,
        AssetServiceTest.class,
        BarServiceTest.class,
        LatestQuoteServiceTest.class,
        OrderServiceTest.class,
        PositionServiceTest.class,
        Strategy1ServiceTest.class,
        AlpacaWebSocketListenerTest.class
        })
public class TestSuite {
}
