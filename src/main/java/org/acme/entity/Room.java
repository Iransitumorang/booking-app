package org.acme.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "room")
public class Room extends UuidPanacheEntity {

    public String roomNumber;

    public String type;

    public double price;

    @ManyToOne
    public Hotel hotel;
}