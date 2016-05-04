package org.dmitry.tasks.resources;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;

import org.dmitry.tasks.domain.Task;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mvc.ResourceAssemblerSupport;

public abstract class TaskResourceAssemblerSupport extends ResourceAssemblerSupport<Task, TaskResource> {
	private final Class<?> controllerCls;

	public TaskResourceAssemblerSupport(Class<?> controllerClass) {
		super(controllerClass, TaskResource.class);
		this.controllerCls = controllerClass;
	}
	
	protected Link getSelfLink(Task entity) {
		return linkTo(controllerCls)
				.slash(entity.getId())
				.withSelfRel();
	}
	
	protected Link getCompleteLink(Task entity) {
		return linkTo(controllerCls)
				.slash(entity.getId())
				.slash(ELinkRelation.COMPLETE.name().toLowerCase())
				.withRel(ELinkRelation.COMPLETE.getStringValue());
	}

}
