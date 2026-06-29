package com.devnem0y.vacancy_scout.vacancies;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class VacancyGroupingService {

    public VacancySearchResponse groupByCityAndSchedule(List<VacancyDto> rawList, String preferredCity) {
        List<VacancyDto> myCityOffice = new ArrayList<>();
        List<VacancyDto> myCityRemote = new ArrayList<>();
        List<VacancyDto> otherCitiesRemote = new ArrayList<>();

        for (VacancyDto v : rawList) {
            boolean isMyCity = v.areaName().equalsIgnoreCase(preferredCity);

            if (isMyCity) {
                if (v.schedule() == Schedule.OFFICE) myCityOffice.add(v);
                else if (v.schedule() == Schedule.REMOTE) myCityRemote.add(v);
            } else {
                // Не мой город → только REMOTE. OFFICE из других городов не показываем.
                if (v.schedule() == Schedule.REMOTE) otherCitiesRemote.add(v);
            }
        }

        return new VacancySearchResponse(myCityOffice, myCityRemote, otherCitiesRemote);
    }
}
