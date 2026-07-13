package com.feadev.eaids.development.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddStrengthRequest {

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    private String category;

    @NotBlank(message = "Description is required")
    @Size(max = 255)
    private String description;

    private BigDecimal score;
}
