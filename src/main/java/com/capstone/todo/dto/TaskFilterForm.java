package com.capstone.todo.dto;

import com.capstone.todo.domain.TaskStatus;

import java.time.LocalDate;

public class TaskFilterForm {

    private String keyword;
    private String status;
    private LocalDate dateFrom;
    private LocalDate dateTo;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(LocalDate dateFrom) {
        this.dateFrom = dateFrom;
    }

    public LocalDate getDateTo() {
        return dateTo;
    }

    public void setDateTo(LocalDate dateTo) {
        this.dateTo = dateTo;
    }

    public boolean hasAnyFilter() {
        return hasKeyword() || hasStatusFilter() || dateFrom != null || dateTo != null;
    }

    public TaskStatus getResolvedStatus() {
        if (!hasStatusFilter()) {
            return null;
        }
        return TaskStatus.valueOf(status.trim().toUpperCase());
    }

    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }

    public boolean hasStatusFilter() {
        return status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status.trim());
    }
}
