package fr.flolec.alpacabot.indicators.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import org.ta4j.core.Indicator;
import org.ta4j.core.num.DecimalNum;
import org.ta4j.core.num.Num;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AverageIndicatorTest {

    @Mock
    private Indicator<Num> indicator1;

    @Mock
    private Indicator<Num> indicator2;

    @Mock
    private final BarSeries barSeries = new BaseBarSeries();

    private AverageIndicator averageIndicator;

    @BeforeEach
    void setUp() {
        when(indicator1.getBarSeries()).thenReturn(barSeries);
        when(barSeries.getMaximumBarCount()).thenReturn(1);
        averageIndicator = new AverageIndicator(indicator1, indicator2);
    }

    @Test
    void calculate() {
        when(indicator1.getValue(0)).thenReturn(DecimalNum.valueOf(10));
        when(indicator2.getValue(0)).thenReturn(DecimalNum.valueOf(6));
        assertEquals(8, averageIndicator.getValue(0).doubleValue());
    }

    @Test
    void getUnstableBars() {
        when(indicator1.getUnstableBars()).thenReturn(5);
        when(indicator2.getUnstableBars()).thenReturn(2);
        assertEquals(5, averageIndicator.getUnstableBars());
    }

}