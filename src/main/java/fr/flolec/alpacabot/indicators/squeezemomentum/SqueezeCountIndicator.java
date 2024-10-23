package fr.flolec.alpacabot.indicators.squeezemomentum;

import org.ta4j.core.indicators.CachedIndicator;

public class SqueezeCountIndicator extends CachedIndicator<Integer> {

    private final SqueezeIndicator squeezeIndicator;

    public SqueezeCountIndicator(SqueezeIndicator squeezeIndicator) {
        super(squeezeIndicator);
        this.squeezeIndicator = squeezeIndicator;
    }

    @Override
    protected Integer calculate(int i) {
        if (i < getUnstableBars()) return null;
        if (squeezeIndicator.getValue(i) == null || !squeezeIndicator.getValue(i)) return 0;
        Integer previous = calculate(i - 1);
        if (previous == null) return null;
        return previous + 1;
    }

    @Override
    public int getUnstableBars() {
        return squeezeIndicator.getUnstableBars();
    }
    // TODO: en vrai, est-ce que ce ne serait pas jusqu'au premier sqOn post squeezeIndicator.getUnstableBars() ?

}
