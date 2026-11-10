package ru.mirea.simulator.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Доменный класс инструктора автошколы (сущность Instructor).
 * Включает связь 1:N с допущенными категориями (Instructor_Category).
 */
public class Instructor {
    private final int instructorId;
    private final String fullName;
    private final String licenseNumber;
    private final String phoneNumber;
    private final Set<String> categories;

    public Instructor(int instructorId, String fullName, String licenseNumber, String phoneNumber) {
        this.instructorId = instructorId;
        this.fullName = fullName;
        this.licenseNumber = licenseNumber;
        this.phoneNumber = phoneNumber;
        this.categories = new HashSet<>();
    }

    public int getInstructorId() { return instructorId; }
    public String getFullName() { return fullName; }
    public String getLicenseNumber() { return licenseNumber; }
    public String getPhoneNumber() { return phoneNumber; }
    public Set<String> getCategories() { return Collections.unmodifiableSet(categories); }

    public void addCategory(String category) {
        if (category != null && !category.trim().isEmpty()) {
            this.categories.add(category.trim().toUpperCase());
        }
    }

    public boolean hasCategory(String category) {
        return categories.contains(category != null ? category.trim().toUpperCase() : "");
    }

    @Override
    public String toString() {
        return fullName + " (уд. №" + licenseNumber + ", категории: " + categories + ")";
    }
}
