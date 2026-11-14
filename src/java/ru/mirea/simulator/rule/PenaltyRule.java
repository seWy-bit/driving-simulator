package ru.mirea.simulator.rule;

/**
 * Базовый абстрактный класс правила начисления штрафов (сущность Penalty_Rule).
 * Задаёт контракт проверки соблюдения регламента упражнения.
 */
public abstract class PenaltyRule {
    protected final int violationTypeId;
    protected final String code;
    protected final String description;
    protected final int penaltyPoints;

    public PenaltyRule(int violationTypeId, String code, String description, int penaltyPoints) {
        this.violationTypeId = violationTypeId;
        this.code = code;
        this.description = description;
        this.penaltyPoints = penaltyPoints;
    }

    public int getViolationTypeId() { return violationTypeId; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public int getPenaltyPoints() { return penaltyPoints; }

    /**
     * Полиморфный метод проверки наступления условия штрафа.
     */
    public abstract boolean isViolated(Object contextData);

    /**
     * Формирование детализированного пояснения к зафиксированному инциденту.
     */
    public abstract String formatDetails(Object contextData);

    @Override
    public String toString() {
        return "Правило " + code + " [" + violationTypeId + "] (" + penaltyPoints + " б.): " + description;
    }
}
