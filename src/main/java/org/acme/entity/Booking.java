package org.acme.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;

@Entity
public class Booking extends PanacheEntity {

    @ManyToOne
    public Room room;

    public String customerName;

    public LocalDate checkInDate;

    public LocalDate checkOutDate;

    public String status; // BOOKED / CANCELLED
}