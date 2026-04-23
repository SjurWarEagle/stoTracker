package com.stotracker.service;

import com.stotracker.model.StoData;
import com.stotracker.repository.StoDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StoDataServiceTest {

    @Mock
    private StoDataRepository repository;

    private StoDataService service;

    @BeforeEach
    void setUp() {
        service = new StoDataService(repository);
    }

    @Test
    void getAllData_returnsOrderedList() {
        // Arrange
        StoData data1 = new StoData("Char1");
        data1.setUpdatedAt(LocalDateTime.now().minusHours(1));
        StoData data2 = new StoData("Char2");
        data2.setUpdatedAt(LocalDateTime.now());
        when(repository.findAllByOrderByNameAsc()).thenReturn(List.of(data1, data2));

        // Act
        Result<List<StoData>> result = service.getAllData();

        // Assert
        assertTrue(result.success());
        assertEquals(2, result.data().size());
        assertEquals("Char1", result.data().get(0).getName());
    }

    @Test
    void addName_withValidName_succeeds() {
        // Arrange
        String name = "NewChar";
        when(repository.findByName(name)).thenReturn(Optional.empty());
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.addName(name);

        // Assert
        assertTrue(result.success());
        assertEquals(name, result.data().getName());
        assertEquals(0, result.data().getDilithium());
        assertEquals(0, result.data().getCredits());
    }

    @Test
    void addName_withBlankName_fails() {
        // Act
        Result<StoData> result = service.addName("   ");

        // Assert
        assertFalse(result.success());
        assertEquals("Name cannot be empty", result.error());
    }

    @Test
    void addName_withNullName_fails() {
        // Act
        Result<StoData> result = service.addName(null);

        // Assert
        assertFalse(result.success());
        assertEquals("Name cannot be empty", result.error());
    }

    @Test
    void addName_withExistingName_fails() {
        // Arrange
        String name = "ExistingChar";
        when(repository.findByName(name)).thenReturn(Optional.of(new StoData(name)));

        // Act
        Result<StoData> result = service.addName(name);

        // Assert
        assertFalse(result.success());
        assertEquals("Name already exists", result.error());
    }

    @Test
    void addName_trimsWhitespace() {
        // Arrange
        String name = "  TrimmedChar  ";
        when(repository.findByName("TrimmedChar")).thenReturn(Optional.empty());
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.addName(name);

        // Assert
        assertTrue(result.success());
        assertEquals("TrimmedChar", result.data().getName());
    }

    @Test
    void updateData_withValidId_updatesFields() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setDilithium(100);
        existing.setCredits(500);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.updateData(id, 200, 1000);

        // Assert
        assertTrue(result.success());
        assertEquals(200, result.data().getDilithium());
        assertEquals(1000, result.data().getCredits());
    }

    @Test
    void updateData_withInvalidId_fails() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Result<StoData> result = service.updateData(999L, 100, 200);

        // Assert
        assertFalse(result.success());
        assertEquals("Record not found", result.error());
    }

    @Test
    void updateData_ignoresNegativeValues() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setDilithium(100);
        existing.setCredits(500);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.updateData(id, -50, -100);

        // Assert
        assertTrue(result.success());
        assertEquals(100, result.data().getDilithium()); // Unchanged
        assertEquals(500, result.data().getCredits()); // Unchanged
    }

    @Test
    void deleteByName_withValidName_succeeds() {
        // Arrange
        String name = "CharToDelete";
        StoData toDelete = new StoData(name);
        when(repository.findByName(name)).thenReturn(Optional.of(toDelete));
        doNothing().when(repository).deleteByName(name);

        // Act
        Result<StoData> result = service.deleteByName(name);

        // Assert
        assertTrue(result.success());
        verify(repository).deleteByName(name);
    }

    @Test
    void deleteByName_withBlankName_fails() {
        // Act
        Result<StoData> result = service.deleteByName("   ");

        // Assert
        assertFalse(result.success());
        assertEquals("Name cannot be empty", result.error());
    }

    @Test
    void deleteByName_withNonExistentName_fails() {
        // Arrange
        when(repository.findByName("NonExistent")).thenReturn(Optional.empty());

        // Act
        Result<StoData> result = service.deleteByName("NonExistent");

        // Assert
        assertFalse(result.success());
        assertEquals("Name not found", result.error());
    }

    @Test
    void recordTimestamp_recruitment_setsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.recordTimestamp(id, "recruitment");

        // Assert
        assertTrue(result.success());
        assertNotNull(result.data().getRecruitmentTime());
        assertNull(result.data().getRefiningTime());
        assertNull(result.data().getEventTime());
    }

    @Test
    void recordTimestamp_refining_setsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.recordTimestamp(id, "refining");

        // Assert
        assertTrue(result.success());
        assertNull(result.data().getRecruitmentTime());
        assertNotNull(result.data().getRefiningTime());
        assertNull(result.data().getEventTime());
    }

    @Test
    void recordTimestamp_event_setsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.recordTimestamp(id, "event");

        // Assert
        assertTrue(result.success());
        assertNull(result.data().getRecruitmentTime());
        assertNull(result.data().getRefiningTime());
        assertNotNull(result.data().getEventTime());
    }

    @Test
    void recordTimestamp_invalidType_fails() {
        // Arrange
        when(repository.findById(1L)).thenReturn(Optional.of(new StoData("Char")));

        // Act
        Result<StoData> result = service.recordTimestamp(1L, "invalid");

        // Assert
        assertFalse(result.success());
        assertEquals("Invalid timestamp type", result.error());
    }

    @Test
    void recordTimestamp_caseInsensitive() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.recordTimestamp(id, "RECRUITMENT");

        // Assert
        assertTrue(result.success());
        assertNotNull(result.data().getRecruitmentTime());
    }

    @Test
    void recordTimestamp_refiningCaseInsensitive_setsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.recordTimestamp(id, "REFINING");

        // Assert
        assertTrue(result.success());
        assertNotNull(result.data().getRefiningTime());
    }

    @Test
    void clearTimestamp_recruitment_clearsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setRecruitmentTime(LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.clearTimestamp(id, "recruitment");

        // Assert
        assertTrue(result.success());
        assertNull(result.data().getRecruitmentTime());
    }

    @Test
    void clearTimestamp_refining_clearsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setRefiningTime(LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.clearTimestamp(id, "refining");

        // Assert
        assertTrue(result.success());
        assertNull(result.data().getRefiningTime());
    }

    @Test
    void clearTimestamp_refiningCaseInsensitive_clearsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setRefiningTime(LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.clearTimestamp(id, "REFINING");

        // Assert
        assertTrue(result.success());
        assertNull(result.data().getRefiningTime());
    }

    @Test
    void clearTimestamp_event_clearsTime() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setEventTime(LocalDateTime.now());
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.clearTimestamp(id, "event");

        // Assert
        assertTrue(result.success());
        assertNull(result.data().getEventTime());
    }

    @Test
    void clearTimestamp_invalidType_fails() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        when(repository.findById(id)).thenReturn(Optional.of(existing));

        // Act
        Result<StoData> result = service.clearTimestamp(id, "invalid");

        // Assert
        assertFalse(result.success());
        assertEquals("Invalid timestamp type", result.error());
    }

    @Test
    void clearTimestamp_nonExistentId_fails() {
        // Arrange
        when(repository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Result<StoData> result = service.clearTimestamp(999L, "recruitment");

        // Assert
        assertFalse(result.success());
        assertEquals("Record not found", result.error());
    }

    @Test
    void updateData_setsUpdatedAt() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setUpdatedAt(LocalDateTime.now().minusHours(1));
        LocalDateTime beforeUpdate = existing.getUpdatedAt();
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.updateData(id, 100, null);

        // Assert
        assertTrue(result.success());
        assertNotNull(result.data().getUpdatedAt());
    }

    @Test
    void recordTimestamp_setsUpdatedAt() {
        // Arrange
        Long id = 1L;
        StoData existing = new StoData("Char");
        existing.setId(id);
        existing.setUpdatedAt(LocalDateTime.now().minusHours(1));
        when(repository.findById(id)).thenReturn(Optional.of(existing));
        when(repository.save(any(StoData.class))).thenAnswer(inv -> inv.getArgument(0));

        // Act
        Result<StoData> result = service.recordTimestamp(id, "recruitment");

        // Assert
        assertTrue(result.success());
        assertNotNull(result.data().getUpdatedAt());
    }
}