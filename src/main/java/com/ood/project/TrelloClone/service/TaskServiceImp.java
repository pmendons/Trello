package com.ood.project.TrelloClone.service;

import com.ood.project.TrelloClone.model.enitity.*;
import com.ood.project.TrelloClone.model.task.*;
import com.ood.project.TrelloClone.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Long.parseLong;


@Service
@RequiredArgsConstructor
public class TaskServiceImp implements TaskService {
    private final TaskRepository taskRepo;
    private final TaskCommentRepository taskCommentRepo;
    private final TaskUsersRepository taskUsersRepo;
    private final TaskHistoryTableRepository taskHistoryTableRepository;
    private final UserRepository userRepo;
    LocalDateTime myDateObj = LocalDateTime.now();
    DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
    String formattedDate = myDateObj.format(myFormatObj);
    TaskHistoryTable taskFromHistory;

    /**
     * Takes task body
     * Sets status to default value TODO
     * sets Estimated Time To complete to 2 weeks
     * Returns Task
     * @param task
     * @return
     */
    @Override
    public TaskResponse saveTask(Task task) {
        task.setTimeCreated(formattedDate);
        task.setStatus(Status.TODO);
        task.setETC("2 weeks");
        taskRepo.save(task);
        return getTaskResponse(task);
    }

    /**
     * Takes taskID
     * Returns that task
     * @param taskID
     * @return
     */
    @Override
    public TaskResponse getTask(long taskID) {
        Task task = taskRepo.findByTaskID(taskID);
        return getTaskResponse(task);
    }

    /**
     * Fetches all takes in order of Status
     * @return
     */
    @Override
    public List<TaskResponse> getAllTask() {

        List<TaskResponse> taskResponseList = new ArrayList<>();
        List<Task> tasks = taskRepo.findByOrderByStatusAsc();
        for (Task task : tasks) {
            taskResponseList.add(getTaskResponse(task));
        }
        return taskResponseList;
    }


    /**
     * Takes ModifyTaskRequest body
     * ModifyTaskRequest Contains:
     *     long taskID;
     *     String stringStatus;
     *     String taskName;
     *     String taskDescription;
     *     String comment;
     *     long userID;
     * returns task with modifications
     * @param modifyTaskRequest
     * @return
     */
    @Override
    public TaskResponse modifyTask(ModifyTaskRequest modifyTaskRequest) {

        Task taskFromRepo = taskRepo.findByTaskID(modifyTaskRequest.getTaskID());
        if (modifyTaskRequest.getComment() != null) {
            TaskComment taskComment = new TaskComment();
            taskComment.setTask(taskFromRepo);
            taskComment.setComment(modifyTaskRequest.getComment());
            taskComment.setUserDetails(userRepo.findByUserID(modifyTaskRequest.getUserID()));
            taskCommentRepo.save(taskComment);
            String modification = modifyTaskRequest.getComment();
            saveToTaskHistory(taskFromRepo, modification, formattedDate, "comment");
        }
        if (modifyTaskRequest.getUserID() != 0) {
            TaskUsers taskUsers = TaskUsers.builder()
                    .task(taskFromRepo)
                    .userDetails(userRepo.findByUserID(modifyTaskRequest.getUserID()))
                    .build();
            taskUsersRepo.save(taskUsers);
            String modification = String.valueOf(modifyTaskRequest.getUserID());
            saveToTaskHistory(taskFromRepo, modification, formattedDate, "user");
        }
        if (modifyTaskRequest.getStatus() != null) {
            if (taskUsersRepo.findByTask(taskFromRepo) != null) {
                if (modifyTaskRequest.getStatus().equalsIgnoreCase("move forward")) {
                    String modification = taskFromRepo.getStatus().toString();
                    saveToTaskHistory(taskFromRepo, modification, formattedDate, "status");
                    taskFromRepo.setStatus(taskFromRepo.getStatus().transition());
                }
            }
        }
        if (modifyTaskRequest.getDescription() != null) {
            String modification =  taskFromRepo.getDescription();
            saveToTaskHistory(taskFromRepo, modification, formattedDate, "des");
            taskFromRepo.setDescription(modifyTaskRequest.getDescription());
        }
        if (modifyTaskRequest.getTaskName() != null) {
            String modification =  taskFromRepo.getTaskName();
            saveToTaskHistory(taskFromRepo, modification, formattedDate, "taskName");
            taskFromRepo.setTaskName(modifyTaskRequest.getTaskName());
        }
        taskRepo.save(taskFromRepo);
        taskFromRepo.setTimeUpdated(formattedDate);
        return getTaskResponse(taskFromRepo);
    }

    private TaskResponse getTaskResponse(Task task){
        return TaskResponse.builder().task(task)
                .comments(taskCommentRepo.findByTask(task).stream().map(TaskComment::getComment).collect(Collectors.toList()))
                .userDetails(taskUsersRepo.findByTask(task).stream().map(TaskUsers::getUserDetails).collect(Collectors.toList()))
                .build();
    }

    /**
     * Takes Task, Modifications, Value(TimeStamp)
     * Saves History of the Task
     * @param task
     * @param modification
     * @param value
     */
    private void saveToTaskHistory(Task task, String modification, String value, String tagg) {
        TaskHistoryTable taskHistoryTable = TaskHistoryTable.builder()
                .taskID(task.getTaskID())
                .taskName(task.getTaskName())
                .ETC(task.getETC())
                .status(task.getStatus())
                .description(task.getDescription())
                .timeCreated(task.getTimeCreated())
                .timeUpdated(value)
                .modification(modification)
                .tag(tagg)
                .isUndone(false)
                .build();
        taskHistoryTableRepository.save(taskHistoryTable);
    }

    /**
     * Takes taskID
     * Deletes that task
     * @param taskID
     */
    @Override
    public void deleteTaskByID(long taskID) {
        taskRepo.deleteById(taskID);
    }

    /**
     * Takes taskID
     * Undos to the previous version of the task.
     * @param taskID
     * @return
     */
    @Override
    public TaskResponse undo(long taskID) {

        List <TaskHistoryTable> taskFromHistoryList = taskHistoryTableRepository.findByTaskID(taskID);
        for (int x = taskFromHistoryList.size() -1; x>=0; x--){
            if(!taskFromHistoryList.get(x).isUndone()){
                taskFromHistory = taskFromHistoryList.get(x);
                break;
            }
        }


        Task undoTask = taskRepo.findByTaskID(taskFromHistory.getTaskID());

        List<TaskUsers> taskUsers = taskUsersRepo.findByTask(undoTask);
        List<TaskComment> taskComment = taskCommentRepo.findByTask(undoTask);
        String last = taskFromHistory.getTag();


        if (last.equals("taskName")) {
            undoTask.setTaskName(taskFromHistory.getModification());
        }
        if (last.equals("status")) {
            undoTask.setStatus(undoTask.getStatus().undo());
        }
        if (last.equals("des")) {
            undoTask.setDescription(taskFromHistory.getModification());
        }
        if (last.equals("user")) {
            for (int i = 0; i < taskUsers.size(); i++) {
                if (taskUsers.get(i).getUserDetails().getUserID() == parseLong(taskFromHistory.getModification())) {
                    long taskUserID = taskUsers.get(i).getUserTaskID();
                    taskUsersRepo.deleteById(taskUserID);
                }
            }
        }
        if (last.equals("comment")) {
            for (int i = 0; i < taskComment.size(); i++) {
                if (taskComment.get(i).getComment().equals(taskFromHistory.getModification())) {
                    long commentID = taskComment.get(i).getCommentID();
                    taskCommentRepo.deleteById(commentID);
                }
            }
        }
        undoTask.setTimeUpdated(formattedDate);
        taskRepo.save(undoTask);
        taskFromHistory.setUndone(true);
        taskHistoryTableRepository.save(taskFromHistory);

        return getTaskResponse(undoTask);
    }


    /**
     * Takes taskID
     * returns History of that task
     * @param taskID
     * @return
     */
    @Override
    public List<TaskHistoryTable> getHistoryTable(long taskID) {
        return taskHistoryTableRepository.findByTaskID(taskID);
    }
}
