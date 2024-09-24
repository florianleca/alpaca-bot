package fr.flolec.alpacabot.indicators.squeezemomentum;

import jakarta.annotation.Nullable;
import org.ta4j.core.indicators.CachedIndicator;

public class SqueezeReleaseIndicator extends CachedIndicator<Boolean> {

    private final SqueezeCountIndicator squeezeCountIndicator;

    private final int minSqueezeCount;

    public SqueezeReleaseIndicator(SqueezeCountIndicator squeezeCountIndicator, int minSqueezeCount) {
        super(squeezeCountIndicator);
        this.squeezeCountIndicator = squeezeCountIndicator;
        this.minSqueezeCount = minSqueezeCount;
    }

    @Override
    @Nullable
    protected Boolean calculate(int i) {
        if (i < getUnstableBars()) return false;
        Integer squeezeCount = squeezeCountIndicator.getValue(i);
        Integer previousSqueezeCount = squeezeCountIndicator.getValue(i - 1);
        if (squeezeCount == null || previousSqueezeCount == null) return false;
        return squeezeCount == 0 && previousSqueezeCount >= minSqueezeCount;
    }

    @Override
    public int getUnstableBars() {
        return squeezeCountIndicator.getUnstableBars();
    }

}
