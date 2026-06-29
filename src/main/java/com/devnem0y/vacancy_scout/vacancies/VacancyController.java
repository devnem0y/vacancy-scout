package com.devnem0y.vacancy_scout.vacancies;

import com.devnem0y.vacancy_scout.users.UserPreferencesService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;

@Controller
public class VacancyController {

    private final HhVacancyService hhVacancyService;
    private final UserPreferencesService userPreferencesService;

    public VacancyController(HhVacancyService hhVacancyService, UserPreferencesService userPreferencesService) {
        this.hhVacancyService = hhVacancyService;
        this.userPreferencesService = userPreferencesService;
    }

    @PostMapping("/api/vacancies/search")
    public ResponseEntity<VacancySearchResponse> search(@RequestBody VacancySearchRequest request) {
        var attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) return ResponseEntity.status(401).build();

        HttpSession session = attributes.getRequest().getSession(false);
        if (session == null) return ResponseEntity.status(401).build();

        String hhUserId = (String) session.getAttribute("hh_user_id");
        String accessToken = (String) session.getAttribute("hh_access_token");

        if (hhUserId == null || accessToken == null) return ResponseEntity.status(401).build();

        var prefs = userPreferencesService.findOrCreate(hhUserId);
        var city = request.city() != null && !request.city().isBlank() ? request.city() : prefs.getCity();
        var filter = new VacancyFilter(city, request.schedules(), request.text(), request.period());
        List<VacancyDto> rawList = hhVacancyService.fetchFromHh(accessToken, filter);

        var groupingService = new VacancyGroupingService();
        VacancySearchResponse response = groupingService.groupByCityAndSchedule(rawList, city);

        return ResponseEntity.ok(response);
    }
}