package com.capstone.todo.dto;

import java.time.LocalDate;

public class TaskFilterCriteria {

    private TaskFilterStatus status = TaskFilterStatus.ALL;
    private LocalDate from;
    private LocalDate to;

    public TaskFilterStatus getStatus() {
        return status;
    }

    public void setStatus(TaskFilterStatus status) {
        this.status = status == null ? TaskFilterStatus.ALL : status;
    }

    public LocalDate getFrom() {
        return from;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public LocalDate getTo() {
        return to;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }
}
