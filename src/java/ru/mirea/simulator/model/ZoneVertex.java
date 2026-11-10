package ru.mirea.simulator.model;

/**
 * Вершина замкнутого полигона запретной зоны (сущность Zone_Vertex).
 */
public class ZoneVertex {
    private final String zoneId;
    private final int vertexIndex;
    private final double coordX;
    private final double coordY;

    public ZoneVertex(String zoneId, int vertexIndex, double coordX, double coordY) {
        this.zoneId = zoneId;
        this.vertexIndex = vertexIndex;
        this.coordX = coordX;
        this.coordY = coordY;
    }

    public String getZoneId() { return zoneId; }
    public int getVertexIndex() { return vertexIndex; }
    public double getCoordX() { return coordX; }
    public double getCoordY() { return coordY; }

    @Override
    public String toString() {
        return String.format("V%d(%.2f, %.2f)", vertexIndex, coordX, coordY);
    }
}
