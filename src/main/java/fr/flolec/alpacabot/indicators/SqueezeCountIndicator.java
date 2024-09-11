package fr.flolec.alpacabot.indicators;

import org.ta4j.core.indicators.CachedIndicator;

public class SqueezeCountIndicator extends CachedIndicator<Integer> {

    private final SqueezeIndicator squeezeIndicator;

    public SqueezeCountIndicator(SqueezeIndicator squeezeIndicator) {
        super(squeezeIndicator);
        this.squeezeIndicator = squeezeIndicator;
    }

    @Override
    protected Integer calculate(int i) {
        if (squeezeIndicator.getValue(i) == null || squeezeIndicator.getValue(i)) return 0;
        return 1 + calculate(i - 1);
    }

    @Override
    public int getUnstableBars() {
        return squeezeIndicator.getUnstableBars();
    }

}
