package com.feadev.eaids.academy.dto.response;

import com.feadev.eaids.academy.dto.request.AddressDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * API representation of an individual academy application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndividualApplicationResponse {

    private UUID id;
    private String fullName;
    private int age;
    private String email;
    private String phone;
    private String location;
    private AddressDto address;
    private String status;
    private String reviewNotes;
    private UUID reviewedBy;
    private LocalDateTime reviewDate;
    private LocalDateTime createdDate;
    private LocalDateTime lastModifiedDate;
}
