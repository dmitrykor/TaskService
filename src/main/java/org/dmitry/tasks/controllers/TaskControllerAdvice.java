package org.dmitry.tasks.controllers;

import org.dmitry.tasks.util.IncompleteTaskException;
import org.dmitry.tasks.util.TaskIsReadOnlyException;
import org.dmitry.tasks.util.TaskNotFoundException;
import org.dmitry.tasks.util.UnassignedTaskException;
import org.dmitry.tasks.util.UserNotFoundException;
import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class TaskControllerAdvice {
	
    @ResponseBody
    @ExceptionHandler(TaskNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public VndErrors userNotFoundExceptionHandler(TaskNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }
	
    @ResponseBody
    @ExceptionHandler({TaskIsReadOnlyException.class, IncompleteTaskException.class,
    	UnassignedTaskException.class, UserNotFoundException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public VndErrors taskIsReadOnlyExceptionHandler(RuntimeException ex) {
        return new VndErrors("error", ex.getMessage());
    }

}
