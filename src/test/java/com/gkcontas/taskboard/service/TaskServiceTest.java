package com.gkcontas.taskboard.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gkcontas.taskboard.model.Task;
import com.gkcontas.taskboard.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void shouldListTasksOrderedByCreationDate() {
        Task task = new Task("Write tests");
        when(taskRepository.findAllByOrderByCreatedAtAsc()).thenReturn(List.of(task));

        List<Task> tasks = taskService.list();

        assertThat(tasks).containsExactly(task);
    }

    @Test
    void shouldCreateTaskWithGivenTitle() {
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task created = taskService.create("Buy milk");

        assertThat(created.getTitle()).isEqualTo("Buy milk");
        assertThat(created.isCompleted()).isFalse();
    }

    @Test
    void shouldMarkTaskAsCompleted() {
        Task task = new Task("Buy milk");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.complete(1L);

        assertThat(task.isCompleted()).isTrue();
    }

    @Test
    void shouldThrowNotFoundWhenCompletingMissingTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.complete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
    }

    @Test
    void shouldDeleteExistingTask() {
        Task task = new Task("Buy milk");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.delete(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingMissingTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("99");
    }
}
