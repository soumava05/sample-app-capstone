package com.capstone.todo.dto;

import com.capstone.todo.domain.TaskStatus;
import org.testng.annotations.Test;

import java.time.LocalDate;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class TaskFilterFormTest {

    @Test
    public void newTaskFilterFormShouldDefaultToNoFilters() {
        TaskFilterForm filterForm = new TaskFilterForm();

        assertNull(filterForm.getKeyword());
        assertNull(filterForm.getStatus());
        assertNull(filterForm.getDateFrom());
        assertNull(filterForm.getDateTo());
        assertFalse(filterForm.hasAnyFilter());
    }

    @Test
    public void hasAnyFilterShouldReturnTrueWhenAnyValueIsPresent() {
        TaskFilterForm filterForm = new TaskFilterForm();
        filterForm.setKeyword("plan");
        filterForm.setDateFrom(LocalDate.of(2026, 6, 1));

        assertTrue(filterForm.hasAnyFilter());
    }

    @Test
    public void resolvedStatusShouldReturnNullForAllOrBlank() {
        TaskFilterForm allFilter = new TaskFilterForm();
        allFilter.setStatus("ALL");

        TaskFilterForm blankFilter = new TaskFilterForm();
        blankFilter.setStatus(" ");

        assertNull(allFilter.getResolvedStatus());
        assertNull(blankFilter.getResolvedStatus());
        assertFalse(allFilter.hasStatusFilter());
        assertFalse(blankFilter.hasStatusFilter());
    }

    @Test
    public void resolvedStatusShouldReturnEnumForSpecificStatus() {
        TaskFilterForm filterForm = new TaskFilterForm();
        filterForm.setStatus("completed");

        assertEquals(filterForm.getResolvedStatus(), TaskStatus.COMPLETED);
        assertTrue(filterForm.hasStatusFilter());
    }
}
