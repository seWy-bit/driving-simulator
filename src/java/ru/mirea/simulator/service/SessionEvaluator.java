package ru.mirea.simulator.service;

import ru.mirea.simulator.algorithm.BoundaryChecker;
import ru.mirea.simulator.algorithm.SdlpCalculator;
import ru.mirea.simulator.filter.TelemetryFilter;
import ru.mirea.simulator.model.*;
import ru.mirea.simulator.rule.LateralDeviationRule;
import ru.mirea.simulator.rule.PenaltyRule;

import java.util.*;

/**
 * Сервисный класс автоматизированной обработки и оценки контрольного заезда.
 * Реализует сквозной процесс формирования электронного протокола.
 */
public class SessionEvaluator {
    private final TelemetryFilter filter;
    private final SdlpCalculator sdlpCalculator;
    private final BoundaryChecker boundaryChecker;
    private final Map<String, PenaltyRule> rulesMap;

    public SessionEvaluator(TelemetryFilter filter, SdlpCalculator sdlpCalculator, 
                            BoundaryChecker boundaryChecker, List<PenaltyRule> rules) {
        this.filter = filter;
        this.sdlpCalculator = sdlpCalculator;
        this.boundaryChecker = boundaryChecker;
        this.rulesMap = new HashMap<>();
        for (PenaltyRule r : rules) {
            this.rulesMap.put(r.getCode(), r);
        }
    }

    public SessionSummary evaluate(DrivingSession session, int filterWindowSize, double maxAllowedSdlp) {
        // 1. Фильтрация телеметрии
        List<TelemetryPoint> filtered = filter.filter(session.getRawTelemetry(), filterWindowSize);
        session.setFilteredTelemetry(filtered);

        // 2. Расчет SDLP
        SdlpCalculator.SdlpResult sdlpResult = sdlpCalculator.calculate(filtered, session.getExercise().getReferenceLine());

        // 3. Проверка границ упражнения (Ray-Casting)
        List<SessionViolation> boundaryViolations = boundaryChecker.checkViolations(
                session.getSessionId(),
                filtered,
                session.getExercise().getForbiddenZones(),
                rulesMap
        );

        List<SessionViolation> allViolations = new ArrayList<>(boundaryViolations);
        int totalPenalty = 0;
        for (SessionViolation v : boundaryViolations) {
            totalPenalty += v.getPenaltyPoints();
            session.addViolation(v);
        }

        // 4. Проверка правила бокового отклонения SDLP
        PenaltyRule sdlpRule = rulesMap.get("2.1");
        if (sdlpRule == null) {
            sdlpRule = new LateralDeviationRule(3, "2.1", 
                    "Превышение порога стабильности полосы", 2, maxAllowedSdlp);
        }

        if (sdlpRule.isViolated(sdlpResult.getSdlpValue())) {
            SessionViolation sdlpViolation = new SessionViolation(
                    session.getSessionId(),
                    allViolations.size() + 1,
                    sdlpRule.getViolationTypeId(),
                    sdlpRule.getCode(),
                    sdlpRule.formatDetails(sdlpResult.getSdlpValue()),
                    sdlpRule.getPenaltyPoints(),
                    null,
                    null,
                    null,
                    "SDLP-LIM",
                    "Норматив бокового рыскания",
                    "Нестабильное руление, повышенная амплитуда боковых колебаний автомобиля"
            );
            allViolations.add(sdlpViolation);
            totalPenalty += sdlpRule.getPenaltyPoints();
            session.addViolation(sdlpViolation);
        }

        boolean isPassed = totalPenalty < session.getExercise().getMaxPenaltyPoints();

        return new SessionSummary(
                session.getSessionId(),
                session.getStudent().getFullName(),
                session.getInstructor().getFullName(),
                session.getVehicleRegNumber(),
                session.getExercise().getTitle(),
                sdlpResult.getSdlpValue(),
                sdlpResult.getMeanDeviation(),
                sdlpResult.getMaxDeviation(),
                totalPenalty,
                isPassed,
                allViolations
        );
    }
}
