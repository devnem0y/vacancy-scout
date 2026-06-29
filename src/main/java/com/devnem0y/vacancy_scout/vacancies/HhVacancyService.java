package com.devnem0y.vacancy_scout.vacancies;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HhVacancyService {

    private final RestTemplate restTemplate;

    public HhVacancyService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public List<VacancyDto> fetchFromHh(String accessToken, VacancyFilter filter) {
        String areaId = resolveAreaId(filter.areaName());
        if (areaId == null && filter.areaName() != null && !filter.areaName().isBlank()) {

            return List.of();
        }

        UriComponentsBuilder builder = UriComponentsBuilder
                .fromUriString("https://api.hh.ru/vacancies")
                .queryParam("text", filter.text())
                .queryParam("area", areaId)
                .queryParam("schedule", mapSchedules(filter.schedules()))
                .queryParam("period", filter.period());

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        headers.set("Accept", "application/json");
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<VacanciesResponse> response = restTemplate.exchange(
                builder.build().toUri(),
                org.springframework.http.HttpMethod.GET,
                request,
                VacanciesResponse.class
        );

        if (response.getBody() == null || response.getBody().items() == null) {
            return List.of();
        }

        return response.getBody().items().stream()
                .map(this::mapToVacancyDto)
                .collect(Collectors.toList());
    }

    private String resolveAreaId(String areaName) {
        if (areaName == null || areaName.isBlank()) return null;

        System.out.println("Ищем город: " + areaName);

        var searchBuilder = UriComponentsBuilder.fromUriString("https://api.hh.ru/areas").queryParam("text", areaName);
        var areasResponse = restTemplate.getForEntity(searchBuilder.build().toUri(), AreaItem[].class);

        AreaItem[] areasArray = areasResponse.getBody();

        System.out.println("Ответ от HH (кол-во городов): " + (areasArray == null ? 0 : areasArray.length));

        if (areasArray == null || areasArray.length == 0) {
            System.out.println("Город не найден!");
            return null;
        }

        List<AreaItem> areas = Arrays.asList(areasArray);

        String id = areas.getFirst().id();
        System.out.println("Найден ID города: " + id + " (" + areas.getFirst().name() + ")");
        return id;
    }

    private List<String> mapSchedules(List<Schedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return List.of("remote", "office");
        }
        return schedules.stream()
                .map(s -> s == Schedule.REMOTE ? "remote" : "office")
                .distinct()
                .toList();
    }

    private VacancyDto mapToVacancyDto(VacancyItem item) {
        var scheduleId = item.schedule() != null ? item.schedule().id() : null;
        Schedule schedule = mapScheduleId(scheduleId);

        var city = item.area() != null ? item.area().name() : "Не указан";
        var publishedAt = parsePublishedAt(item.published_at());

        return new VacancyDto(item.id(), item.name(), publishedAt, schedule, city, item.alternate_url());
    }

    private Schedule mapScheduleId(String id) {
        if ("remote".equals(id)) return Schedule.REMOTE;
        if ("office".equals(id) || "hybrid".equals(id)) return Schedule.OFFICE;
        return Schedule.OFFICE;
    }

    private LocalDateTime parsePublishedAt(String isoString) {
        if (isoString == null) return LocalDateTime.now();
        try {
            return LocalDateTime.parse(isoString);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    public record VacanciesResponse(List<VacancyItem> items) {}

    public record VacancyItem(
            String id,
            String name,
            String published_at,
            VacancySchedule schedule,
            VacancyArea area,
            String alternate_url
    ) {}

    public record VacancySchedule(String id) {}
    public record VacancyArea(String id, String name) {}
    public record AreaItem(String id, String name) {}
}
