package com.stotracker.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResultTest {

    @Test
    void ok_createsSuccessfulResult() {
        Result<String> result = Result.ok("test data");

        assertTrue(result.success());
        assertEquals("test data", result.data());
        assertNull(result.error());
    }

    @Test
    void ok_withNullData_succeeds() {
        Result<String> result = Result.ok(null);

        assertTrue(result.success());
        assertNull(result.data());
        assertNull(result.error());
    }

    @Test
    void err_createsFailedResult() {
        Result<String> result = Result.err("error message");

        assertFalse(result.success());
        assertNull(result.data());
        assertEquals("error message", result.error());
    }

    @Test
    void ok_withInteger_succeeds() {
        Result<Integer> result = Result.ok(42);

        assertTrue(result.success());
        assertEquals(42, result.data());
        assertNull(result.error());
    }

    @Test
    void err_withComplexError_succeeds() {
        Result<String> result = Result.err("Name already exists");

        assertFalse(result.success());
        assertNull(result.data());
        assertEquals("Name already exists", result.error());
    }
}
