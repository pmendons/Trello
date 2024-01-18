package com.ood.project.TrelloClone.model.enitity;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TaskUsers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long userTaskID;
    @ManyToOne
    @JoinColumn(name = "taskID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NonNull
    private Task task;
    @NonNull
    @ManyToOne
    @JoinColumn(name = "userID")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private UserDetails userDetails;
}
