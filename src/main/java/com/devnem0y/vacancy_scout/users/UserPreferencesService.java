package com.devnem0y.vacancy_scout.users;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserPreferencesService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;

    public UserPreferencesService(UserRepository userRepository, UserPreferencesRepository userPreferencesRepository) {
        this.userRepository = userRepository;
        this.userPreferencesRepository = userPreferencesRepository;
    }

    @Transactional
    public UserPreferences findOrCreate(String hhUserId) {
        // 1. Ищем или создаём User (по hh_user_id)
        User user = userRepository.findByHhUserId(hhUserId)
                .orElseGet(() -> {
                    User newUser = new User(hhUserId);
                    return userRepository.save(newUser);
                });

        // 2. Ищем UserPreferences по связи с User
        return userPreferencesRepository.findByUser_HhUserId(hhUserId)
                .orElseGet(() -> {
                    UserPreferences prefs = new UserPreferences(user, "Челябинск", "");
                    return userPreferencesRepository.save(prefs);
                });
    }
}
