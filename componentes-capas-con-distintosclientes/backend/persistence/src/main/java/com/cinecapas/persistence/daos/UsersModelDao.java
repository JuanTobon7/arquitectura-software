package com.cinecapas.persistence.daos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name = "users")
@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class UsersModelDao {
    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    private String email;
    private String claveHash;
}
