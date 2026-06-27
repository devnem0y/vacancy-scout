package com.devnem0y.vacancy_scout.users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {

    @Id
    @Column(name = "hh_user_id", length = 50)
    private String hhUserId; // Уникальный ID пользователя из HH API

    public User(String hhUserId) {
        this.hhUserId = hhUserId;
    }

    // Сюда можно позже добавить createdAt, если понадобится
}
