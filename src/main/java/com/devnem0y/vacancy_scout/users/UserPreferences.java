package com.devnem0y.vacancy_scout.users;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
public class UserPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hh_user_id", referencedColumnName = "hh_user_id")
    private User user;

    @Column(length = 100)
    private String city = "Челябинск"; // Значение по умолчанию

    @Column(length = 255)
    private String keywords = ""; // Пустые ключевые слова по умолчанию

    public UserPreferences(User user, String city, String keywords) {
        this.user = user;
        this.city = city;
        this.keywords = keywords;
    }
}
