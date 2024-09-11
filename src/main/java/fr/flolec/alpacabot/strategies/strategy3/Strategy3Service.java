package fr.flolec.alpacabot.strategies.strategy3;

import fr.flolec.alpacabot.indicators.AverageIndicator;
import fr.flolec.alpacabot.indicators.DifferenceIndicator;
import org.springframework.beans.factory.annotation.Value;
import org.ta4j.core.BarSeries;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.keltner.KeltnerChannelFacade;
import org.ta4j.core.indicators.statistics.SimpleLinearRegressionIndicator;
import org.ta4j.core.rules.OverIndicatorRule;
import org.ta4j.core.rules.UnderIndicatorRule;

/**
 * Squeeze Momentum Indicator [LazyBear]
 */
public class Strategy3Service {

    /**
     * length = input(20, title="BB Length")
     * mult = input(2.0,title="BB MultFactor")
     * lengthKC=input(20, title="KC Length")
     * multKC = input(1.5, title="KC MultFactor")
     */

    @Value("${S3_LENGTH_BB}")
    private int lengthBB;

    @Value("${S3_MULT_BB}")
    private double multFactorBB;

    @Value("${S3_LENGTH_KC}")
    private int lengthKC;

    @Value("${S3_MULT_KC}")
    private double multFactorKC;

    public Strategy buildStrategy3(BarSeries series) {

        // Bollinger Bands
        BollingerBandFacade bollingerBandFacade = new BollingerBandFacade(series, lengthBB, multFactorBB);

        // Keltner Channel
        KeltnerChannelFacade keltnerChannelFacade = new KeltnerChannelFacade(series, lengthKC, lengthKC, multFactorKC);

        // sqzOn  = (lowerBB > lowerKC) and (upperBB < upperKC)
        Rule sqzOnRule = new OverIndicatorRule(bollingerBandFacade.lower(), keltnerChannelFacade.lower())
                .and(new UnderIndicatorRule(bollingerBandFacade.upper(), keltnerChannelFacade.upper()));

        // sqzOff = (lowerBB < lowerKC) and (upperBB > upperKC)
        Rule sqzOffRule = new UnderIndicatorRule(bollingerBandFacade.lower(), keltnerChannelFacade.lower())
                .and(new OverIndicatorRule(bollingerBandFacade.upper(), keltnerChannelFacade.upper()));

        // noSqz  = (sqzOn == false) and (sqzOff == false)
        Rule noSqzRule = sqzOnRule.negation().and(sqzOffRule.negation());


        // val = linreg(source - avg(avg(highest(high, lengthKC), lowest(low, lengthKC)), sma(close,lengthKC)), lengthKC, 0)
        HighestValueIndicator highPrice = new HighestValueIndicator(new HighPriceIndicator(series), lengthKC);
        LowestValueIndicator lowPrice = new LowestValueIndicator(new LowPriceIndicator(series), lengthKC);
        SMAIndicator smaClose = new SMAIndicator(new ClosePriceIndicator(series), lengthKC);
        AverageIndicator avgHighLow = new AverageIndicator(highPrice, lowPrice);
        AverageIndicator avgHighLowSmaClose = new AverageIndicator(avgHighLow, smaClose);
        DifferenceIndicator diff = new DifferenceIndicator(new ClosePriceIndicator(series), avgHighLowSmaClose);
        SimpleLinearRegressionIndicator val = new SimpleLinearRegressionIndicator(diff, lengthKC);

        // bcolor = iff( val > 0, iff( val > nz(val[1]), lime, green), iff( val < nz(val[1]), red, maroon))
        PreviousValueIndicator previousVal = new PreviousValueIndicator(val, 1);
        Rule valGreaterThanZero = new OverIndicatorRule(val, 0);
        Rule valGreaterThanPrevious = new OverIndicatorRule(val, previousVal);
        Rule valLesserThanPrevious = new UnderIndicatorRule(val, previousVal);

        Rule limeRule = valGreaterThanZero.and(valGreaterThanPrevious);
        Rule greenRule = valGreaterThanZero.and(valGreaterThanPrevious.negation());

        Rule redRule = valGreaterThanZero.negation().and(valLesserThanPrevious);
        Rule maroonRule = valGreaterThanZero.negation().and(valLesserThanPrevious.negation());

        return null;
    }

}
