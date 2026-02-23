package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@Entity
public class Room extends PanacheEntity {

    public String roomNumber;

    public String type;

    public double price;

    @ManyToOne
    public Hotel hotel;
}