package ru.mirea.simulator.model;

import java.util.List;

/**
 * Итоговый агрегат результатов заезда для экзаменационного протокола.
 */
public class SessionSummary {
    private final int sessionId;
    private final String studentName;
    private final String instructorName;
    private final String vehicleRegNumber;
    private final String exerciseTitle;
    private final double sdlpMetric;
    private final double meanDeviation;
    private final double maxDeviation;
    private final int totalPenaltyPoints;
    private final boolean isPassed;
    private final List<SessionViolation> violations;

    public SessionSummary(int sessionId, String studentName, String instructorName, 
                          String vehicleRegNumber, String exerciseTitle, 
                          double sdlpMetric, double meanDeviation, double maxDeviation, 
                          int totalPenaltyPoints, boolean isPassed, List<SessionViolation> violations) {
        this.sessionId = sessionId;
        this.studentName = studentName;
        this.instructorName = instructorName;
        this.vehicleRegNumber = vehicleRegNumber;
        this.exerciseTitle = exerciseTitle;
        this.sdlpMetric = sdlpMetric;
        this.meanDeviation = meanDeviation;
        this.maxDeviation = maxDeviation;
        this.totalPenaltyPoints = totalPenaltyPoints;
        this.isPassed = isPassed;
        this.violations = violations;
    }

    public int getSessionId() { return sessionId; }
    public String getStudentName() { return studentName; }
    public String getInstructorName() { return instructorName; }
    public String getVehicleRegNumber() { return vehicleRegNumber; }
    public String getExerciseTitle() { return exerciseTitle; }
    public double getSdlpMetric() { return sdlpMetric; }
    public double getMeanDeviation() { return meanDeviation; }
    public double getMaxDeviation() { return maxDeviation; }
    public int getTotalPenaltyPoints() { return totalPenaltyPoints; }
    public boolean isPassed() { return isPassed; }
    public List<SessionViolation> getViolations() { return violations; }
    public String getStatusString() {
        return isPassed ? "СДАНО (ЗАЧЁТ)" : "НЕ СДАНО (ПЕРЕСДАЧА)";
    }
}
