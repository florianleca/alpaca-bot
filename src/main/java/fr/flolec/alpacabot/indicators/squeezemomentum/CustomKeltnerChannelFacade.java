package fr.flolec.alpacabot.indicators.squeezemomentum;

import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.TRIndicator;
import org.ta4j.core.indicators.numeric.NumericIndicator;

public class CustomKeltnerChannelFacade {

    private final NumericIndicator upper;
    private final NumericIndicator lower;

    public CustomKeltnerChannelFacade(BarSeries series, int lengthKC, Number multKC) {

        // source = close
        NumericIndicator source = NumericIndicator.of(new ClosePriceIndicator(series));

        // ma = sma(source, lengthKC)
        NumericIndicator ma = source.sma(lengthKC);

        // range = useTrueRange ? tr : (high - low)
        NumericIndicator range = NumericIndicator.of(new TRIndicator(series));

        // rangema = sma(range, lengthKC)
        NumericIndicator rangema = range.sma(lengthKC);

        // upperKC = ma + rangema * multKC
        this.upper = ma.plus(rangema.multipliedBy(multKC));

        // lowerKC = ma - rangema * multKC
        this.lower = ma.minus(rangema.multipliedBy(multKC));
    }

    public NumericIndicator upper() {
        return this.upper;
    }

    public NumericIndicator lower() {
        return this.lower;
    }

}