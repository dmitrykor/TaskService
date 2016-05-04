package org.dmitry.tasks.controllers;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

import org.dmitry.tasks.domain.Task;
import org.dmitry.tasks.domain.TaskStatus;
import org.dmitry.tasks.repo.UserRepository;
import org.dmitry.tasks.resources.TaskResource;
import org.dmitry.tasks.resources.TaskResourceAssembler;
import org.dmitry.tasks.services.TaskService;
import org.dmitry.tasks.util.Constants;
import org.dmitry.tasks.util.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resources;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Handles requests for manipulating of tasks
 */
@RestController
@RequestMapping(value="/api/tasks")
public class TaskController {
	
	@Autowired
	private TaskService taskService;
	
	@Autowired
	private UserRepository userRepo;
	
	@RequestMapping(method = RequestMethod.POST)
	public ResponseEntity<TaskResource> createTask(
			Principal principal, @RequestBody TaskResource taskRes) {
		taskRes.setOwner(principal.getName());
		Task task = taskService.createTask(taskRes);
		return buildTaskResponse(task, HttpStatus.CREATED);
	}
	
	@RequestMapping(value = "/{taskId}", method = RequestMethod.PUT)
	public HttpEntity<TaskResource> updateTask(
			@PathVariable long taskId, @RequestBody TaskResource taskRes) {
		Task task = taskService.updateTask(taskId, taskRes);
		return buildTaskResponseOK(task);		
	}
	
	@RequestMapping(value = "/{taskId}/assign", method = RequestMethod.PUT)
	public ResponseEntity<?> assignTask(
			@PathVariable long taskId,
			@RequestParam(name = Constants.USER_PARAM, required = true) String userId) {
		if (!StringUtils.isEmpty(userId))
			validateUser(userId);
		Task task = taskService.assignToUser(taskId, StringUtils.isEmpty(userId) ? null : userId);
		return buildTaskResponseOK(task);		
	}
	
	@RequestMapping(value = "/{taskId}/complete", method = RequestMethod.PUT)
	public ResponseEntity<?> completeTask(@PathVariable long taskId) {
		Task task = taskService.completeTask(taskId);
		return buildTaskResponseOK(task);		
	}
	
	@RequestMapping(value = "/{taskId}", method = RequestMethod.GET)
	public ResponseEntity<TaskResource> getTask(@PathVariable long taskId) {
		Task task = taskService.getTask(taskId);
		return buildTaskResponseOK(task);		
	}
	
	@RequestMapping(method = RequestMethod.GET)
	public Resources<TaskResource> getTasks(Principal principal,
			@RequestParam(name = Constants.STATUS_PARAM, required = false, defaultValue = "unassigned") String status) {
		List<Task> tasks = taskService.getTasks(principal.getName(), TaskStatus.valueOf(status.toUpperCase()));
		TaskResourceAssembler assembler = new TaskResourceAssembler(principal.getName());
		List<TaskResource> resources = 
				tasks.stream()
					.map(assembler::toResource)
					.collect(Collectors.toList());
		return new Resources<TaskResource>(resources);
	}
	
	@RequestMapping(value = "/{taskId}", method = RequestMethod.DELETE)
	public ResponseEntity<?> deleteTask(@PathVariable long taskId) {
		taskService.deleteTask(taskId);
		return new ResponseEntity<>(HttpStatus.OK);		
	}
	
	private ResponseEntity<TaskResource> buildTaskResponseOK(Task task) {
		return buildTaskResponse(task, HttpStatus.OK);
	}
	
	private ResponseEntity<TaskResource> buildTaskResponse(Task task, HttpStatus status) {
		String username = SecurityContextHolder.getContext().getAuthentication().getName();
		TaskResourceAssembler assembler = new TaskResourceAssembler(username);
		TaskResource resource = assembler.toResource(task);
		return new ResponseEntity<>(resource, status);		
	}
	
	private void validateUser(String userId) {
		userRepo.findByUsername(userId)
			.orElseThrow(() -> new UserNotFoundException(userId));
	}

}
