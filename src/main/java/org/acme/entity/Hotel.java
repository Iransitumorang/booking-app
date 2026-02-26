package org.acme.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "hotel")
public class Hotel extends UuidPanacheEntity {

    public String name;

    public String location;
}