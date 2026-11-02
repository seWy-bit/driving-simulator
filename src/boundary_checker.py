"""
boundary_checker.py - Процедурный модуль контроля пространственных ограничений автодрома.
Реализует:
Алгоритм 2: Проверка пересечения траектории с полигональными зонами нарушений 
методом трассировки луча (Ray-Casting Algorithm / Point-in-Polygon).
Содержит вложенные циклы и ветвления.
Временная сложность: O(n * m), где:
  n - количество точек телеметрии в сессии заезда,
  m - количество контрольных зон полигонов автодрома.
Сложность по памяти: O(k), где k - количество зафиксированных нарушений.
"""


def is_point_in_polygon(px, py, polygon_vertices):
    """
    Процедура проверки попадания точки (px, py) внутрь произвольного многоугольника.
    Алгоритм выпускает горизонтальный луч из точки вправо (+X) и подсчитывает 
    число пересечений с ребрами многоугольника.
    
    Временная сложность для одного полигона из v вершин: O(v).
    """
    v_count = len(polygon_vertices)
    if v_count < 3:
        return False
        
    inside = False
    p1 = polygon_vertices[0]
    
    for idx in range(1, v_count + 1):
        p2 = polygon_vertices[idx % v_count]
        
        # Проверяем, пересекает ли горизонтальный луч y = py отрезок [p1, p2]
        # Условие: py должно лежать строго между p1.y и p2.y
        min_y = min(p1[1], p2[1])
        max_y = max(p1[1], p2[1])
        
        if min_y < py <= max_y:
            # Находим координату X точки пересечения ребра с горизонтальной прямой y = py
            dy = p2[1] - p1[1]
            if abs(dy) > 1e-9:
                x_intersection = p1[0] + (py - p1[1]) * (p2[0] - p1[0]) / dy
                if px <= x_intersection:
                    inside = not inside
                    
        p1 = p2
        
    return inside


def check_boundary_violations(filtered_points, forbidden_zones, penalty_rules):
    """
    Алгоритм 2 (Комплексный анализ нарушений траектории).
    Перебирает n точек траектории и m контрольных зон с вложенным циклом по ребрам зон.
    
    Временная сложность в худшем случае: O(n * m).
    
    :param filtered_points: список словарей точек заезда (длина n)
    :param forbidden_zones: список словарей контрольных запретных зон автодрома (длина m)
    :param penalty_rules: список словарей правил начисления штрафных баллов
    :return: список словарей зафиксированных нарушений (ассоциативные сущности)
    """
    violations_detected = []
    
    # Быстрый индекс правил по коду нарушения
    rules_by_code = {}
    for rule in penalty_rules:
        rules_by_code[rule["code"]] = rule
        
    # Множество для предотвращения повторной фиксации одного и того же непрерывного инцидента
    active_zone_incidents = set()
    
    # Внешний цикл 1: по всем точкам траектории (n итераций)
    for point_idx, pt in enumerate(filtered_points):
        px = pt["coord_x"]
        py = pt["coord_y"]
        ts = pt["timestamp_sec"]
        
        # Вложенный цикл 2: по всем контрольным зонам автодрома (m итераций)
        for zone in forbidden_zones:
            zone_id = zone["zone_id"]
            polygon = zone["polygon"]
            
            # Вложенный вызов с циклом по v вершинам полигона
            point_inside_forbidden = is_point_in_polygon(px, py, polygon)
            
            # Ветвление: анализ факта нарушения
            if point_inside_forbidden:
                if zone_id not in active_zone_incidents:
                    # Новое нарушение: фиксируем факт первого входа в запретную зону
                    rule_code = zone["rule_code"]
                    rule_info = rules_by_code.get(rule_code, {
                        "violation_type_id": 99,
                        "description": "Неидентифицированное нарушение границ",
                        "penalty_points": 1
                    })
                    
                    violation_record = {
                        "record_id": len(violations_detected) + 1,
                        "timestamp_offset": ts,
                        "point_index": point_idx,
                        "coord_x": px,
                        "coord_y": py,
                        "zone_id": zone_id,
                        "zone_name": zone["name"],
                        "violation_type_id": rule_info["violation_type_id"],
                        "rule_code": rule_code,
                        "rule_description": rule_info["description"],
                        "penalty_points": rule_info["penalty_points"],
                        "comment": f"Зафиксирован наезд на координаты ({px}, {py}) зоны {zone['name']}"
                    }
                    violations_detected.append(violation_record)
                    active_zone_incidents.add(zone_id)
            else:
                # Автомобиль покинул запретную зону: сбрасываем флаг для возможности фиксации повторного наезда
                if zone_id in active_zone_incidents:
                    active_zone_incidents.remove(zone_id)
                    
    return violations_detected
