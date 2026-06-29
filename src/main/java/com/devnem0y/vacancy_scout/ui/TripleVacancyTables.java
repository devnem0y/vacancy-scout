package com.devnem0y.vacancy_scout.ui;

import com.devnem0y.vacancy_scout.vacancies.VacancyDto;
import com.devnem0y.vacancy_scout.vacancies.VacancySearchResponse;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import org.springframework.stereotype.Component;

@Component
public class TripleVacancyTables extends VerticalLayout {

    private final Grid<VacancyDto> gridMyCityOffice;
    private final Grid<VacancyDto> gridMyCityRemote;
    private final Grid<VacancyDto> gridOtherCitiesRemote;

    public TripleVacancyTables() {
        gridMyCityOffice = createGrid("Мой город: офис");
        gridMyCityRemote = createGrid("Мой город: удалённо");
        gridOtherCitiesRemote = createGrid("Другие города: удалённо");

        setPadding(true);
        setSpacing(true);
        add(gridMyCityOffice, gridMyCityRemote, gridOtherCitiesRemote);
    }

    private Grid<VacancyDto> createGrid(String caption) {
        Grid<VacancyDto> g = new Grid<>();
        //g.setCaption(caption);
        g.setAriaLabel(caption);
        g.addColumn(VacancyDto::title).setHeader("Вакансия");
        g.addColumn(VacancyDto::publishedAt).setHeader("Дата");
        g.addColumn(VacancyDto::schedule).setHeader("Формат");
        g.addColumn(VacancyDto::areaName).setHeader("Город");

        // Ссылка в новой вкладке
        g.addComponentColumn(v -> {
            Anchor a = new Anchor(v.url(), "Ссылка");
            a.setTarget("_blank");
            return a;
        }).setHeader("Ссылка");

        g.setSizeFull();
        return g;
    }

    public void setData(VacancySearchResponse response) {
        gridMyCityOffice.setItems(response.myCityOffice());
        gridMyCityRemote.setItems(response.myCityRemote());
        gridOtherCitiesRemote.setItems(response.otherCitiesRemote());
    }
}
