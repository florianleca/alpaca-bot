package fr.flolec.alpacabot.strategies;

import fr.flolec.alpacabot.strategies.squeezemomentum.SqueezeMomentumStrategyBuilder;
import lombok.Getter;

@Getter
public enum StrategyEnum {

    SQUEEZE_MOMENTUM(SqueezeMomentumStrategyBuilder.class);

    private final Class<?> clazz;

    StrategyEnum(Class<?> clazz) {
        this.clazz = clazz;
    }

}
