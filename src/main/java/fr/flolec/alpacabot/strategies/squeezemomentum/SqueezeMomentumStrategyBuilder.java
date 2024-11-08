package fr.flolec.alpacabot.strategies.squeezemomentum;

import fr.flolec.alpacabot.indicators.squeezemomentum.MomentumIndicator;
import fr.flolec.alpacabot.indicators.squeezemomentum.SqueezeCountIndicator;
import fr.flolec.alpacabot.indicators.squeezemomentum.SqueezeIndicator;
import fr.flolec.alpacabot.indicators.squeezemomentum.SqueezeReleaseIndicator;
import fr.flolec.alpacabot.strategies.StrategyBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import org.ta4j.core.Strategy;
import org.ta4j.core.indicators.EMAIndicator;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.rules.BooleanIndicatorRule;
import org.ta4j.core.rules.OverIndicatorRule;

/**
 * Squeeze Momentum Indicator [LazyBear]
 */
@Component
public class SqueezeMomentumStrategyBuilder implements StrategyBuilder {

    private final int lengthBB;

    private final double multFactorBB;

    private final int lengthKC;

    private final double multFactorKC;

    private final int squeezeOnCount;

    private SqueezeReleaseIndicator squeezeReleaseIndicator;
    private MomentumIndicator momentumIndicator;

    public SqueezeMomentumStrategyBuilder(@Value("${SQZMO_LENGTH_BB}") int lengthBB,
                                          @Value("${SQZMO_MULT_BB}") double multFactorBB,
                                          @Value("${SQZMO_LENGTH_KC}") int lengthKC,
                                          @Value("${SQZMO_MULT_KC}") double multFactorKC,
                                          @Value("${SQZMO_SQUEEZE_ON_COUNT}") int squeezeOnCount) {
        this.lengthBB = lengthBB;
        this.multFactorBB = multFactorBB;
        this.lengthKC = lengthKC;
        this.multFactorKC = multFactorKC;
        this.squeezeOnCount = squeezeOnCount;
    }

    public Strategy buildStrategy(BarSeries series) {
        initIndicators(series);

        // Au-dessus d’une 200 EMA
        EMAIndicator ema200 = new EMAIndicator(new ClosePriceIndicator(series), 200);
        Rule above200EMA = new OverIndicatorRule(new ClosePriceIndicator(series), ema200);

        // On attend au moins 6 sqOn suivis d'un sqOff
        Rule sixSqOn = new BooleanIndicatorRule(squeezeReleaseIndicator);

        // MomentumIndicator lime green
        // bcolor = iff( val > 0, iff( val > nz(val[1]), lime, green), iff( val < nz(val[1]), red, maroon))
        Rule valGreaterThanZero = new OverIndicatorRule(momentumIndicator, 0);
        PreviousValueIndicator previousVal = new PreviousValueIndicator(momentumIndicator, 1);
        Rule valGreaterThanPrevious = new OverIndicatorRule(momentumIndicator, previousVal);
        Rule limeGreen = valGreaterThanZero.and(valGreaterThanPrevious);

        // Règle d'achat
        Rule entryRule = above200EMA.and(sixSqOn).and(limeGreen);

        // Règle de vente
        Rule exitRule = limeGreen.negation();

        int unstableBars = Math.max(squeezeReleaseIndicator.getUnstableBars(), momentumIndicator.getUnstableBars());
        return new BaseStrategy("Squeeze Momentum", entryRule, exitRule, unstableBars);
    }

    private void initIndicators(BarSeries series) {
        SqueezeIndicator squeezeIndicator = new SqueezeIndicator(series, lengthBB, multFactorBB, lengthKC, multFactorKC);
        SqueezeCountIndicator squeezeCountIndicator = new SqueezeCountIndicator(squeezeIndicator);
        this.squeezeReleaseIndicator = new SqueezeReleaseIndicator(squeezeCountIndicator, squeezeOnCount);
        this.momentumIndicator = new MomentumIndicator(series, lengthKC);
    }

}
