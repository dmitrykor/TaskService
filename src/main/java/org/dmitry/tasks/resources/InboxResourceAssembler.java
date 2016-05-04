package org.dmitry.tasks.resources;

import org.dmitry.tasks.controllers.InboxController;
import org.dmitry.tasks.domain.Task;
import org.springframework.hateoas.Link;

public class InboxResourceAssembler extends TaskResourceAssemblerSupport {

	public InboxResourceAssembler() {
		super(InboxController.class);
	}

	@Override
	public TaskResource toResource(Task entity) {
		TaskResource resource = new TaskResource();
		resource.setName(entity.getName());
		resource.setDescription(entity.getDescription());
		resource.setOwner(entity.getOwner());
		resource.setAssigned(entity.getAssigned());
		resource.setTargetObjName(entity.getTargetObjName());

		// self link
		resource.add(getSelfLink(entity));
		// link to the target object (like invoice, tc.)
		resource.add(getTargetLink(entity));
		// link to complete the task
		resource.add(getCompleteLink(entity));
		
		return resource;
	}
	
	private Link getTargetLink(Task entity) {
		return new Link(entity.getTargetObjUrl(), ELinkRelation.TARGET_OBJECT.getStringValue());
	}
}
