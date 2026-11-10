package ru.mirea.simulator.model;

/**
 * Дискретный отсчет телеметрии заезда (сущность Telemetry_Point).
 */
public class TelemetryPoint {
    private final int sessionId;
    private final int pointIndex;
    private final double timestampSec;
    private double coordX;
    private double coordY;
    private final double speedKmh;
    private final double steeringAngleDeg;
    private final double rawCoordX;
    private final double rawCoordY;

    public TelemetryPoint(int sessionId, int pointIndex, double timestampSec,
                          double coordX, double coordY, double speedKmh, double steeringAngleDeg) {
        this.sessionId = sessionId;
        this.pointIndex = pointIndex;
        this.timestampSec = timestampSec;
        this.coordX = coordX;
        this.coordY = coordY;
        this.speedKmh = speedKmh;
        this.steeringAngleDeg = steeringAngleDeg;
        this.rawCoordX = coordX;
        this.rawCoordY = coordY;
    }

    public TelemetryPoint(TelemetryPoint other, double smoothedX, double smoothedY) {
        this.sessionId = other.sessionId;
        this.pointIndex = other.pointIndex;
        this.timestampSec = other.timestampSec;
        this.coordX = smoothedX;
        this.coordY = smoothedY;
        this.speedKmh = other.speedKmh;
        this.steeringAngleDeg = other.steeringAngleDeg;
        this.rawCoordX = other.rawCoordX;
        this.rawCoordY = other.rawCoordY;
    }

    public int getSessionId() { return sessionId; }
    public int getPointIndex() { return pointIndex; }
    public double getTimestampSec() { return timestampSec; }
    public double getCoordX() { return coordX; }
    public double getCoordY() { return coordY; }
    public double getSpeedKmh() { return speedKmh; }
    public double getSteeringAngleDeg() { return steeringAngleDeg; }
    public double getRawCoordX() { return rawCoordX; }
    public double getRawCoordY() { return rawCoordY; }

    @Override
    public String toString() {
        return String.format("Pt#%d[t=%.1fs, (%.4f, %.4f), v=%.1f км/ч]", 
                pointIndex, timestampSec, coordX, coordY, speedKmh);
    }
}
