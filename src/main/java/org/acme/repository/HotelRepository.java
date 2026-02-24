package org.acme.repository;

import io.quarkus.panache.common.Page;
import org.acme.entity.Hotel;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class HotelRepository implements PanacheRepository<Hotel> {

    public List<Hotel> findAll(Page page) {
        return findAll().page(page).list();
    }
}