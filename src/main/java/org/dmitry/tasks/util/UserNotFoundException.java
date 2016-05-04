package org.dmitry.tasks.util;

@SuppressWarnings("serial")
public class UserNotFoundException extends RuntimeException {
	public UserNotFoundException(String userId) {
		super("Could not find the user '" + userId + "'.");
	}
}
