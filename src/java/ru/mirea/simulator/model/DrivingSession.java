package ru.mirea.simulator.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Контрольный практический заезд (сущность Driving_Session).
 */
public class DrivingSession {
    private final int sessionId;
    private final Student student;
    private final Instructor instructor;
    private final Exercise exercise;
    private final String startTime;
    private final String vehicleRegNumber;
    private final List<TelemetryPoint> rawTelemetry;
    private final List<TelemetryPoint> filteredTelemetry;
    private final List<SessionViolation> violations;

    public DrivingSession(int sessionId, Student student, Instructor instructor, 
                          Exercise exercise, String startTime, String vehicleRegNumber) {
        this.sessionId = sessionId;
        this.student = student;
        this.instructor = instructor;
        this.exercise = exercise;
        this.startTime = startTime;
        this.vehicleRegNumber = vehicleRegNumber;
        this.rawTelemetry = new ArrayList<>();
        this.filteredTelemetry = new ArrayList<>();
        this.violations = new ArrayList<>();
    }

    public int getSessionId() { return sessionId; }
    public Student getStudent() { return student; }
    public Instructor getInstructor() { return instructor; }
    public Exercise getExercise() { return exercise; }
    public String getStartTime() { return startTime; }
    public String getVehicleRegNumber() { return vehicleRegNumber; }
    public List<TelemetryPoint> getRawTelemetry() { return Collections.unmodifiableList(rawTelemetry); }
    public List<TelemetryPoint> getFilteredTelemetry() { return Collections.unmodifiableList(filteredTelemetry); }
    public List<SessionViolation> getViolations() { return Collections.unmodifiableList(violations); }

    public void addRawPoint(TelemetryPoint pt) {
        if (pt != null) rawTelemetry.add(pt);
    }

    public void setFilteredTelemetry(List<TelemetryPoint> points) {
        this.filteredTelemetry.clear();
        if (points != null) this.filteredTelemetry.addAll(points);
    }

    public void addViolation(SessionViolation v) {
        if (v != null) violations.add(v);
    }

    public int calculateTotalPenalty() {
        int sum = 0;
        for (SessionViolation v : violations) {
            sum += v.getPenaltyPoints();
        }
        return sum;
    }
}
