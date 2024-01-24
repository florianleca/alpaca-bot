package fr.flolec.alpacabot;

import fr.flolec.alpacabot.accountdetails.AccountDetailsServiceTest;
import fr.flolec.alpacabot.asset.AssetServiceTest;
import fr.flolec.alpacabot.bar.BarServiceTest;
import fr.flolec.alpacabot.alpacaapi.httprequests.latestquote.LatestQuoteService;
import fr.flolec.alpacabot.order.OrderServiceTest;
import fr.flolec.alpacabot.position.PositionServiceTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;


@Suite
@SelectClasses({
        AccountDetailsServiceTest.class,
        AssetServiceTest.class,
        BarServiceTest.class,
        LatestQuoteService.class,
        OrderServiceTest.class,
        PositionServiceTest.class,
        })
public class TestSuite {
}
