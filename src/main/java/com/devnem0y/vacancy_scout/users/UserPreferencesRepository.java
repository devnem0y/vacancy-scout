package com.devnem0y.vacancy_scout.users;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserPreferencesRepository extends JpaRepository<UserPreferences, Long> {
    Optional<UserPreferences> findByUser_HhUserId(String hhUserId);
}
