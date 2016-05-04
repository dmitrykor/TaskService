package org.dmitry.tasks.util;

@SuppressWarnings("serial")
public class UnassignedTaskException extends RuntimeException {
	public UnassignedTaskException(long taskId) {
		super("The task with id='" + taskId + "' is unassigned.");
	}
}
