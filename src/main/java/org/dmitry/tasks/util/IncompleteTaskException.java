package org.dmitry.tasks.util;

@SuppressWarnings("serial")
public class IncompleteTaskException extends RuntimeException {
	public IncompleteTaskException(long taskId) {
		super("Could not assign task with id='" + taskId + "' since its desctiption is incomplete.");
	}
}
