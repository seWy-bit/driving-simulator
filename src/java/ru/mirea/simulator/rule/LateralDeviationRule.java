package ru.mirea.simulator.rule;

import java.util.Locale;

/**
 * Правило штрафа за нестабильное удержание коридора движения (превышение SDLP).
 * Наследует PenaltyRule и выполняет пороговый статистический контроль.
 */
public class LateralDeviationRule extends PenaltyRule {
    private final double maxAllowedSdlp;

    public LateralDeviationRule(int violationTypeId, String code, String description, 
                                int penaltyPoints, double maxAllowedSdlp) {
        super(violationTypeId, code, description, penaltyPoints);
        this.maxAllowedSdlp = maxAllowedSdlp;
    }

    public double getMaxAllowedSdlp() { return maxAllowedSdlp; }

    @Override
    public boolean isViolated(Object contextData) {
        if (contextData instanceof Number) {
            double sdlpValue = ((Number) contextData).doubleValue();
            return sdlpValue > maxAllowedSdlp;
        }
        return false;
    }

    @Override
    public String formatDetails(Object contextData) {
        double sdlpValue = (contextData instanceof Number) ? ((Number) contextData).doubleValue() : 0.0;
        return String.format(Locale.US, "Превышение порога стабильности полосы (SDLP=%.4f м > %.2f м)", 
                sdlpValue, maxAllowedSdlp);
    }
}
