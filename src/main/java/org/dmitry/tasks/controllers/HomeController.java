package org.dmitry.tasks.controllers;

import static org.springframework.hateoas.TemplateVariable.VariableType.REQUEST_PARAM;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

import java.util.Collection;

import org.dmitry.tasks.resources.ELinkRelation;
import org.dmitry.tasks.resources.HomePageResource;
import org.dmitry.tasks.util.Constants;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.TemplateVariable;
import org.springframework.hateoas.TemplateVariables;
import org.springframework.hateoas.UriTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;


/**
 * Handles requests for the application api home page.
 */
@Controller
public class HomeController {
	
	@ResponseBody
    @RequestMapping(value = "/api", method = RequestMethod.GET)
    public HttpEntity<HomePageResource> getHomePage() {
 		HomePageResource resource = new HomePageResource();
 		Link selfLink = linkTo(methodOn(HomeController.class).getHomePage()).withSelfRel();
 		resource.add(selfLink);
 		Link inboxLink = linkTo(InboxController.class).withRel(ELinkRelation.INBOX.getStringValue());
 		resource.add(inboxLink);
 		// Only managers have access to task editing api
 		Collection<? extends GrantedAuthority> authorities = SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		if (authorities.stream().anyMatch((authority) -> authority.getAuthority().equals("ROLE_" + Constants.MANAGER_ROLE))) {
	 		Link tasksLink = getTasksLink();
	 		resource.add(tasksLink);
		}
		return new ResponseEntity<>(resource, HttpStatus.OK);
    }
	
	private Link getTasksLink() {
		TemplateVariable var = new TemplateVariable(Constants.STATUS_PARAM, REQUEST_PARAM);
		TemplateVariables vars = new TemplateVariables(var);
		String baseLink = linkTo(TaskController.class).toString();
		UriTemplate template = new UriTemplate(baseLink, vars);
 		return new Link(template, ELinkRelation.TASKS.getStringValue());
	}
}
