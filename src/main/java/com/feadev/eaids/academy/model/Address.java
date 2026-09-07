package com.feadev.eaids.academy.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Reusable JPA embeddable representing a physical address.
 * Columns are prefixed with {@code addr_} so an owning entity can embed it
 * without column-name collisions.
 */
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "addr_country", length = 100)
    private String country;

    @Column(name = "addr_state", length = 100)
    private String state;

    @Column(name = "addr_lga", length = 100)
    private String lgaOrProvince;

    @Column(name = "addr_street", length = 255)
    private String streetAddress;
}
