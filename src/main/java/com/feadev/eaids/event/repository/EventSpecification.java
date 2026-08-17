package com.feadev.eaids.event.repository;

import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.enums.EventStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class EventSpecification {

    private EventSpecification() {
    }

    public static Specification<Event> withFilters(EventStatus status, String eventType,
                                                   String country, String search,
                                                   LocalDate startDateFrom, LocalDate startDateTo) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (eventType != null && !eventType.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("eventType")), eventType.toLowerCase()));
            }

            if (country != null && !country.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("country")), country.toLowerCase()));
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
                Predicate locationLike = cb.like(cb.lower(root.get("location")), pattern);
                Predicate descriptionLike = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(nameLike, locationLike, descriptionLike));
            }

            if (startDateFrom != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("startDate"), startDateFrom));
            }

            if (startDateTo != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("startDate"), startDateTo));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
