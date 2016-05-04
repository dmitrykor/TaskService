package org.dmitry.tasks.resources;

public enum ELinkRelation {
	EDIT("edit"),
	REMOVE("http://identifiers.dmitry.org/linkrel/remove"),
	ASSIGN("http://identifiers.dmitry.org/linkrel/assign"),
	COMPLETE("http://identifiers.dmitry.org/linkrel/complete"),
	
	TARGET_OBJECT("http://identifiers.dmitry.org/linkrel/target"),
	
	TASKS("http://identifiers.dmitry.org/linkrel/tasks"),
	INBOX("http://identifiers.dmitry.org/linkrel/inbox");

	private String linkRelation;

	private ELinkRelation(String link) {
		linkRelation = link;
	}

	public String getStringValue() {
		return linkRelation;
	}
}
