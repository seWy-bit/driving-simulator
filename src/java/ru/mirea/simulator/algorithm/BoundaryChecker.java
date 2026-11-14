package ru.mirea.simulator.algorithm;

import ru.mirea.simulator.model.ForbiddenZone;
import ru.mirea.simulator.model.SessionViolation;
import ru.mirea.simulator.model.TelemetryPoint;
import ru.mirea.simulator.model.ZoneVertex;
import ru.mirea.simulator.rule.PenaltyRule;

import java.util.*;

/**
 * Алгоритм 2: Детектирование пересечения границ упражнения методом трассировки луча (Ray-Casting).
 * Временная сложность: O(n * m * v), где n - точки, m - зоны, v - среднее число вершин.
 */
public class BoundaryChecker {

    public boolean isPointInPolygon(double px, double py, List<ZoneVertex> vertices) {
        int vCount = vertices.size();
        if (vCount < 3) {
            return false;
        }

        boolean inside = false;
        ZoneVertex p1 = vertices.get(0);

        for (int idx = 1; idx <= vCount; idx++) {
            ZoneVertex p2 = vertices.get(idx % vCount);

            double minY = Math.min(p1.getCoordY(), p2.getCoordY());
            double maxY = Math.max(p1.getCoordY(), p2.getCoordY());

            if (minY < py && py <= maxY) {
                double dy = p2.getCoordY() - p1.getCoordY();
                if (Math.abs(dy) > 1e-9) {
                    double xIntersection = p1.getCoordX() + (py - p1.getCoordY()) * (p2.getCoordX() - p1.getCoordX()) / dy;
                    if (px <= xIntersection) {
                        inside = !inside;
                    }
                }
            }
            p1 = p2;
        }

        return inside;
    }

    public List<SessionViolation> checkViolations(int sessionId, List<TelemetryPoint> filteredPoints,
                                                 List<ForbiddenZone> forbiddenZones,
                                                 Map<String, PenaltyRule> rulesByCode) {
        List<SessionViolation> violations = new ArrayList<>();
        Set<String> activeZoneIncidents = new HashSet<>();

        for (int pointIdx = 0; pointIdx < filteredPoints.size(); pointIdx++) {
            TelemetryPoint pt = filteredPoints.get(pointIdx);
            double px = pt.getCoordX();
            double py = pt.getCoordY();
            double ts = pt.getTimestampSec();

            for (ForbiddenZone zone : forbiddenZones) {
                String zoneId = zone.getZoneId();
                boolean pointInside = isPointInPolygon(px, py, zone.getVertices());

                if (pointInside) {
                    if (!activeZoneIncidents.contains(zoneId)) {
                        String ruleCode = zone.getRuleCode();
                        PenaltyRule rule = rulesByCode.get(ruleCode);

                        int vTypeId = (rule != null) ? rule.getViolationTypeId() : 99;
                        String desc = (rule != null) ? rule.getDescription() : "Неидентифицированное нарушение границ";
                        int points = (rule != null) ? rule.getPenaltyPoints() : 1;

                        String comment = String.format(Locale.US, "Зафиксирован наезд на координаты (%.4f, %.4f) зоны %s", 
                                px, py, zone.getName());

                        SessionViolation v = new SessionViolation(
                                sessionId,
                                violations.size() + 1,
                                vTypeId,
                                ruleCode,
                                desc,
                                points,
                                ts,
                                px,
                                py,
                                zoneId,
                                zone.getName(),
                                comment
                        );
                        violations.add(v);
                        activeZoneIncidents.add(zoneId);
                    }
                } else {
                    activeZoneIncidents.remove(zoneId);
                }
            }
        }

        return violations;
    }
}
