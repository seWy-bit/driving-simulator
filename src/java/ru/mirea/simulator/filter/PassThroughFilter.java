package ru.mirea.simulator.filter;

import ru.mirea.simulator.model.TelemetryPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация сквозного фильтра без сглаживания (Pass-Through).
 * Используется для калибровки и сравнительного анализа сырых данных с отфильтрованными.
 */
public class PassThroughFilter implements TelemetryFilter {

    @Override
    public List<TelemetryPoint> filter(List<TelemetryPoint> rawPoints, int windowSize) {
        List<TelemetryPoint> copy = new ArrayList<>();
        for (TelemetryPoint pt : rawPoints) {
            copy.add(new TelemetryPoint(pt, pt.getRawCoordX(), pt.getRawCoordY()));
        }
        return copy;
    }
}
