package org.dmitry.tasks.resources;

import org.springframework.hateoas.ResourceSupport;

public class HomePageResource extends ResourceSupport {
	private static final String name = "Task service";
	public HomePageResource() {
	}
	public String getName() {
		return name;
	}
}
