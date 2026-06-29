package com.devnem0y.vacancy_scout.vacancies;

import java.time.LocalDateTime;

public record VacancyDto(String id, String title, LocalDateTime publishedAt,
                         Schedule schedule, String areaName, String url) { }
