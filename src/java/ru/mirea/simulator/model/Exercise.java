package ru.mirea.simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Практическое контрольное упражнение автодрома (сущность Exercise).
 */
public class Exercise {
    private final int exerciseId;
    private final String title;
    private final int maxDurationSec;
    private final int maxPenaltyPoints;
    private final List<ReferenceNode> referenceLine;
    private final List<ForbiddenZone> forbiddenZones;

    public Exercise(int exerciseId, String title, int maxDurationSec, int maxPenaltyPoints) {
        this.exerciseId = exerciseId;
        this.title = title;
        this.maxDurationSec = maxDurationSec;
        this.maxPenaltyPoints = maxPenaltyPoints;
        this.referenceLine = new ArrayList<>();
        this.forbiddenZones = new ArrayList<>();
    }

    public int getExerciseId() { return exerciseId; }
    public String getTitle() { return title; }
    public int getMaxDurationSec() { return maxDurationSec; }
    public int getMaxPenaltyPoints() { return maxPenaltyPoints; }
    public List<ReferenceNode> getReferenceLine() { return Collections.unmodifiableList(referenceLine); }
    public List<ForbiddenZone> getForbiddenZones() { return Collections.unmodifiableList(forbiddenZones); }

    public void addReferenceNode(double x, double y) {
        referenceLine.add(new ReferenceNode(this.exerciseId, referenceLine.size(), x, y));
    }

    public void addForbiddenZone(ForbiddenZone zone) {
        if (zone != null) {
            forbiddenZones.add(zone);
        }
    }

    @Override
    public String toString() {
        return title + " (макс. штрафов: " + maxPenaltyPoints + ")";
    }
}
