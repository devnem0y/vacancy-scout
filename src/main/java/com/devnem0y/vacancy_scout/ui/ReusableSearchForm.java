package com.devnem0y.vacancy_scout.ui;

import com.devnem0y.vacancy_scout.vacancies.Schedule;
import com.devnem0y.vacancy_scout.vacancies.VacancySearchRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Consumer;

@Component
public class ReusableSearchForm extends VerticalLayout {

    private final TextField cityField;
    private final TextField keywordsField;
    private final Button searchButton;

    public ReusableSearchForm() {
        cityField = new TextField("Ваш город");
        keywordsField = new TextField("Ключевые слова (название вакансии)");
        searchButton = new Button("Найти");

        setPadding(true);
        setSpacing(true);
        add(cityField, keywordsField, searchButton);
    }

    public void setInitialValues(String city, String keywords) {
        cityField.setValue(city);
        keywordsField.setValue(keywords);
    }

    public VacancySearchRequest buildRequest() {
        var schedules = List.of(Schedule.OFFICE, Schedule.REMOTE);
        return new VacancySearchRequest(
                cityField.getValue(),
                schedules,
                keywordsField.getValue(),
                30 // daysBack по умолчанию
        );
    }

    public void setOnSearch(Consumer<VacancySearchRequest> listener) {
        searchButton.addClickListener(e -> listener.accept(buildRequest()));
    }
}
