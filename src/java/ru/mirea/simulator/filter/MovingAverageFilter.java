package ru.mirea.simulator.filter;

import ru.mirea.simulator.model.TelemetryPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация алгоритма центрированного скользящего среднего с усечением на границах.
 * Временная сложность: O(n * w) = O(n) при фиксированном окне w.
 */
public class MovingAverageFilter implements TelemetryFilter {

    @Override
    public List<TelemetryPoint> filter(List<TelemetryPoint> rawPoints, int windowSize) {
        List<TelemetryPoint> filtered = new ArrayList<>();
        int totalCount = rawPoints.size();
        if (totalCount == 0) {
            return filtered;
        }

        int halfW = windowSize / 2;

        for (int i = 0; i < totalCount; i++) {
            int startIdx = Math.max(0, i - halfW);
            int endIdx = Math.min(totalCount, i + halfW + 1);
            int countInWindow = endIdx - startIdx;

            double sumX = 0.0;
            double sumY = 0.0;

            for (int j = startIdx; j < endIdx; j++) {
                sumX += rawPoints.get(j).getCoordX();
                sumY += rawPoints.get(j).getCoordY();
            }

            double avgX = Math.round((sumX / countInWindow) * 10000.0) / 10000.0;
            double avgY = Math.round((sumY / countInWindow) * 10000.0) / 10000.0;

            filtered.add(new TelemetryPoint(rawPoints.get(i), avgX, avgY));
        }

        return filtered;
    }
}
