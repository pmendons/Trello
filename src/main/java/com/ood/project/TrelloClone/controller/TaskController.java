package com.ood.project.TrelloClone.controller;

import com.ood.project.TrelloClone.model.enitity.Task;
import com.ood.project.TrelloClone.model.enitity.TaskComment;
import com.ood.project.TrelloClone.model.enitity.TaskHistoryTable;
import com.ood.project.TrelloClone.model.enitity.TaskUsers;
import com.ood.project.TrelloClone.model.task.AddCommentRequest;
import com.ood.project.TrelloClone.model.task.AddUserRequest;
import com.ood.project.TrelloClone.model.task.ModifyTaskRequest;
import com.ood.project.TrelloClone.model.task.TaskResponse;
import com.ood.project.TrelloClone.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tasks")
public class TaskController {
    private final TaskService taskService;

    /**
     * SHOWS BOARD
     * All Tasks in order of TODO --> DOING --> DONE
     * @return
     */
    @GetMapping("/getAll")
    public ResponseEntity<List<TaskResponse>> getTasks() {
        return ResponseEntity.ok().body(taskService.getAllTask());
    }

    /**
     * Requests a task body with taskName and description
     * Saves tasks
     * Status --> TODO
     * taskID increments, primary key
     * Returns task body
     * @param task
     * @return
     */
    @PostMapping("/add")
    public ResponseEntity<TaskResponse> saveTask(@RequestBody Task task) {
        return ResponseEntity.ok().body(taskService.saveTask(task));
    }

    /**
     * Requests ModifyTaskRequestBody
     * Mandatory to give taskID
     * Returns the updated task
     * Sets time updated to current time stamp
     * @param task
     * @return
     */
    @PutMapping("/modify")
    public ResponseEntity<TaskResponse> modifyTask(@RequestBody ModifyTaskRequest task) {
        return ResponseEntity.ok().body(taskService.modifyTask(task));
    }

    /**
     * Request a taskID
     * Returns that task
     * @param taskID
     * @return
     */
    @GetMapping("/get")
    public ResponseEntity<TaskResponse> getTaskById(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskService.getTask(taskID));
    }

    @PutMapping("/undo")
    public ResponseEntity<TaskResponse> undoTask(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskService.undo(taskID));
    }

    /**
     * Takes taskID
     * returns List if history of modification of that Task
     * @param taskID
     * @return
     */
    @GetMapping("/getHistory")
    public ResponseEntity<List<TaskHistoryTable>> getTaskHistoryById(@RequestParam long taskID) {
        return ResponseEntity.ok().body(taskService.getHistoryTable(taskID));
    }

    /**
     * Deletes a Task
     * takes taskID
     * @param taskID
     * @return
     */
    @DeleteMapping("/delete")
    public ResponseEntity<String> deleteTask(@RequestParam long taskID) {
        taskService.deleteTaskByID(taskID);
        return ResponseEntity.ok("Deleted task with taskId: " + taskID);
    }

}
