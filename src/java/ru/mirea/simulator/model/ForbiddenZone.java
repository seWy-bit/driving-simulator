package ru.mirea.simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Контрольная запретная зона автодрома (сущность Forbidden_Zone).
 */
public class ForbiddenZone {
    private final String zoneId;
    private final int exerciseId;
    private final String ruleCode;
    private final String name;
    private final List<ZoneVertex> vertices;

    public ForbiddenZone(String zoneId, int exerciseId, String ruleCode, String name) {
        this.zoneId = zoneId;
        this.exerciseId = exerciseId;
        this.ruleCode = ruleCode;
        this.name = name;
        this.vertices = new ArrayList<>();
    }

    public String getZoneId() { return zoneId; }
    public int getExerciseId() { return exerciseId; }
    public String getRuleCode() { return ruleCode; }
    public String getName() { return name; }
    public List<ZoneVertex> getVertices() { return Collections.unmodifiableList(vertices); }

    public void addVertex(double x, double y) {
        vertices.add(new ZoneVertex(this.zoneId, vertices.size(), x, y));
    }

    @Override
    public String toString() {
        return name + " [" + zoneId + ", код: " + ruleCode + ", вершин: " + vertices.size() + "]";
    }
}
