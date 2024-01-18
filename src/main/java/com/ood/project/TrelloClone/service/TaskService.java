package com.ood.project.TrelloClone.service;

import com.ood.project.TrelloClone.model.enitity.Task;
import com.ood.project.TrelloClone.model.enitity.TaskComment;
import com.ood.project.TrelloClone.model.enitity.TaskHistoryTable;
import com.ood.project.TrelloClone.model.enitity.TaskUsers;
import com.ood.project.TrelloClone.model.task.AddCommentRequest;
import com.ood.project.TrelloClone.model.task.AddUserRequest;
import com.ood.project.TrelloClone.model.task.ModifyTaskRequest;
import com.ood.project.TrelloClone.model.task.TaskResponse;

import java.util.List;

public interface TaskService {
    TaskResponse saveTask(Task task);

    TaskResponse getTask(long taskID);

    List<TaskResponse> getAllTask();

    TaskResponse modifyTask(ModifyTaskRequest modifyTaskRequest);

    List<TaskHistoryTable> getHistoryTable(long taskID);

    void deleteTaskByID(long taskID);
    TaskResponse undo(long taskID);
}
