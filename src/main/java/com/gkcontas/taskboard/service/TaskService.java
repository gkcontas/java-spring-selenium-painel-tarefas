package com.gkcontas.taskboard.service;

import com.gkcontas.taskboard.model.Task;
import com.gkcontas.taskboard.repository.TaskRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional(readOnly = true)
    public List<Task> list() {
        return taskRepository.findAllByOrderByCreatedAtAsc();
    }

    public Task create(String title) {
        return taskRepository.save(new Task(title));
    }

    public void complete(Long id) {
        findEntityById(id).complete();
    }

    public void delete(Long id) {
        Task task = findEntityById(id);
        taskRepository.delete(task);
    }

    private Task findEntityById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found with id " + id));
    }
}
