package org.dmitry.tasks.resources;

import static org.springframework.hateoas.TemplateVariable.VariableType.REQUEST_PARAM;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import org.dmitry.tasks.controllers.TaskController;
import org.dmitry.tasks.domain.Task;
import org.dmitry.tasks.util.Constants;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;

public class TaskResourceAssembler extends TaskResourceAssemblerSupport {
	private final String userId;

	public TaskResourceAssembler(String userId) {
		super(TaskController.class);
		this.userId = userId;
	}
	
	@Override
	public TaskResource toResource(Task entity) {
		TaskResource resource = new TaskResource();
		resource.setName(entity.getName());
		resource.setDescription(entity.getDescription());
		resource.setOwner(entity.getOwner());
		resource.setAssignee(entity.getAssignee());
		resource.setCompleted(entity.getCompleted());
		resource.setCreated(entity.getCreated());
		resource.setAssigned(entity.getAssigned());
		resource.setTargetObjName(entity.getTargetObjName());
		resource.setTargetObjUrl(entity.getTargetObjUrl());

		// self link
		resource.add(getSelfLink(entity));
		// edit link: the task can be edited if it is not assigned
		if (!entity.isAssigned())
			resource.add(getEditLink(entity));
		if (!entity.isCompleted()) { // if not completed
			// remove task link
			resource.add(getRemoveLink(entity));
			// re-assign link: only owner can re-assign the task
			if (entity.getOwner().equals(userId)) {
				resource.add(getAssignLink(entity));
			}
			// complete link: if assigned and not completed, the task can be completed
			if (entity.isAssigned())
				resource.add(getCompleteLink(entity));
		}
		
		return resource;
	}
	
	private Link getAssignLink(Task entity) {
		TemplateVariable var = new TemplateVariable(Constants.USER_PARAM, REQUEST_PARAM);
		TemplateVariables vars = new TemplateVariables(var);
		String baseLink = linkTo(TaskController.class).slash(entity.getId())
				.slash(ELinkRelation.ASSIGN.name().toLowerCase()).toString();
		UriTemplate template = new UriTemplate(baseLink, vars);
 		return new Link(template, ELinkRelation.ASSIGN.getStringValue());
	}
	
	private Link getEditLink(Task entity) {
		return linkTo(methodOn(TaskController.class)
				.getTask(entity.getId()))
				.withRel(ELinkRelation.EDIT.getStringValue());
	}
	
	private Link getRemoveLink(Task entity) {
		return linkTo(methodOn(TaskController.class)
				.getTask(entity.getId()))
				.withRel(ELinkRelation.REMOVE.getStringValue());
	}
}
