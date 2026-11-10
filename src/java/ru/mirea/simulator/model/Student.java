package ru.mirea.simulator.model;

/**
 * Доменный класс курсанта автошколы (сущность Student).
 */
public class Student {
    private final int studentId;
    private final String fullName;
    private final String passportData;
    private final String category;
    private final String phoneNumber;
    private final String medicalCertNumber;

    public Student(int studentId, String fullName, String passportData, 
                   String category, String phoneNumber, String medicalCertNumber) {
        this.studentId = studentId;
        this.fullName = fullName;
        this.passportData = passportData;
        this.category = category;
        this.phoneNumber = phoneNumber;
        this.medicalCertNumber = medicalCertNumber;
    }

    public int getStudentId() { return studentId; }
    public String getFullName() { return fullName; }
    public String getPassportData() { return passportData; }
    public String getCategory() { return category; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getMedicalCertNumber() { return medicalCertNumber; }

    @Override
    public String toString() {
        return fullName + " (категория " + category + ", ID: " + studentId + ")";
    }
}
