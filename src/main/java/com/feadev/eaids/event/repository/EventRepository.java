package com.feadev.eaids.event.repository;

import com.feadev.eaids.event.model.Event;
import com.feadev.eaids.event.model.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID>, JpaSpecificationExecutor<Event> {

    List<Event> findByStatus(EventStatus status);

    boolean existsByName(String name);
}
