package fr.flolec.alpacabot.indicators.utils;

import org.ta4j.core.Indicator;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

public class AverageIndicator extends CachedIndicator<Num> {

    private final Indicator<Num> indicator1;
    private final Indicator<Num> indicator2;

    public AverageIndicator(Indicator<Num> indicator1, Indicator<Num> indicator2) {
        super(indicator1);
        this.indicator1 = indicator1;
        this.indicator2 = indicator2;
    }

    @Override
    protected Num calculate(int index) {
        return indicator1.getValue(index).plus(indicator2.getValue(index)).dividedBy(DecimalNum.valueOf(2));
    }

    @Override
    public int getUnstableBars() {
        return Math.max(indicator1.getUnstableBars(), indicator2.getUnstableBars());
    }

}
