package com.internal.tasktracker;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Runs against the seed data in data.sql (49 tasks, 2 of them archived).
@SpringBootTest
@AutoConfigureMockMvc
class TaskControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void searchNeverReturnsArchivedTasks() throws Exception {
        // "api" matches the two archived seed tasks via their description
        mvc.perform(get("/api/tasks").param("q", "api").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].archived", everyItem(is(false))))
                .andExpect(jsonPath("$.items[*].id", not(hasItems(20, 21))));
    }

    @Test
    void statusFilterAppliesToDescriptionMatches() throws Exception {
        mvc.perform(get("/api/tasks").param("q", "api").param("status", "OPEN").param("pageSize", "100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", greaterThan(0)))
                .andExpect(jsonPath("$.items[*].status", everyItem(is("OPEN"))));
    }

    @Test
    void blankSearchReturnsAllActiveTasks() throws Exception {
        mvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total", is(47)))
                .andExpect(jsonPath("$.items", hasSize(10)));
    }

    @Test
    void invalidStatusIsBadRequest() throws Exception {
        mvc.perform(get("/api/tasks").param("status", "nope"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void outOfRangePagingIsClamped() throws Exception {
        mvc.perform(get("/api/tasks").param("page", "0").param("pageSize", "-5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page", is(1)))
                .andExpect(jsonPath("$.pageSize", is(1)))
                .andExpect(jsonPath("$.items", hasSize(1)));

        mvc.perform(get("/api/tasks").param("pageSize", "100000"))
                .andExpect(jsonPath("$.pageSize", is(100)));
    }

    @Test
    void pagesDoNotOverlap() throws Exception {
        mvc.perform(get("/api/tasks").param("page", "5").param("pageSize", "10"))
                .andExpect(jsonPath("$.items", hasSize(7)));
        mvc.perform(get("/api/tasks").param("page", "6").param("pageSize", "10"))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void likeWildcardsAreMatchedLiterally() throws Exception {
        mvc.perform(get("/api/tasks").param("q", "%"))
                .andExpect(jsonPath("$.total", is(0)));
        mvc.perform(get("/api/tasks").param("q", "_"))
                .andExpect(jsonPath("$.total", is(0)));
    }
}
