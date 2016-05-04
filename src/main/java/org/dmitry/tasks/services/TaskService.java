package org.dmitry.tasks.services;

import java.util.List;

import org.dmitry.tasks.domain.Task;
import org.dmitry.tasks.domain.TaskStatus;
import org.dmitry.tasks.resources.TaskResource;
import org.dmitry.tasks.util.TaskNotFoundException;

public interface TaskService {
	/*
	 * Create new task from task' data transfer object (DTO)
	 * @param resource task' DTO
	 * @return domain task object
	 */
	Task createTask(TaskResource resource);
	/*
	 * Updates existed task from task' DTO.
	 * @param resource task' DTO
	 * @return domain task object
	 */
	Task updateTask(long taskId, TaskResource resource);
	/*
	 * Searches task given its id
	 * @param taskId
	 * @return domain task object
	 */
	Task getTask(long taskId) throws TaskNotFoundException;
	/*
	 * Returns all tasks of the current user in a given state
	 * @param status
	 * @param owner logged in user name
	 * @return the list of domain task objects
	 */
	List<Task> getTasks(String owner, TaskStatus status);
	/*
	 * Returns all uncompleted tasks assigned to the current user
	 * @param assignee logged in user name
	 * @return the list of domain task objects
	 */
	List<Task> getTasksAssignedToCurrentUser(String assignee);
	/*
	 * Assigns the task to the provided user
	 * @param taskId
	 * @param userId might be null value which means unassigning the task
	 * @return the domain task objects
	 */
	Task assignToUser(long taskId, String userId);
	/*
	 * Completes the task
	 * @param taskId
	 * @return the domain task objects
	 */
	Task completeTask(long taskId);
	/*
	 * Deleted the task
	 * @param taskId
	 */
	void deleteTask(long taskId);
}
