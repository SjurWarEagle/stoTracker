package com.stotracker.controller;

import com.stotracker.model.StoData;
import com.stotracker.repository.StoDataRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoDataRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void index_returnsEmptyListWhenNoData() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("index"))
                .andExpect(model().attribute("stoDataList", hasSize(0)));
    }

    @Test
    void add_createsNewCharacter() throws Exception {
        mockMvc.perform(post("/add")
                        .param("name", "TestChar")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("stoDataList", hasSize(1)))
                .andExpect(model().attribute("stoDataList", hasItem(
                        allOf(
                                hasProperty("name", is("TestChar")),
                                hasProperty("dilithium", is(0)),
                                hasProperty("credits", is(0))
                        )
                )));
    }

    @Test
    void add_withDuplicateName_redirects() throws Exception {
        repository.save(new StoData("ExistingChar"));

        mockMvc.perform(post("/add")
                        .param("name", "ExistingChar")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void update_modifiesDilithiumAndCredits() throws Exception {
        StoData saved = repository.save(new StoData("UpdateChar"));

        mockMvc.perform(post("/update")
                        .param("id", String.valueOf(saved.getId()))
                        .param("dilithium", "500")
                        .param("credits", "1000")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assert updated.getDilithium() == 500;
        assert updated.getCredits() == 1000;
    }

    @Test
    void timestamp_setsRecruitmentTime() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));

        mockMvc.perform(post("/timestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "recruitment")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assert updated.getRecruitmentTime() != null;
        assert updated.getConvertionTime() == null;
        assert updated.getEventTime() == null;
    }

    @Test
    void timestamp_setsConvertionTime() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));

        mockMvc.perform(post("/timestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "convertion")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assert updated.getConvertionTime() != null;
    }

    @Test
    void timestamp_setsEventTime() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));

        mockMvc.perform(post("/timestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "event")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assert updated.getEventTime() != null;
    }

    @Test
    void untimestamp_clearsRecruitmentTime() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));
        saved.setRecruitmentTime(java.time.LocalDateTime.now());
        repository.save(saved);

        mockMvc.perform(post("/untimestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "recruitment")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assert updated.getRecruitmentTime() == null;
    }

    @Test
    void untimestamp_clearsConvertionTime() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));
        saved.setConvertionTime(java.time.LocalDateTime.now());
        repository.save(saved);

        mockMvc.perform(post("/untimestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "convertion")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assert updated.getConvertionTime() == null;
    }

    @Test
    void untimestamp_clearsEventTime() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));
        saved.setEventTime(java.time.LocalDateTime.now());
        repository.save(saved);

        mockMvc.perform(post("/untimestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "event")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assert updated.getEventTime() == null;
    }

    @Test
    void delete_removesCharacter() throws Exception {
        repository.save(new StoData("DeleteChar"));

        mockMvc.perform(post("/delete")
                        .param("name", "DeleteChar")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("stoDataList", hasSize(0)));
    }

    @Test
    void index_returnsCharactersSortedByName() throws Exception {
        repository.save(new StoData("Zeta"));
        repository.save(new StoData("Alpha"));
        repository.save(new StoData("Beta"));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("stoDataList", hasSize(3)))
                .andExpect(model().attribute("stoDataList", hasItem(hasProperty("name", is("Alpha")))))
                .andExpect(model().attribute("stoDataList", hasItem(hasProperty("name", is("Beta")))))
                .andExpect(model().attribute("stoDataList", hasItem(hasProperty("name", is("Zeta")))));
    }

    @Test
    void add_withBlankName_redirects() throws Exception {
        mockMvc.perform(post("/add")
                        .param("name", "   ")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void update_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/update")
                        .param("id", "99999")
                        .param("dilithium", "500")
                        .param("credits", "1000")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void timestamp_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/timestamp")
                        .param("id", "99999")
                        .param("type", "recruitment")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void timestamp_withInvalidType_redirects() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));

        mockMvc.perform(post("/timestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "invalid_type")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void untimestamp_withNonExistentId_redirects() throws Exception {
        mockMvc.perform(post("/untimestamp")
                        .param("id", "99999")
                        .param("type", "recruitment")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void untimestamp_withInvalidType_redirects() throws Exception {
        StoData saved = repository.save(new StoData("TimestampChar"));

        mockMvc.perform(post("/untimestamp")
                        .param("id", String.valueOf(saved.getId()))
                        .param("type", "invalid_type")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void delete_withNonExistentName_redirects() throws Exception {
        mockMvc.perform(post("/delete")
                        .param("name", "NonExistent")
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void update_withNullDilithiumAndCredits_succeeds() throws Exception {
        StoData saved = repository.save(new StoData("UpdateChar"));
        int originalDilithium = saved.getDilithium();
        int originalCredits = saved.getCredits();

        mockMvc.perform(post("/update")
                        .param("id", String.valueOf(saved.getId()))
                        .contentType("application/x-www-form-urlencoded"))
                .andExpect(status().is3xxRedirection());

        StoData updated = repository.findById(saved.getId()).orElseThrow();
        assertEquals(Integer.valueOf(originalDilithium), updated.getDilithium());
        assertEquals(Integer.valueOf(originalCredits), updated.getCredits());
    }
}