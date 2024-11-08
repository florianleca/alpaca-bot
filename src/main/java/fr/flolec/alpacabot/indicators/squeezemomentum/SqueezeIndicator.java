package fr.flolec.alpacabot.indicators.squeezemomentum;

import jakarta.annotation.Nullable;
import org.ta4j.core.BarSeries;
import org.ta4j.core.indicators.CachedIndicator;
import org.ta4j.core.indicators.bollinger.BollingerBandFacade;

public class SqueezeIndicator extends CachedIndicator<Boolean> {

    private final BollingerBandFacade bollingerBandFacade;
    private final CustomKeltnerChannelFacade keltnerChannelFacade;
    private final int unstableBars;


    public SqueezeIndicator(BarSeries series, int lengthBB, double multBB, int lengthKC, double multKC) {
        super(series);
        this.unstableBars = Math.max(lengthBB, lengthKC);
        bollingerBandFacade = new BollingerBandFacade(series, lengthBB, multBB);
        keltnerChannelFacade = new CustomKeltnerChannelFacade(series, lengthKC, multKC);
    }

    @Override
    @Nullable
    public Boolean calculate(int index) {
        double lowerBB = bollingerBandFacade.lower().getValue(index).doubleValue();
        double upperBB = bollingerBandFacade.upper().getValue(index).doubleValue();
        double lowerKC = keltnerChannelFacade.lower().getValue(index).doubleValue();
        double upperKC = keltnerChannelFacade.upper().getValue(index).doubleValue();

        // sqzOn  = (lowerBB > lowerKC) and (upperBB < upperKC)
        // black -> orange
        if (lowerBB > lowerKC && upperBB < upperKC) return true;

        // sqzOff = (lowerBB < lowerKC) and (upperBB > upperKC)
        // gray -> pink
        if (lowerBB < lowerKC && upperBB > upperKC) return false;

        // noSqz  = (sqzOn == false) and (sqzOff == false)
        // blue
        return null;
    }

    @Override
    public int getUnstableBars() {
        return unstableBars;
    }

}
