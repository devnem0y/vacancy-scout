package com.devnem0y.vacancy_scout.ui;

import com.devnem0y.vacancy_scout.users.UserPreferencesService;
import com.devnem0y.vacancy_scout.vacancies.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Route("dashboard")
public class DashboardScreen extends VerticalLayout {

    private static final Logger log = LoggerFactory.getLogger(DashboardScreen.class);

    private final UserPreferencesService userPreferencesService;
    private final HhVacancyService hhVacancyService;
    private final VacancyGroupingService groupingService;

    private TextField cityField;
    private TextField keywordsField;
    private Button searchButton;

    private Grid<VacancyDto> officeTable;
    private Grid<VacancyDto> remoteMyCityTable;
    private Grid<VacancyDto> remoteOtherCityTable;

    public DashboardScreen(UserPreferencesService userPreferencesService,
                           HhVacancyService hhVacancyService, VacancyGroupingService groupingService) {
        this.userPreferencesService = userPreferencesService;
        this.hhVacancyService = hhVacancyService;
        this.groupingService = groupingService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        initLayout();
        loadPreferences();
    }

    private void initLayout() {
        cityField = new TextField("Город");
        cityField.setPlaceholder("Например: Челябинск");

        keywordsField = new TextField("Ключевые слова");
        keywordsField.setPlaceholder("Java, Spring, Vaadin");

        searchButton = new Button("Найти");
        searchButton.addClickListener(_ -> performSearch());
        searchButton.addClassName("search-button");

        VerticalLayout formLayout = new VerticalLayout(cityField, keywordsField, searchButton);
        formLayout.setMargin(false);

        officeTable = createVacancyGrid("Офис (свой город)");
        remoteMyCityTable = createVacancyGrid("Удалённо (свой город)");
        remoteOtherCityTable = createVacancyGrid("Удалённо (другие города)");

        add(new com.vaadin.flow.component.html.H2("Поиск вакансий"), formLayout);
        add(officeTable, remoteMyCityTable, remoteOtherCityTable);
    }

    private Grid<VacancyDto> createVacancyGrid(String caption) {
        Grid<VacancyDto> grid = new Grid<>();
        grid.setAriaLabel(caption);
        grid.addColumn(VacancyDto::title).setHeader("Вакансия");
        grid.addColumn(VacancyDto::areaName).setHeader("Город");
        grid.addColumn(VacancyDto::publishedAt).setHeader("Дата");
        grid.addComponentColumn(v -> {
            var link = new Anchor(v.url(), "Ссылка");
            link.setTarget("_blank");
            return link;
        }).setHeader("Ссылка");
        return grid;
    }

    private void loadPreferences() {
        var attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return;
        var session = attrs.getRequest().getSession(false);
        if (session == null) return;

        String hhUserId = (String) session.getAttribute("hh_user_id");
        if (hhUserId == null) return;

        var prefs = userPreferencesService.findOrCreate(hhUserId);
        cityField.setValue(prefs.getCity());
        keywordsField.setValue(prefs.getKeywords());
    }

    private void performSearch() {
        String city = cityField.getValue();
        String keywords = keywordsField.getValue();

        var request = new VacancySearchRequest(
                city,
                List.of(Schedule.OFFICE, Schedule.REMOTE),
                keywords,
                30
        );

        UI.getCurrent().access(() -> Notification.show("Начинаю поиск вакансий..."));

        String accessToken;
        try {
            var attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            var session = attrs.getRequest().getSession(false);
            if (session == null) {
                throw new IllegalStateException("Сессия не найдена — возможно, истекла");
            }
            accessToken = (String) session.getAttribute("hh_access_token");
            if (accessToken == null || accessToken.isBlank()) {
                throw new IllegalStateException("Токен HH отсутствует в сессии. Авторизуйтесь заново.");
            }
        } catch (Exception e) {
            log.error("Ошибка получения контекста запроса или токена: {}", e.getMessage(), e);
            UI.getCurrent().access(() ->
                    Notification.show("Ошибка: " + e.getMessage())
            );
            return;
        }

        String cityToSearch = (city != null) ? city.trim() : "";
        log.info("[performSearch] Контекст получен. Город: '{}', токен: {}",
                cityToSearch,
                (accessToken.length() > 10 ? "***" + accessToken.substring(accessToken.length() - 8) : "скрыт"));

        CompletableFuture.runAsync(() -> {
            try {
                log.info("[performSearch-async] Начинаем запрос к HH API...");

                VacancyFilter filter = new VacancyFilter(
                        cityToSearch,
                        request.schedules(),
                        request.text(),
                        request.period()
                );

                List<VacancyDto> rawList = hhVacancyService.fetchFromHh(accessToken, filter);

                log.info("[performSearch-async] Получено сырых вакансий: {}", rawList.size());

                VacancyGroupingService groupingService = new VacancyGroupingService();
                VacancySearchResponse resp = groupingService.groupByCityAndSchedule(rawList, cityToSearch);

                int officeCount = resp.myCityOffice().size();
                int remoteMyCityCount = resp.myCityRemote().size();
                int remoteOtherCityCount = resp.otherCitiesRemote().size();

                log.info("[performSearch-async] Сгруппировано: Офис={}, Удал.свой={}, Удал.др={}",
                        officeCount, remoteMyCityCount, remoteOtherCityCount);

                UI.getCurrent().access(() -> {
                    officeTable.setItems(resp.myCityOffice());
                    remoteMyCityTable.setItems(resp.myCityRemote());
                    remoteOtherCityTable.setItems(resp.otherCitiesRemote());

                    long total = officeCount + remoteMyCityCount + remoteOtherCityCount;
                    if (total == 0) {
                        Notification.show(
                                "По запросу ничего не найдено. Проверьте название города или ключевые слова.");
                    } else {
                        Notification.show("Готово! Найдено вакансий: " + total);
                    }
                });

            } catch (Exception e) {
                log.error("[performSearch-async] Критическая ошибка при выполнении поиска: {}", e.getMessage(), e);

                UI.getCurrent().access(() ->
                        Notification.show("Ошибка поиска: " + (e.getMessage() != null ? e.getMessage() : "Неизвестная ошибка"))
                );
            }
        });
    }
}