package com.feadev.eaids.validation;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import net.jqwik.api.*;

import java.math.BigDecimal;
import java.util.Set;

/**
 * Property-based tests for physical metrics range validation.
 * heightCm must be between 50 and 300 inclusive.
 * weightKg must be between 20 and 300 inclusive.
 *
 * Validates: Requirements 4.3
 */
class PhysicalMetricsValidationPropertyTest {

    private static final Validator validator;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // Test wrapper for height validation
    private record HeightHolder(
            @DecimalMin(value = "50", message = "Height must be at least 50 cm")
            @DecimalMax(value = "300", message = "Height must not exceed 300 cm")
            BigDecimal heightCm
    ) {}

    // Test wrapper for weight validation
    private record WeightHolder(
            @DecimalMin(value = "20", message = "Weight must be at least 20 kg")
            @DecimalMax(value = "300", message = "Weight must not exceed 300 kg")
            BigDecimal weightKg
    ) {}

    @Property
    void validHeightValuesShouldPass(@ForAll("validHeights") BigDecimal heightCm) {
        HeightHolder holder = new HeightHolder(heightCm);
        Set<ConstraintViolation<HeightHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected valid height " + heightCm + " but got violations: " + violations);
        }
    }

    @Property
    void heightBelowMinimumShouldFail(@ForAll("tooShortHeights") BigDecimal heightCm) {
        HeightHolder holder = new HeightHolder(heightCm);
        Set<ConstraintViolation<HeightHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for height below 50: " + heightCm);
        }
    }

    @Property
    void heightAboveMaximumShouldFail(@ForAll("tooTallHeights") BigDecimal heightCm) {
        HeightHolder holder = new HeightHolder(heightCm);
        Set<ConstraintViolation<HeightHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for height above 300: " + heightCm);
        }
    }

    @Property
    void validWeightValuesShouldPass(@ForAll("validWeights") BigDecimal weightKg) {
        WeightHolder holder = new WeightHolder(weightKg);
        Set<ConstraintViolation<WeightHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected valid weight " + weightKg + " but got violations: " + violations);
        }
    }

    @Property
    void weightBelowMinimumShouldFail(@ForAll("tooLightWeights") BigDecimal weightKg) {
        WeightHolder holder = new WeightHolder(weightKg);
        Set<ConstraintViolation<WeightHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for weight below 20: " + weightKg);
        }
    }

    @Property
    void weightAboveMaximumShouldFail(@ForAll("tooHeavyWeights") BigDecimal weightKg) {
        WeightHolder holder = new WeightHolder(weightKg);
        Set<ConstraintViolation<WeightHolder>> violations = validator.validate(holder);
        if (violations.isEmpty()) {
            throw new AssertionError("Expected rejection for weight above 300: " + weightKg);
        }
    }

    @Property
    void nullHeightShouldPassValidation() {
        // Null is valid for @DecimalMin/@DecimalMax; @NotNull handles null
        HeightHolder holder = new HeightHolder(null);
        Set<ConstraintViolation<HeightHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected null height to pass (let @NotNull handle it)");
        }
    }

    @Property
    void nullWeightShouldPassValidation() {
        // Null is valid for @DecimalMin/@DecimalMax; @NotNull handles null
        WeightHolder holder = new WeightHolder(null);
        Set<ConstraintViolation<WeightHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected null weight to pass (let @NotNull handle it)");
        }
    }

    @Property
    void boundaryHeightValuesShouldPass(@ForAll("boundaryHeights") BigDecimal heightCm) {
        HeightHolder holder = new HeightHolder(heightCm);
        Set<ConstraintViolation<HeightHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected boundary height " + heightCm + " to pass but got violations: " + violations);
        }
    }

    @Property
    void boundaryWeightValuesShouldPass(@ForAll("boundaryWeights") BigDecimal weightKg) {
        WeightHolder holder = new WeightHolder(weightKg);
        Set<ConstraintViolation<WeightHolder>> violations = validator.validate(holder);
        if (!violations.isEmpty()) {
            throw new AssertionError("Expected boundary weight " + weightKg + " to pass but got violations: " + violations);
        }
    }

    @Provide
    Arbitrary<BigDecimal> validHeights() {
        // Heights between 50 and 300 inclusive, with one decimal place
        return Arbitraries.integers().between(500, 3000)
                .map(i -> BigDecimal.valueOf(i).divide(BigDecimal.TEN));
    }

    @Provide
    Arbitrary<BigDecimal> tooShortHeights() {
        // Heights below 50
        return Arbitraries.integers().between(1, 499)
                .map(i -> BigDecimal.valueOf(i).divide(BigDecimal.TEN));
    }

    @Provide
    Arbitrary<BigDecimal> tooTallHeights() {
        // Heights above 300
        return Arbitraries.integers().between(3001, 5000)
                .map(i -> BigDecimal.valueOf(i).divide(BigDecimal.TEN));
    }

    @Provide
    Arbitrary<BigDecimal> validWeights() {
        // Weights between 20 and 300 inclusive, with one decimal place
        return Arbitraries.integers().between(200, 3000)
                .map(i -> BigDecimal.valueOf(i).divide(BigDecimal.TEN));
    }

    @Provide
    Arbitrary<BigDecimal> tooLightWeights() {
        // Weights below 20
        return Arbitraries.integers().between(1, 199)
                .map(i -> BigDecimal.valueOf(i).divide(BigDecimal.TEN));
    }

    @Provide
    Arbitrary<BigDecimal> tooHeavyWeights() {
        // Weights above 300
        return Arbitraries.integers().between(3001, 5000)
                .map(i -> BigDecimal.valueOf(i).divide(BigDecimal.TEN));
    }

    @Provide
    Arbitrary<BigDecimal> boundaryHeights() {
        // Exact boundary values
        return Arbitraries.of(
                new BigDecimal("50"),
                new BigDecimal("50.0"),
                new BigDecimal("300"),
                new BigDecimal("300.0")
        );
    }

    @Provide
    Arbitrary<BigDecimal> boundaryWeights() {
        // Exact boundary values
        return Arbitraries.of(
                new BigDecimal("20"),
                new BigDecimal("20.0"),
                new BigDecimal("300"),
                new BigDecimal("300.0")
        );
    }
}
