package ru.mirea.simulator.model;

/**
 * Зафиксированное нарушение правил в ходе контрольного заезда
 * (ассоциативная сущность Session_Violation, разрешающая связь M:N).
 */
public class SessionViolation {
    private final int sessionId;
    private final int recordId;
    private final int violationTypeId;
    private final String ruleCode;
    private final String ruleDescription;
    private final int penaltyPoints;
    private final Double timestampOffset;
    private final Double coordX;
    private final Double coordY;
    private final String zoneId;
    private final String zoneName;
    private final String comment;

    public SessionViolation(int sessionId, int recordId, int violationTypeId,
                            String ruleCode, String ruleDescription, int penaltyPoints,
                            Double timestampOffset, Double coordX, Double coordY,
                            String zoneId, String zoneName, String comment) {
        this.sessionId = sessionId;
        this.recordId = recordId;
        this.violationTypeId = violationTypeId;
        this.ruleCode = ruleCode;
        this.ruleDescription = ruleDescription;
        this.penaltyPoints = penaltyPoints;
        this.timestampOffset = timestampOffset;
        this.coordX = coordX;
        this.coordY = coordY;
        this.zoneId = zoneId;
        this.zoneName = zoneName;
        this.comment = comment;
    }

    public int getSessionId() { return sessionId; }
    public int getRecordId() { return recordId; }
    public int getViolationTypeId() { return violationTypeId; }
    public String getRuleCode() { return ruleCode; }
    public String getRuleDescription() { return ruleDescription; }
    public int getPenaltyPoints() { return penaltyPoints; }
    public Double getTimestampOffset() { return timestampOffset; }
    public Double getCoordX() { return coordX; }
    public Double getCoordY() { return coordY; }
    public String getZoneId() { return zoneId; }
    public String getZoneName() { return zoneName; }
    public String getComment() { return comment; }

    @Override
    public String toString() {
        String tStr = timestampOffset != null ? String.format("%.1fs", timestampOffset) : "итог";
        return String.format("#%d [%s] Код %s (%d б.): %s", 
                recordId, tStr, ruleCode, penaltyPoints, ruleDescription);
    }
}
