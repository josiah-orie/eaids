package com.feadev.eaids.registrations.dto.request;

import com.feadev.eaids.registrations.model.enums.DocumentType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadRequest {

    @NotNull(message = "Document type is required")
    private DocumentType documentType;
}
