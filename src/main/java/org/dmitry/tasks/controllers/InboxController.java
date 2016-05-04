package org.dmitry.tasks.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.dmitry.tasks.domain.Task;
import org.dmitry.tasks.resources.InboxResourceAssembler;
import org.dmitry.tasks.resources.TaskResource;
import org.dmitry.tasks.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles requests for user' inbox of assigned tasks.
 */
@RestController
@RequestMapping(value="/api/inbox")
public class InboxController {
	@Autowired
	private TaskService taskService;
	
	@RequestMapping(method = RequestMethod.GET)
	public Resources<TaskResource> getTasks() {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		List<Task> tasks = taskService.getTasksAssignedToCurrentUser(username);
		InboxResourceAssembler assembler = new InboxResourceAssembler();
		List<TaskResource> resources = 
				tasks.stream()
					.map(assembler::toResource)
					.collect(Collectors.toList());
		return new Resources<TaskResource>(resources);
	}
	
	@RequestMapping(value = "/{taskId}/complete", method = RequestMethod.PUT)
	public ResponseEntity<?> completeTask(@PathVariable long taskId) {
		Task task = taskService.completeTask(taskId);
		//TODO check if the task assigned to the current user
		//String username = SecurityContextHolder.getContext().getAuthentication().getName();
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);		
	}
	
	@RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
	public ResponseEntity<TaskResource> getTask(@PathVariable long taskId) {
		//TODO check if the task assigned to the current user
		//String username = SecurityContextHolder.getContext().getAuthentication().getName();
		Task task = taskService.getTask(taskId);
		return buildTaskResponse(task);		
	}
	
	private ResponseEntity<TaskResource> buildTaskResponse(Task task) {
		InboxResourceAssembler assembler = new InboxResourceAssembler();
		TaskResource resource = assembler.toResource(task);
		return new ResponseEntity<>(resource, HttpStatus.OK);		
	}

}
