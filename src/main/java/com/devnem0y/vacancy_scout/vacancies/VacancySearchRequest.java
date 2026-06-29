package com.devnem0y.vacancy_scout.vacancies;


import java.util.List;

public record VacancySearchRequest(String city, List<Schedule> schedules, String text, Integer period) { }
