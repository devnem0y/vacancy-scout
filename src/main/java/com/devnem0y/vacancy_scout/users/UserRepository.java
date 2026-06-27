package com.devnem0y.vacancy_scout.users;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByHhUserId(String hhUserId);
}
