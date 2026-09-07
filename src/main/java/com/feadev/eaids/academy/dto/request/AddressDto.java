package com.feadev.eaids.academy.dto.request;

import com.feadev.eaids.academy.model.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request/response representation of the embeddable {@link Address}.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDto {

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @NotBlank(message = "State is required")
    @Size(max = 100)
    private String state;

    @NotBlank(message = "LGA/Province is required")
    @Size(max = 100)
    private String lgaOrProvince;

    @NotBlank(message = "Street address is required")
    @Size(max = 255)
    private String streetAddress;

    public Address toEmbeddable() {
        return Address.builder()
                .country(country)
                .state(state)
                .lgaOrProvince(lgaOrProvince)
                .streetAddress(streetAddress)
                .build();
    }

    public static AddressDto from(Address address) {
        if (address == null) {
            return null;
        }
        return AddressDto.builder()
                .country(address.getCountry())
                .state(address.getState())
                .lgaOrProvince(address.getLgaOrProvince())
                .streetAddress(address.getStreetAddress())
                .build();
    }
}
