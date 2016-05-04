package org.dmitry.tasks.util;

@SuppressWarnings("serial")
public class TaskNotFoundException extends RuntimeException {
	public TaskNotFoundException(long taskId) {
		super("Could not find task with id='" + taskId + "'.");
	}
}
