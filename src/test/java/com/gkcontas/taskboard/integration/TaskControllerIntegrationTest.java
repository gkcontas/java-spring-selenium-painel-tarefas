package com.gkcontas.taskboard.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class TaskControllerIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldRenderEmptyStateWhenNoTasksExist() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No tasks yet")));
    }

    @Test
    void shouldCreateCompleteAndDeleteTaskThroughFormSubmissions() throws Exception {
        mockMvc.perform(post("/tasks").param("title", "Write integration test"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        String pageAfterCreate = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Write integration test")))
                .andReturn().getResponse().getContentAsString();

        Long id = extractTaskId(pageAfterCreate);

        mockMvc.perform(post("/tasks/{id}/complete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("completed")));

        mockMvc.perform(post("/tasks/{id}/delete", id))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("Write integration test"))));
    }

    @Test
    void shouldReturnNotFoundWhenCompletingMissingTask() throws Exception {
        mockMvc.perform(post("/tasks/{id}/complete", 999_999))
                .andExpect(status().isNotFound());
    }

    private Long extractTaskId(String html) {
        int start = html.indexOf("id=\"task-");
        int idStart = start + "id=\"task-".length();
        int idEnd = html.indexOf('"', idStart);
        return Long.parseLong(html.substring(idStart, idEnd));
    }
}
