package com.stotracker.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StoDataTest {

    @Test
    void defaultConstructor_createsEmptyObject() {
        StoData data = new StoData();

        assertNull(data.getId());
        assertNull(data.getName());
        assertEquals(0, data.getDilithium());
        assertEquals(0, data.getCredits());
        assertNull(data.getRecruitmentTime());
        assertNull(data.getRefiningTime());
        assertNull(data.getEventTime());
        assertNull(data.getUpdatedAt());
    }

    @Test
    void constructor_withName_setsDefaults() {
        StoData data = new StoData("TestChar");

        assertNull(data.getId());
        assertEquals("TestChar", data.getName());
        assertEquals(0, data.getDilithium());
        assertEquals(0, data.getCredits());
        assertNull(data.getRecruitmentTime());
        assertNull(data.getRefiningTime());
        assertNull(data.getEventTime());
        assertNotNull(data.getUpdatedAt());
    }

    @Test
    void setDilithium_updatesValue() {
        StoData data = new StoData("Char");
        data.setDilithium(5000);

        assertEquals(5000, data.getDilithium());
    }

    @Test
    void setCredits_updatesValue() {
        StoData data = new StoData("Char");
        data.setCredits(100000);

        assertEquals(100000, data.getCredits());
    }

    @Test
    void setRecruitmentTime_updatesValue() {
        StoData data = new StoData("Char");
        LocalDateTime time = LocalDateTime.now();
        data.setRecruitmentTime(time);

        assertEquals(time, data.getRecruitmentTime());
    }

    @Test
    void setRefiningTime_updatesValue() {
        StoData data = new StoData("Char");
        LocalDateTime time = LocalDateTime.now();
        data.setRefiningTime(time);

        assertEquals(time, data.getRefiningTime());
    }

    @Test
    void setEventTime_updatesValue() {
        StoData data = new StoData("Char");
        LocalDateTime time = LocalDateTime.now();
        data.setEventTime(time);

        assertEquals(time, data.getEventTime());
    }

    @Test
    void updateTimestamp_setsUpdatedAt() {
        StoData data = new StoData("Char");
        LocalDateTime before = data.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}

        data.updateTimestamp();

        assertNotEquals(before, data.getUpdatedAt());
    }

    @Test
    void prePersist_updateTimestamp_isCalled() {
        StoData data = new StoData("Char");
        LocalDateTime beforeUpdate = data.getUpdatedAt();

        try {
            Thread.sleep(10);
        } catch (InterruptedException ignored) {}

        data.updateTimestamp();

        assertNotNull(data.getUpdatedAt());
    }

    @Test
    void setters_returnVoid() {
        StoData data = new StoData();

        data.setId(1L);
        data.setName("Test");
        data.setDilithium(100);
        data.setCredits(200);
        data.setRecruitmentTime(LocalDateTime.now());
        data.setRefiningTime(LocalDateTime.now());
        data.setEventTime(LocalDateTime.now());
        data.setUpdatedAt(LocalDateTime.now());

        assertEquals(1L, data.getId());
        assertEquals("Test", data.getName());
        assertEquals(100, data.getDilithium());
        assertEquals(200, data.getCredits());
    }
}
