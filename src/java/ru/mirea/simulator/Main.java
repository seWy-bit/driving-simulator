package ru.mirea.simulator;

import ru.mirea.simulator.algorithm.BoundaryChecker;
import ru.mirea.simulator.algorithm.SdlpCalculator;
import ru.mirea.simulator.filter.MovingAverageFilter;
import ru.mirea.simulator.filter.TelemetryFilter;
import ru.mirea.simulator.model.*;
import ru.mirea.simulator.rule.BoundaryViolationRule;
import ru.mirea.simulator.rule.LateralDeviationRule;
import ru.mirea.simulator.rule.PenaltyRule;
import ru.mirea.simulator.service.SessionEvaluator;
import ru.mirea.simulator.util.SimpleJsonParser;

import java.io.File;
import java.nio.file.Files;
import java.util.*;

/**
 * Главный исполняемый класс контрольного этапа КТ3.
 * Запускается из консоли командой: java ru.mirea.simulator.Main [путь_к_файлу.json]
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("Запуск объектно-ориентированного симулятора телеметрии (Java 21, КТ3)...");

        String jsonPath = "test_data.json";
        if (args.length > 0 && args[0] != null && !args[0].trim().isEmpty()) {
            jsonPath = args[0].trim();
        }

        File file = new File(jsonPath);
        if (!file.exists()) {
            // Список возможных путей относительно корня репозитория или текущего каталога
            String[] searchPaths = new String[] {
                    "src/java/" + jsonPath,
                    "src/" + jsonPath,
                    "data/" + jsonPath,
                    "../" + jsonPath,
                    "../src/java/" + jsonPath,
                    "../src/" + jsonPath,
                    "../../" + jsonPath,
                    "../../src/java/" + jsonPath
            };

            for (String candidate : searchPaths) {
                File candFile = new File(candidate);
                if (candFile.exists()) {
                    file = candFile;
                    break;
                }
            }

            // Если не найден в файловой системе, проверяем каталог скомпилированных классов (classpath / out)
            if (!file.exists()) {
                try {
                    File codeDir = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                    if (codeDir.isFile()) {
                        codeDir = codeDir.getParentFile();
                    }
                    File inCodeDir = new File(codeDir, jsonPath);
                    if (inCodeDir.exists()) {
                        file = inCodeDir;
                    } else if (codeDir.getParentFile() != null) {
                        File inParent = new File(codeDir.getParentFile(), jsonPath);
                        if (inParent.exists()) {
                            file = inParent;
                        } else {
                            File inProjectSrc = new File(codeDir.getParentFile(), "src/java/" + jsonPath);
                            if (inProjectSrc.exists()) {
                                file = inProjectSrc;
                            }
                        }
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (!file.exists()) {
            System.err.println("Ошибка: файл набора данных не найден: " + jsonPath);
            System.exit(1);
        }

        try {
            System.out.println("Загрузка данных из: " + file.getAbsolutePath() + "...");
            String content = Files.readString(file.toPath());
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) SimpleJsonParser.parse(content);

            // 1. Десериализация курсанта
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> studentsRaw = (List<Map<String, Object>>) data.get("students");
            Map<String, Object> sMap = studentsRaw.get(0);
            Student student = new Student(
                    ((Number) sMap.get("student_id")).intValue(),
                    (String) sMap.get("full_name"),
                    (String) sMap.get("passport_data"),
                    (String) sMap.get("category"),
                    (String) sMap.get("phone_number"),
                    (String) sMap.get("medical_cert_number")
            );

            // 2. Десериализация инструктора
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> instRaw = (List<Map<String, Object>>) data.get("instructors");
            Map<String, Object> iMap = instRaw.get(0);
            Instructor instructor = new Instructor(
                    ((Number) iMap.get("instructor_id")).intValue(),
                    (String) iMap.get("full_name"),
                    (String) iMap.get("license_number"),
                    (String) iMap.get("phone_number")
            );
            instructor.addCategory(student.getCategory());

            // 3. Десериализация правил
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> rulesRaw = (List<Map<String, Object>>) data.get("penalty_rules");
            List<PenaltyRule> penaltyRules = new ArrayList<>();
            for (Map<String, Object> rMap : rulesRaw) {
                int id = ((Number) rMap.get("violation_type_id")).intValue();
                String code = (String) rMap.get("code");
                String desc = (String) rMap.get("description");
                int pts = ((Number) rMap.get("penalty_points")).intValue();

                if ("2.1".equals(code)) {
                    penaltyRules.add(new LateralDeviationRule(id, code, desc, pts, 0.40));
                } else {
                    penaltyRules.add(new BoundaryViolationRule(id, code, desc, pts));
                }
            }

            // 4. Десериализация упражнения
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> exRaw = (List<Map<String, Object>>) data.get("exercises");
            Map<String, Object> eMap = exRaw.get(0);
            Exercise exercise = new Exercise(
                    ((Number) eMap.get("exercise_id")).intValue(),
                    (String) eMap.get("title"),
                    ((Number) eMap.get("max_duration_sec")).intValue(),
                    ((Number) eMap.get("max_penalty_points")).intValue()
            );

            @SuppressWarnings("unchecked")
            List<List<Number>> refCenterline = (List<List<Number>>) eMap.get("reference_centerline");
            for (List<Number> pt : refCenterline) {
                exercise.addReferenceNode(pt.get(0).doubleValue(), pt.get(1).doubleValue());
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> zonesRaw = (List<Map<String, Object>>) eMap.get("forbidden_zones");
            for (Map<String, Object> zMap : zonesRaw) {
                ForbiddenZone fz = new ForbiddenZone(
                        (String) zMap.get("zone_id"),
                        exercise.getExerciseId(),
                        (String) zMap.get("rule_code"),
                        (String) zMap.get("name")
                );
                @SuppressWarnings("unchecked")
                List<List<Number>> poly = (List<List<Number>>) zMap.get("polygon");
                for (List<Number> vertex : poly) {
                    fz.addVertex(vertex.get(0).doubleValue(), vertex.get(1).doubleValue());
                }
                exercise.addForbiddenZone(fz);
            }

            // 5. Десериализация сеанса
            @SuppressWarnings("unchecked")
            Map<String, Object> sessMap = (Map<String, Object>) data.get("driving_session");
            DrivingSession session = new DrivingSession(
                    ((Number) sessMap.get("session_id")).intValue(),
                    student,
                    instructor,
                    exercise,
                    (String) sessMap.get("start_time"),
                    (String) sessMap.get("vehicle_reg_number")
            );

            // 6. Десериализация точек телеметрии
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> telemRaw = (List<Map<String, Object>>) data.get("raw_telemetry");
            int ptSeq = 0;
            for (Map<String, Object> tMap : telemRaw) {
                Number pIdxNum = (Number) tMap.get("point_index");
                int ptIdx = (pIdxNum != null) ? pIdxNum.intValue() : ptSeq++;
                session.addRawPoint(new TelemetryPoint(
                        session.getSessionId(),
                        ptIdx,
                        ((Number) tMap.get("timestamp_sec")).doubleValue(),
                        ((Number) tMap.get("coord_x")).doubleValue(),
                        ((Number) tMap.get("coord_y")).doubleValue(),
                        ((Number) tMap.get("speed_kmh")).doubleValue(),
                        ((Number) tMap.get("steering_angle_deg")).doubleValue()
                ));
            }
            System.out.printf("Успешно загружено %d точек телеметрии.%n", session.getRawTelemetry().size());

            // 7. Оценка заезда через сервисный слой
            TelemetryFilter filter = new MovingAverageFilter();
            SdlpCalculator sdlpCalc = new SdlpCalculator();
            BoundaryChecker boundaryChecker = new BoundaryChecker();
            SessionEvaluator evaluator = new SessionEvaluator(filter, sdlpCalc, boundaryChecker, penaltyRules);

            SessionSummary summary = evaluator.evaluate(session, 3, 0.40);

            // 8. Вывод протокола
            displayConsoleReport(summary);

        } catch (Exception ex) {
            System.err.println("Исключение при выполнении симуляции: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private static void displayConsoleReport(SessionSummary summary) {
        System.out.println("================================================================================");
        System.out.println("========== ЭКЗАМЕНАЦИОННЫЙ ПРОТОКОЛ КОНТРОЛЬНОГО ЗАЕЗДА (JAVA 21) ==============");
        System.out.println("================================================================================");
        System.out.printf("Сессия №:               %d%n", summary.getSessionId());
        System.out.printf("Курсант:                %s%n", summary.getStudentName());
        System.out.printf("Инструктор:             %s%n", summary.getInstructorName());
        System.out.printf("Учебный автомобиль:     %s%n", summary.getVehicleRegNumber());
        System.out.printf("Выполняемое упражнение: %s%n", summary.getExerciseTitle());
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("РАСЧЁТНЫЕ ПОКАЗАТЕЛИ КИНЕМАТИКИ И ТЕЛЕМЕТРИИ (АЛГОРИТМ 1):");
        System.out.printf(Locale.US, "  * Стандартное отклонение от оси (SDLP): %.4f м%n", summary.getSdlpMetric());
        System.out.printf(Locale.US, "  * Среднее боковое смещение:             %.4f м%n", summary.getMeanDeviation());
        System.out.printf(Locale.US, "  * Максимальный вылет от оси полосы:     %.4f м%n", summary.getMaxDeviation());
        System.out.println("--------------------------------------------------------------------------------");

        System.out.println("РЕЗУЛЬТАТЫ ДЕТЕКТИРОВАНИЯ ЗОН НАРУШЕНИЙ (АЛГОРИТМ 2, RAY-CASTING):");
        if (summary.getViolations().isEmpty()) {
            System.out.println("  Нарушений границ упражнения и правил разметки не зафиксировано.");
        } else {
            System.out.printf("%-3s | %-8s | %-4s | %-5s | %-50s%n", "№", "Время, с", "Код", "Баллы", "Описание нарушения");
            System.out.println("--------------------------------------------------------------------------------");
            int idx = 1;
            for (SessionViolation v : summary.getViolations()) {
                String tsStr = (v.getTimestampOffset() != null) ? String.format(Locale.US, "%.1f", v.getTimestampOffset()) : "итог";
                String desc = v.getRuleDescription();
                if (desc.length() > 48) desc = desc.substring(0, 48);
                System.out.printf("%-3d | %-8s | %-4s | %-5d | %-50s%n", 
                        idx++, tsStr, v.getRuleCode(), v.getPenaltyPoints(), desc);
                if (v.getComment() != null) {
                    System.out.println("    -> Детали: " + v.getComment());
                }
            }
        }

        System.out.println("================================================================================");
        System.out.println("ИТОГОВЫЙ РЕЗУЛЬТАТ ЗАЕЗДА:");
        System.out.printf("  Начислено штрафных баллов: %d (допустимо: < 5)%n", summary.getTotalPenaltyPoints());
        System.out.printf("  РЕШЕНИЕ СИСТЕМЫ:           [%s]%n", summary.getStatusString());
        System.out.println("================================================================================");
    }
}
