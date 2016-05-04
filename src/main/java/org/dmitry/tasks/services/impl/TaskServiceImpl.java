package org.dmitry.tasks.services.impl;

import java.util.Date;
import java.util.List;

import org.dmitry.tasks.domain.Task;
import org.dmitry.tasks.domain.TaskStatus;
import org.dmitry.tasks.repo.TaskRepository;
import org.dmitry.tasks.resources.TaskResource;
import org.dmitry.tasks.services.TaskService;
import org.dmitry.tasks.util.IncompleteTaskException;
import org.dmitry.tasks.util.TaskIsReadOnlyException;
import org.dmitry.tasks.util.TaskNotFoundException;
import org.dmitry.tasks.util.UnassignedTaskException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TaskServiceImpl implements TaskService {
	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);
	
	@Autowired
	private TaskRepository taskRepo;

	@Override
	public Task createTask(TaskResource resource) {
		logger.debug("Creating a new task.");
		Task task = new Task();
		setTask(task, resource);
		task.setCreated(new Date());
		task.setOwner(resource.getOwner());
		return taskRepo.save(task);
	}

	@Override
	public Task getTask(long taskId) {
		return getTaskImpl(taskId);
	}

	@Override
	public Task updateTask(long taskId, TaskResource resource) {
		logger.debug("Updating a task with id = {}.", taskId);
		Task task = getTaskImpl(taskId);
		if (task.isReadOnly())
			throw new TaskIsReadOnlyException(taskId, task.isCompleted());
		setTask(task, resource);
		taskRepo.save(task);
		return task;
	}

	@Override
	public List<Task> getTasks(String owner, TaskStatus status) {
		logger.debug("Updating a task with status = {}.", status.name());
		switch (status) {
		case UNASSIGNED:
			return taskRepo.findUnassignedByOwner(owner);
		case ASSIGNED:
			return taskRepo.findAssignedByOwner(owner);
		case COMPLETED:
			return taskRepo.findCompletedByOwner(owner);
		default:
			throw new RuntimeException("Unexpected task status.");
		}
	}

	@Override
	public Task assignToUser(long taskId, String userId) {
		logger.debug("Assign a task with id = {} to user = {}.", taskId, userId);
		Task task = getTaskImpl(taskId);
		if (task.isCompleted())
			throw new TaskIsReadOnlyException(taskId, true);
		if (userId != null) {
			if (userId.equals(task.getAssignee()))
				return task; // The task already assigned to the same user
			validateTask(task); // check if all required fields are set
		}
		task.setAssigned(new Date());
		task.setAssignee(userId);
		taskRepo.save(task);
		return task;
	}

	@Override
	public Task completeTask(long taskId) {
		logger.debug("Complete a task with id = {}.", taskId);
		Task task = getTaskImpl(taskId);
		if (task.isCompleted()) // the task is already completed
			throw new TaskIsReadOnlyException(taskId, true);
		if (!task.isAssigned()) // the task has not been assigned
			throw new UnassignedTaskException(taskId);
		task.setCompleted(new Date());
		taskRepo.save(task);
		return task;
	}

	@Override
	public void deleteTask(long taskId) {
		logger.debug("Delete a task with id = {}.", taskId);
		Task task = getTaskImpl(taskId);
		if (task.isCompleted()) // Cannot delete completed task
			throw new TaskIsReadOnlyException(taskId, true);
		taskRepo.delete(task);
	}

	@Override
	public List<Task> getTasksAssignedToCurrentUser(String assignee) {
		return taskRepo.findAssignedTo(assignee);
	}
	
	private void setTask(Task task, TaskResource resource) {
		task.setName(resource.getName());
		task.setDescription(resource.getDescription());
		task.setTargetObjName(resource.getTargetObjName());
		task.setTargetObjUrl(resource.getTargetObjUrl());
	}
	
	private Task getTaskImpl(long taskId) {
		Task task = taskRepo.findOne(taskId);
		if (task == null)
			throw new TaskNotFoundException(taskId);
		return task;
	}

	private void validateTask(Task task) {
		if (StringUtils.isEmpty(task.getName()) || StringUtils.isEmpty(task.getDescription())
			|| StringUtils.isEmpty(task.getTargetObjName()) || StringUtils.isEmpty(task.getTargetObjUrl()))
			throw new IncompleteTaskException(task.getId());
	}
}
