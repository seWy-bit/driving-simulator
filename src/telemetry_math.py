"""
telemetry_math.py - Процедурный модуль математической обработки телеметрии автосимулятора.
Реализует:
1. Алгоритм скользящего среднего для сглаживания координат (O(n)).
2. Алгоритм вычисления метрики бокового отклонения SDLP (O(n)).
Строго процедурный стиль: чистые функции, базовые структуры dict/list/tuple, отсутствие классов.
"""

import math


def filter_coordinates(raw_points, window_size=3):
    """
    Алгоритм 1.1: Сглаживание траектории методом скользящего окна.
    Временная сложность: O(n) в худшем случае (один проход по точкам).
    Сложность по памяти: O(n) для хранения отфильтрованного списка точек.
    
    :param raw_points: список словарей с сырыми замерами [{'coord_x': ..., 'coord_y': ...}]
    :param window_size: ширина окна сглаживания (нечетное целое число >= 1)
    :return: список словарей с отфильтрованными координатами
    """
    total_count = len(raw_points)
    if total_count == 0:
        return []
    
    half_w = window_size // 2
    filtered_points = []
    
    for i in range(total_count):
        start_idx = max(0, i - half_w)
        end_idx = min(total_count, i + half_w + 1)
        count_in_window = end_idx - start_idx
        
        sum_x = 0.0
        sum_y = 0.0
        for j in range(start_idx, end_idx):
            sum_x += raw_points[j]["coord_x"]
            sum_y += raw_points[j]["coord_y"]
            
        avg_x = round(sum_x / count_in_window, 4)
        avg_y = round(sum_y / count_in_window, 4)
        
        # Сохраняем исходные поля с обновленными координатами
        smoothed_point = dict(raw_points[i])
        smoothed_point["coord_x"] = avg_x
        smoothed_point["coord_y"] = avg_y
        smoothed_point["raw_coord_x"] = raw_points[i]["coord_x"]
        smoothed_point["raw_coord_y"] = raw_points[i]["coord_y"]
        filtered_points.append(smoothed_point)
        
    return filtered_points


def get_reference_lateral_offset(point_x, point_y, reference_line):
    """
    Вычисляет отклонение точки (x, y) по нормали к кусочно-линейной осевой траектории.
    Для горизонтальных участков оси autodrome (y = const) отклонение равно (point_y - line_y).
    :param point_x: координата X точки
    :param point_y: координата Y точки
    :param reference_line: список узлов [[x0, y0], [x1, y1], ...]
    :return: знаковое боковое смещение (в метрах)
    """
    if len(reference_line) < 2:
        return 0.0
        
    # Поиск ближайшего сегмента опорной траектории по оси X
    chosen_segment = (reference_line[0], reference_line[1])
    for seg_idx in range(len(reference_line) - 1):
        p1 = reference_line[seg_idx]
        p2 = reference_line[seg_idx + 1]
        if p1[0] <= point_x <= p2[0]:
            chosen_segment = (p1, p2)
            break
            
    p_start, p_end = chosen_segment
    dx = p_end[0] - p_start[0]
    dy = p_end[1] - p_start[1]
    
    if abs(dx) < 1e-6:
        # Вертикальный сегмент
        return point_x - p_start[0]
        
    # Линейная интерполяция высоты целевой оси для координаты X
    slope = dy / dx
    target_y = p_start[1] + slope * (point_x - p_start[0])
    return point_y - target_y


def calculate_sdlp(filtered_points, reference_line):
    """
    Алгоритм 1.2: Вычисление дисперсии бокового положения (SDLP - Standard Deviation of Lateral Position).
    Временная сложность: O(n) (один проход для расчета отклонений и средних значений).
    Сложность по памяти: O(n) для массива отклонений (или O(1) при расчете на лету).
    
    Формула:
    mean_dev = (1 / n) * sum(d_i)
    SDLP = sqrt((1 / n) * sum((d_i - mean_dev)^2))
    
    :param filtered_points: список отфильтрованных точек траектории
    :param reference_line: опорная осевая линия коридора упражнения
    :return: кортеж (sdlp_value, mean_deviation, max_deviation, deviations_list)
    """
    n = len(filtered_points)
    if n == 0:
        return 0.0, 0.0, 0.0, []
        
    deviations = []
    sum_dev = 0.0
    max_dev = 0.0
    
    for pt in filtered_points:
        dev = get_reference_lateral_offset(pt["coord_x"], pt["coord_y"], reference_line)
        deviations.append(dev)
        sum_dev += dev
        if abs(dev) > max_dev:
            max_dev = abs(dev)
            
    mean_dev = sum_dev / n
    
    variance_sum = 0.0
    for dev in deviations:
        diff = dev - mean_dev
        variance_sum += diff * diff
        
    variance = variance_sum / n
    sdlp_value = math.sqrt(variance)
    
    return round(sdlp_value, 4), round(mean_dev, 4), round(max_dev, 4), deviations
