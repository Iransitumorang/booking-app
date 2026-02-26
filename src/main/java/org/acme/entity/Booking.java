package org.acme.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "booking")
public class Booking extends UuidPanacheEntity {

    @ManyToOne
    public Room room;

    public String customerName;

    public LocalDate checkInDate;

    public LocalDate checkOutDate;

    public String status; // BOOKED / CANCELLED
}