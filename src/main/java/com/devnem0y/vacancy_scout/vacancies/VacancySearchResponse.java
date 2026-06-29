package com.devnem0y.vacancy_scout.vacancies;

import java.util.List;

public record VacancySearchResponse(List<VacancyDto> myCityOffice, List<VacancyDto> myCityRemote, List<VacancyDto> otherCitiesRemote) { }
