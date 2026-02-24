package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User extends PanacheEntity {

    @Column(unique = true)
    public String username;

    public String passwordHash;

    public String name;

    public String role;

    public static User findByUsername(String username) {
        return find("username", username).firstResult();
    }
}
