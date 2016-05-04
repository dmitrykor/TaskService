package org.dmitry.tasks.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "task")
public class Task {
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;			// Task id
	private String name;		// Task name
	private String description;	// Task description
	private Date created;		// Date when the task has been created
	private Date assigned;		// Date when the task has been assigned
	private Date completed;		// Date when the task has been completed
	private String owner;		// Task creator id
	private String assignee;	// Task executor id
	private String targetObjUrl;	// URL to an object the task is associated with
	private String targetObjName;	// Name used for representing associated object
	
	public long getId() {
		return id;
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
	public boolean isAssigned() {
		return this.assignee != null;
	}
	public Date getCompleted() {
		return completed;
	}
	public void setCompleted(Date completed) {
		this.completed = completed;
	}
	public boolean isCompleted() {
		return completed != null;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String userId) {
		this.owner = userId;
	}
	public String getAssignee() {
		return assignee;
	}
	public void setAssignee(String assignee) {
		this.assignee = assignee;
	}
	public boolean isReadOnly() {
		return assigned != null;
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
