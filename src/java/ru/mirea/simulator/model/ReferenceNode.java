package ru.mirea.simulator.model;

/**
 * Узел осевой линии упражнения (сущность Reference_Node).
 */
public class ReferenceNode {
    private final int exerciseId;
    private final int nodeIndex;
    private final double coordX;
    private final double coordY;

    public ReferenceNode(int exerciseId, int nodeIndex, double coordX, double coordY) {
        this.exerciseId = exerciseId;
        this.nodeIndex = nodeIndex;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public int getExerciseId() { return exerciseId; }
    public int getNodeIndex() { return nodeIndex; }
    public double getCoordX() { return coordX; }
    public double getCoordY() { return coordY; }

    @Override
    public String toString() {
        return String.format("[%d: (%.2f, %.2f)]", nodeIndex, coordX, coordY);
    }
}
