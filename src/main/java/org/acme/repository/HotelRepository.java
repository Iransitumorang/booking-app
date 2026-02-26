package org.acme.repository;

import io.quarkus.panache.common.Page;
import org.acme.entity.Hotel;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class HotelRepository implements PanacheRepositoryBase<Hotel, UUID> {

    public List<Hotel> findAll(Page page) {
        return findAll().page(page).list();
    }
}