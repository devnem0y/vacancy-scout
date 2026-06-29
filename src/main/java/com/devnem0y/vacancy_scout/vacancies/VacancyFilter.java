package com.devnem0y.vacancy_scout.vacancies;

import java.util.List;

public record VacancyFilter(String areaName, List<Schedule> schedules, String text, Integer period) { }
