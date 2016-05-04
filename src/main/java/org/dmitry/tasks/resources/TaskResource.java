package org.dmitry.tasks.resources;

import java.util.Date;

import org.springframework.hateoas.ResourceSupport;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class TaskResource extends ResourceSupport {
	private String name;
	private String description;
	private String owner;
	private String assignee;
	private Date created;
	private Date assigned;
	private Date completed;
	private String targetObjUrl;
	private String targetObjName;
	
	public TaskResource() {
	}
	
	public TaskResource(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Date getAssigned() {
		return assigned;
	}
	public void setAssigned(Date assigned) {
		this.assigned = assigned;
	}
	public Date getCompleted() {
		return completed;
	}
	public void setCompleted(Date completed) {
		this.completed = completed;
	}
	public String getTargetObjUrl() {
		return targetObjUrl;
	}
	public void setTargetObjUrl(String targetObjUrl) {
		this.targetObjUrl = targetObjUrl;
	}
	public String getTargetObjName() {
		return targetObjName;
	}
	public void setTargetObjName(String targetObjName) {
		this.targetObjName = targetObjName;
	}

}
