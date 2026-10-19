package com.ichuvilin.bookshelf.api.model.request;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UpdateUserRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @AfterAll
    static void tearDown() {
    }

    @Test
    void validate_whenUsernameIsValid_hasNoViolations() {
        var request = new UpdateUserRequest("ivan");

        assertTrue(validator.validate(request).isEmpty());
    }

    @Test
    void validate_whenUsernameIsBlank_hasViolation() {
        var request = new UpdateUserRequest("   ");

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validate_whenUsernameExceeds50Characters_hasViolation() {
        var request = new UpdateUserRequest("a".repeat(51));

        assertFalse(validator.validate(request).isEmpty());
    }

    @Test
    void validate_whenUsernameHasExactly50Characters_isValid() {
        var request = new UpdateUserRequest("a".repeat(50));

        assertTrue(validator.validate(request).isEmpty());
    }
}
