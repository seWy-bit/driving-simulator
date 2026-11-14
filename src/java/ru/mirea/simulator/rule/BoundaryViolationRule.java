package ru.mirea.simulator.rule;

/**
 * Правило штрафа за пересечение геометрических границ и разметки автодрома.
 * Наследует PenaltyRule и переопределяет логику пространственной проверки.
 */
public class BoundaryViolationRule extends PenaltyRule {

    public BoundaryViolationRule(int violationTypeId, String code, String description, int penaltyPoints) {
        super(violationTypeId, code, description, penaltyPoints);
    }

    @Override
    public boolean isViolated(Object contextData) {
        if (contextData instanceof Boolean) {
            return (Boolean) contextData;
        }
        return false;
    }

    @Override
    public String formatDetails(Object contextData) {
        return "Зафиксирован наезд на контрольные границы разметки: " + description;
    }
}
