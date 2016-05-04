package org.dmitry.tasks.util;

@SuppressWarnings("serial")
public class TaskIsReadOnlyException extends RuntimeException {
	public TaskIsReadOnlyException(long taskId, boolean completed) {
		super("Could not modify task with id='" + taskId +
				"' since it is already " + (completed ? "completed." : "assigned."));
	}
}
