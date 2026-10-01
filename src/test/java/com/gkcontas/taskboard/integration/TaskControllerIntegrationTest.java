package com.gkcontas.taskboard.integration;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
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

    /**
     * Matches only ids whose suffix is numeric. A plain search for {@code id="task-}
     * would hit {@code id="task-form"} and {@code id="task-title-input"} first, since
     * the add-task form is rendered above the list.
     */
    private static final Pattern TASK_ID_PATTERN = Pattern.compile("id=\"task-(\\d+)\"");

    private Long extractTaskId(String html) {
        Matcher matcher = TASK_ID_PATTERN.matcher(html);
        if (!matcher.find()) {
            throw new AssertionError("No rendered task id found in the page");
        }
        return Long.valueOf(matcher.group(1));
    }
}
