"""
main.py - Главный исполняемый модуль процедурной реализации (КТ2, ИКБО-31).
Тема практики: «Разработка симуляторов вождения».
Язык: Python 3.13.2.
Стиль: строго процедурный (свободные функции, структуры dict, list, tuple).

Запуск программы:
    python main.py
или:
    python main.py test_data.json
"""

import sys
import os
import json
from telemetry_math import filter_coordinates, calculate_sdlp
from boundary_checker import check_boundary_violations


def load_dataset(file_path):
    """
    Загружает исходные данные учебного заезда из JSON-файла.
    Возвращает словарь со всеми сущностями предметной области.
    """
    if not os.path.exists(file_path):
        raise FileNotFoundError(f"Файл данных '{file_path}' не найден.")
        
    with open(file_path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data


def format_table_header(title, width=80):
    """Вспомогательная процедура форматирования разделителей консоли."""
    print("=" * width)
    print(f" {title.upper()} ".center(width, "="))
    print("=" * width)


def evaluate_session_results(session_info, student, instructor, exercise, 
                             sdlp_result, violations, max_allowed_penalty=5):
    """
    Процедура агрегации результатов заезда, подсчета баллов и определения итоговой оценки.
    :return: итоговый словарь с результатами протокола
    """
    sdlp_val, mean_dev, max_dev, _ = sdlp_result
    
    total_penalty = 0
    for v in violations:
        total_penalty += v["penalty_points"]
        
    # Проверка превышения норматива SDLP (> 0.40 м)
    sdlp_threshold = 0.40
    if sdlp_val > sdlp_threshold:
        sdlp_violation = {
            "record_id": len(violations) + 1,
            "timestamp_offset": None,
            "point_index": None,
            "coord_x": None,
            "coord_y": None,
            "zone_id": "SDLP-LIM",
            "zone_name": "Норматив бокового рыскания",
            "violation_type_id": 3,
            "rule_code": "2.1",
            "rule_description": f"Превышение порога стабильности полосы (SDLP={sdlp_val} м > {sdlp_threshold} м)",
            "penalty_points": 2,
            "comment": "Нестабильное руление, повышенная амплитуда боковых колебаний автомобиля"
        }
        violations.append(sdlp_violation)
        total_penalty += 2
        
    is_passed = total_penalty < max_allowed_penalty
    status_str = "СДАНО (ЗАЧЁТ)" if is_passed else "НЕ СДАНО (ПЕРЕСДАЧА)"
    
    summary = {
        "session_id": session_info["session_id"],
        "student_name": student["full_name"],
        "instructor_name": instructor["full_name"],
        "exercise_title": exercise["title"],
        "car_number": session_info["vehicle_reg_number"],
        "sdlp_metric_m": sdlp_val,
        "mean_deviation_m": mean_dev,
        "max_deviation_m": max_dev,
        "violations_count": len(violations),
        "total_penalty_points": total_penalty,
        "status": status_str,
        "is_passed": is_passed,
        "violations_list": violations
    }
    return summary


def display_console_report(report):
    """Процедура вывода подробного структурированного экзаменационного протокола в консоль."""
    format_table_header("Протокол автоматизированного контроля заезда на автодроме")
    
    print(f"Сессия №:               {report['session_id']}")
    print(f"Курсант:                {report['student_name']}")
    print(f"Инструктор:             {report['instructor_name']}")
    print(f"Учебный автомобиль:     {report['car_number']}")
    print(f"Выполняемое упражнение: {report['exercise_title']}")
    print("-" * 80)
    
    print("РАСЧЁТНЫЕ ПОКАЗАТЕЛИ КИНЕМАТИКИ И ТЕЛЕМЕТРИИ (АЛГОРИТМ 1):")
    print(f"  * Стандартное отклонение от оси (SDLP): {report['sdlp_metric_m']:.4f} м")
    print(f"  * Среднее боковое смещение:             {report['mean_deviation_m']:.4f} м")
    print(f"  * Максимальный вылет от оси полосы:     {report['max_deviation_m']:.4f} м")
    print("-" * 80)
    
    print(f"РЕЗУЛЬТАТЫ ДЕТЕКТИРОВАНИЯ ЗОН НАРУШЕНИЙ (АЛГОРИТМ 2, RAY-CASTING):")
    if not report["violations_list"]:
        print("  Нарушений границ упражнения и правил разметки не зафиксировано.")
    else:
        print(f"{'№':<3} | {'Время, с':<8} | {'Код':<4} | {'Баллы':<5} | {'Описание нарушения':<50}")
        print("-" * 80)
        for idx, item in enumerate(report["violations_list"], 1):
            ts_str = f"{item['timestamp_offset']:.1f}" if item['timestamp_offset'] is not None else "итог"
            desc = item['rule_description'][:48]
            print(f"{idx:<3} | {ts_str:<8} | {item['rule_code']:<4} | {item['penalty_points']:<5} | {desc:<50}")
            if item.get("comment"):
                print(f"    -> Детали: {item['comment']}")
                
    print("=" * 80)
    print(f"ИТОГОВЫЙ РЕЗУЛЬТАТ ЗАЕЗДА:")
    print(f"  Начислено штрафных баллов: {report['total_penalty_points']} (допустимо: < 5)")
    print(f"  РЕШЕНИЕ СИСТЕМЫ:           [{report['status']}]")
    print("=" * 80)


def main():
    """Точка входа в программу."""
    default_data_file = os.path.join(os.path.dirname(__file__), "test_data.json")
    if len(sys.argv) > 1:
        data_file = sys.argv[1]
    else:
        data_file = default_data_file
        
    print(f"Загрузка конфигурации и телеметрии из: {data_file}...")
    dataset = load_dataset(data_file)
    
    student = dataset["students"][0]
    instructor = dataset["instructors"][0]
    exercise = dataset["exercises"][0]
    session_info = dataset["driving_session"]
    raw_points = dataset["raw_telemetry"]
    forbidden_zones = exercise["forbidden_zones"]
    penalty_rules = dataset["penalty_rules"]
    ref_line = exercise["reference_centerline"]
    
    print(f"Обработка {len(raw_points)} точек сырой телеметрии...")
    
    # 1. Шаг автоматизации 1: сглаживание координат (Алгоритм 1.1)
    filtered_points = filter_coordinates(raw_points, window_size=3)
    
    # 2. Шаг автоматизации 2: расчёт SDLP (Алгоритм 1.2)
    sdlp_result = calculate_sdlp(filtered_points, ref_line)
    
    # 3. Шаг автоматизации 3: детектирование нарушений границ зон (Алгоритм 2)
    detected_violations = check_boundary_violations(filtered_points, forbidden_zones, penalty_rules)
    
    # 4. Шаг автоматизации 4: итоговый протокол и оценка
    report = evaluate_session_results(
        session_info, student, instructor, exercise, sdlp_result, detected_violations,
        max_allowed_penalty=exercise["max_penalty_points"]
    )
    
    # 5. Вывод в консоль
    display_console_report(report)


if __name__ == "__main__":
    main()
