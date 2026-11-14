package ru.mirea.simulator.algorithm;

import ru.mirea.simulator.model.ReferenceNode;
import ru.mirea.simulator.model.TelemetryPoint;
import java.util.ArrayList;
import java.util.List;

/**
 * Алгоритм 1.2: Расчёт среднеквадратического бокового отклонения SDLP
 * (Standard Deviation of Lateral Position) и вспомогательный поиск сегментов оси.
 * Временная сложность: O(n * r) = O(n) при фиксированном числе сегментов оси r.
 */
public class SdlpCalculator {

    public static class SdlpResult {
        private final double sdlpValue;
        private final double meanDeviation;
        private final double maxDeviation;
        private final List<Double> deviations;

        public SdlpResult(double sdlpValue, double meanDeviation, double maxDeviation, List<Double> deviations) {
            this.sdlpValue = sdlpValue;
            this.meanDeviation = meanDeviation;
            this.maxDeviation = maxDeviation;
            this.deviations = deviations;
        }

        public double getSdlpValue() { return sdlpValue; }
        public double getMeanDeviation() { return meanDeviation; }
        public double getMaxDeviation() { return maxDeviation; }
        public List<Double> getDeviations() { return deviations; }
    }

    public double getReferenceLateralOffset(double pointX, double pointY, List<ReferenceNode> referenceLine) {
        if (referenceLine == null || referenceLine.size() < 2) {
            return 0.0;
        }

        ReferenceNode pStart = referenceLine.get(0);
        ReferenceNode pEnd = referenceLine.get(1);

        for (int i = 0; i < referenceLine.size() - 1; i++) {
            ReferenceNode p1 = referenceLine.get(i);
            ReferenceNode p2 = referenceLine.get(i + 1);
            if (p1.getCoordX() <= pointX && pointX <= p2.getCoordX()) {
                pStart = p1;
                pEnd = p2;
                break;
            }
        }

        double dx = pEnd.getCoordX() - pStart.getCoordX();
        double dy = pEnd.getCoordY() - pStart.getCoordY();

        if (Math.abs(dx) < 1e-6) {
            return pointX - pStart.getCoordX();
        }

        double slope = dy / dx;
        double targetY = pStart.getCoordY() + slope * (pointX - pStart.getCoordX());
        return pointY - targetY;
    }

    public SdlpResult calculate(List<TelemetryPoint> filteredPoints, List<ReferenceNode> referenceLine) {
        int n = filteredPoints.size();
        if (n == 0) {
            return new SdlpResult(0.0, 0.0, 0.0, new ArrayList<>());
        }

        List<Double> deviations = new ArrayList<>();
        double sumDev = 0.0;
        double maxDev = 0.0;

        for (TelemetryPoint pt : filteredPoints) {
            double dev = getReferenceLateralOffset(pt.getCoordX(), pt.getCoordY(), referenceLine);
            deviations.add(dev);
            sumDev += dev;

            double absDev = Math.abs(dev);
            if (absDev > maxDev) {
                maxDev = absDev;
            }
        }

        double meanDev = sumDev / n;
        double varianceSum = 0.0;
        for (double dev : deviations) {
            double diff = dev - meanDev;
            varianceSum += diff * diff;
        }

        double variance = varianceSum / n;
        double sdlpValue = Math.sqrt(variance);

        double roundedSdlp = Math.round(sdlpValue * 10000.0) / 10000.0;
        double roundedMean = Math.round(meanDev * 10000.0) / 10000.0;
        double roundedMax = Math.round(maxDev * 10000.0) / 10000.0;

        return new SdlpResult(roundedSdlp, roundedMean, roundedMax, deviations);
    }
}
