package com.niit.KanbanBoardService.domian;


import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document
public class Task {
    @Id
    private String taskId;
    private String taskName;
    private String taskImageAdd;
    private String priority;


    public Task() {
    }

    public Task(String taskId, String taskName, String taskImageAdd, String priority) {
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskImageAdd = taskImageAdd;
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Task{" +
                "taskId='" + taskId + '\'' +
                ", taskName='" + taskName + '\'' +
                ", taskImageAdd='" + taskImageAdd + '\'' +
                ", priority='" + priority + '\'' +
                '}';
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getTaskImageAdd() {
        return taskImageAdd;
    }

    public void setTaskImageAdd(String taskImageAdd) {
        this.taskImageAdd = taskImageAdd;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }


}
