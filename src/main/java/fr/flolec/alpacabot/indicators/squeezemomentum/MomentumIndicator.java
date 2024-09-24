package fr.flolec.alpacabot.indicators.squeezemomentum;

import fr.flolec.alpacabot.indicators.utils.AverageIndicator;
import fr.flolec.alpacabot.indicators.utils.DifferenceIndicator;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.SMAIndicator;
import org.ta4j.core.indicators.helpers.*;
import org.ta4j.core.indicators.statistics.SimpleLinearRegressionIndicator;
import org.ta4j.core.num.Num;

public class MomentumIndicator extends CachedIndicator<Num> {

    private final BarSeries series;
    private final int lengthKC;

    public MomentumIndicator(BarSeries series, int lengthKC) {
        super(series);
        this.series = series;
        this.lengthKC = lengthKC;
    }

    @Override
    protected Num calculate(int i) {

        if (i < getUnstableBars()) return null;

        // val = linreg(source  -  avg(avg(highest(high, lengthKC), lowest(low, lengthKC)), sma(close, lengthKC)), lengthKC, 0)

        // val = linreg(source  -  avg(avg(highPrice, lowPrice), sma(close, lengthKC)), lengthKC, 0)
        HighestValueIndicator highPrice = new HighestValueIndicator(new HighPriceIndicator(series), lengthKC);
        LowestValueIndicator lowPrice = new LowestValueIndicator(new LowPriceIndicator(series), lengthKC);

        // val = linreg(source  -  avg(avgHighLow, smaClose), lengthKC, 0)
        AverageIndicator avgHighLow = new AverageIndicator(highPrice, lowPrice);
        SMAIndicator smaClose = new SMAIndicator(new ClosePriceIndicator(series), lengthKC);

        // val = linreg(source  -  avgHighLowSmaClose, lengthKC, 0)
        AverageIndicator avgHighLowSmaClose = new AverageIndicator(avgHighLow, smaClose);

        // val = linreg(diff, lengthKC, 0)
        DifferenceIndicator diff = new DifferenceIndicator(new ClosePriceIndicator(series), avgHighLowSmaClose);
        SimpleLinearRegressionIndicator val = new SimpleLinearRegressionIndicator(diff, lengthKC);

        return val.getValue(i);
    }

    @Override
    public int getUnstableBars() {
        return 2 * this.lengthKC;
    }

}
