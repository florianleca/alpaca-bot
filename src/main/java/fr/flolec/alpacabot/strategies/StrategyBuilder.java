package fr.flolec.alpacabot.strategies;

import org.ta4j.core.BarSeries;
import org.ta4j.core.Strategy;

public interface StrategyBuilder {

    Strategy buildStrategy(BarSeries series);

}
