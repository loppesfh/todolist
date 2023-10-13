package br.com.loppesfh.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.loppesfh.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping(path = "/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping
    public ResponseEntity<Object> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        System.out.println("chegou no controller, userID:" + request.getAttribute("userId"));
        var idUser = request.getAttribute("userId");

        var currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt()) || currentDate.isAfter(taskModel.getEndAt())) {
            return ResponseEntity.badRequest()
                    .body("A data menor que data atual.");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("A data inicial deve ser menor que data final.");
        }

        taskModel.setIdUser((UUID) idUser);
        var taskSaved = taskRepository.save(taskModel);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskSaved);
    }


    @GetMapping
    public List<TaskModel> list(HttpServletRequest request) {
        UUID userId = (UUID) request.getAttribute("userId");        
        return taskRepository.findByIdUser(userId);
    }

    @PutMapping("{taskId}")
    public TaskModel update(@RequestBody TaskModel taskModel, @PathVariable UUID taskId, HttpServletRequest request) {

        var task = taskRepository.findById(taskId).orElseThrow(() -> new RuntimeException("Não existe uma task com esse ID"));
        Utils.copyNonNullProperties(taskModel, task);
        
        return taskRepository.save(task);
    }
}