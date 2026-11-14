package ru.mirea.simulator.filter;

import ru.mirea.simulator.model.TelemetryPoint;
import java.util.List;

/**
 * Интерфейс предварительной фильтрации зашумленного потока телеметрии.
 * Реализует автоматизируемую операцию №1 целевого бизнес-процесса.
 */
public interface TelemetryFilter {
    /**
     * Выполняет фильтрацию и сглаживание координат траектории.
     * @param rawPoints исходный список сырых точек телеметрии
     * @param windowSize размер окна сглаживания
     * @return отфильтрованный список точек телеметрии
     */
    List<TelemetryPoint> filter(List<TelemetryPoint> rawPoints, int windowSize);
}
