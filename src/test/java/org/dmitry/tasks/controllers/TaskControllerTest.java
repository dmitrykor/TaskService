package org.dmitry.tasks.controllers;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.EnumMap;
import java.util.Map;

import javax.servlet.Filter;

import org.dmitry.tasks.TaskApplication;
import org.dmitry.tasks.domain.Task;
import org.dmitry.tasks.domain.TaskStatus;
import org.dmitry.tasks.domain.User;
import org.dmitry.tasks.repo.TaskRepository;
import org.dmitry.tasks.repo.UserRepository;
import org.dmitry.tasks.resources.ELinkRelation;
import org.dmitry.tasks.resources.TaskResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.mock.http.MockHttpOutputMessage;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = {TaskApplication.class/*, TaskControllerAdvice.class*/})
@WebAppConfiguration
public class TaskControllerTest {
	private final MediaType contentType = new MediaType("application", "hal+json");
	
	@Autowired
	private TaskRepository taskRepo;

	@Autowired
	private UserRepository userRepo;

	private MockMvc mockMvc;
	
	@SuppressWarnings("rawtypes")
	private HttpMessageConverter mappingJackson2HttpMessageConverter;
	
	@Autowired
    private WebApplicationContext webApplicationContext;
	
	@Autowired
	private Filter springSecurityFilterChain;
	
	private final static RequestPostProcessor BENDER = user("bender").password("password").roles("MANAGER");
	private final static RequestPostProcessor FRY = user("fry").password("password").roles("MANAGER");
	private final static RequestPostProcessor LEELA = user("leela").password("password").roles("USER");
	
	private final Map<TaskStatus, Task> tasks = new EnumMap<>(TaskStatus.class);
	
	
	@Autowired
    void setConverters(HttpMessageConverter<?>[] converters) {

        this.mappingJackson2HttpMessageConverter = Arrays.asList(converters).stream().filter(
                hmc -> hmc instanceof MappingJackson2HttpMessageConverter).findAny().get();

        Assert.assertNotNull("the JSON message converter must not be null",
                this.mappingJackson2HttpMessageConverter);
    }
	
    @Before
    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
        		.addFilters(springSecurityFilterChain)
        		.defaultRequest(get("/").with(BENDER))
        		.build();
        
        taskRepo.deleteAll();
        userRepo.deleteAllInBatch();
        
        Task unassignedTask = new Task();
        unassignedTask.setCreated(new Date());
        unassignedTask.setName("unassignedTask");
        unassignedTask.setDescription("The task is unassigned.");
        unassignedTask.setTargetObjName("Invoice");
        unassignedTask.setTargetObjUrl("http://dmitry.org/invoices/02389238");
        unassignedTask.setOwner("bender");
        tasks.put(TaskStatus.UNASSIGNED, taskRepo.save(unassignedTask));
        
        Task assignedTask = new Task();
        assignedTask.setCreated(new Date());
        assignedTask.setName("assignedTask");
        assignedTask.setDescription("The task is assigned.");
        assignedTask.setTargetObjName("Invoice");
        assignedTask.setTargetObjUrl("http://dmitry.org/invoices/02389238");
        assignedTask.setOwner("bender");
        assignedTask.setAssigned(new Date());
        assignedTask.setAssignee("leela");
        tasks.put(TaskStatus.ASSIGNED, taskRepo.save(assignedTask));
        
        Task completedTask = new Task();
        completedTask.setCreated(new Date());
        completedTask.setName("completedTask");
        completedTask.setDescription("The task is completed.");
        completedTask.setTargetObjName("Invoice");
        completedTask.setTargetObjUrl("http://dmitry.org/invoices/02389238");
        completedTask.setOwner("bender");
        completedTask.setAssigned(new Date());
        completedTask.setAssignee("leela");
        completedTask.setCompleted(new Date());
        tasks.put(TaskStatus.COMPLETED, taskRepo.save(completedTask));
        
        User bender = new User("bender", "password", "ROLE_MANAGER");
        userRepo.save(bender);
        User fry = new User("fry", "password", "ROLE_MANAGER");
        userRepo.save(fry);
        User leela = new User("leela", "password", "ROLE_USER");
        userRepo.save(leela);
    }
    
    /*******************************************************************************
    * Get task tests
    *******************************************************************************/
	@Test
	public void taskNotFound() throws Exception {
		this.mockMvc.perform(get("/api/tasks/10000"))
                .andExpect(status().isNotFound());
	}
    
	@Test
	public void getSingleTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.UNASSIGNED);
		String url = "/api/tasks/" + task.getId();
		this.mockMvc.perform(get(url))
            .andExpect(status().isOk())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.description", is(task.getDescription())))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.targetObjName", is(task.getTargetObjName())))
	        .andExpect(jsonPath("$.targetObjUrl", is(task.getTargetObjUrl())))
	        .andExpect(jsonPath("$._links.self.href", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']", endsWith(url+"/assign{?user}")))
    		.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.TARGET_OBJECT.getStringValue() + "']['href']").doesNotExist());
	}
    
	@Test
	public void getUnassignedTasks() throws Exception {
		String url = "/api/tasks";
		this.mockMvc.perform(get(url))
			.andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$._embedded.taskResourceList", hasSize(1)))
        	.andExpect(jsonPath("$._embedded.taskResourceList[0].owner", is("bender")));
	}
    
	@Test
	public void getUnassignedTasksAsNotOwner() throws Exception {
		String url = "/api/tasks";
		this.mockMvc.perform(get(url).with(FRY))
			.andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$._embedded").doesNotExist());
	}
    
	@Test
	public void getAssignedTasks() throws Exception {
		String url = "/api/tasks?status=assigned";
		this.mockMvc.perform(get(url))
			.andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$._embedded.taskResourceList", hasSize(1)))
	    	.andExpect(jsonPath("$._embedded.taskResourceList[0].assignee", is("leela")))
	    	.andExpect(jsonPath("$._embedded.taskResourceList[0].owner", is("bender")));
	}
    
	@Test
	public void getAssignedTasksAsNotOwner() throws Exception {
		String url = "/api/tasks?status=assigned";
		this.mockMvc.perform(get(url).with(FRY))
			.andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$._embedded").doesNotExist());
	}
    
	@Test
	public void getCompletedTasks() throws Exception {
		String url = "/api/tasks?status=completed";
		this.mockMvc.perform(get(url))
			.andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$._embedded.taskResourceList", hasSize(1)))
	    	.andExpect(jsonPath("$._embedded.taskResourceList[0].assignee", is("leela")))
	    	.andExpect(jsonPath("$._embedded.taskResourceList[0].owner", is("bender")))
			.andExpect(jsonPath("$._embedded.taskResourceList[0].completed").isNotEmpty());
	}
    
	@Test
	public void getCompletedTasksAsNotOwner() throws Exception {
		String url = "/api/tasks?status=completed";
		this.mockMvc.perform(get(url).with(FRY))
			.andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$._embedded").doesNotExist());
	}
    
	@Test
	public void getTasksByUnauthorizedUser() throws Exception {
		String url = "/api/tasks";
		this.mockMvc.perform(get(url).with(LEELA))
			.andExpect(status().isForbidden());
	}
	
    /*******************************************************************************
    * Inbox tests
    *******************************************************************************/
	@Test
	public void getInboxTasks() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
		String url = "/api/inbox";
		this.mockMvc.perform(get(url).with(LEELA))
			.andExpect(status().isOk())
            .andExpect(content().contentType(contentType))
            .andExpect(jsonPath("$._embedded.taskResourceList", hasSize(1)))
	        .andExpect(jsonPath("$._embedded.taskResourceList[0].name", is(task.getName())))
	        .andExpect(jsonPath("$._embedded.taskResourceList[0].description", is(task.getDescription())))
        	.andExpect(jsonPath("$._embedded.taskResourceList[0].assigned", notNullValue()))
	    	.andExpect(jsonPath("$._embedded.taskResourceList[0].owner", is("bender")))
			.andExpect(jsonPath("$['_embedded']['taskResourceList'][0]['_links']['self']['href']",
					endsWith("/api/inbox/" + task.getId())))
			.andExpect(jsonPath("$['_embedded']['taskResourceList'][0]['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_embedded']['taskResourceList'][0]['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_embedded']['taskResourceList'][0]['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_embedded']['taskResourceList'][0]['_links']['" + ELinkRelation.TARGET_OBJECT.getStringValue() + "']['href']").exists())
			.andExpect(jsonPath("$['_embedded']['taskResourceList'][0]['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']",
					endsWith("/api/inbox/" + task.getId() + "/complete")));
	}
    
	@Test
	public void getSingleInboxTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
		String url = "/api/inbox/" + task.getId();
		this.mockMvc.perform(get(url).with(LEELA))
            .andExpect(status().isOk())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.description", is(task.getDescription())))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.owner", is(task.getOwner())))
	        .andExpect(jsonPath("$.assigned").exists())
	        .andExpect(jsonPath("$.targetObjName", is(task.getTargetObjName())))
	        .andExpect(jsonPath("$.targetObjUrl").doesNotExist())
	        .andExpect(jsonPath("$._links.self.href", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']").doesNotExist())
    		.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']", endsWith(url + "/complete")))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.TARGET_OBJECT.getStringValue() + "']['href']").exists());
	}
	
    @Test
    public void completeInboxTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
        this.mockMvc.perform(put("/api/inbox/" + task.getId() + "/complete").with(LEELA))
        	.andExpect(status().isNoContent());
    }
	
    /*******************************************************************************
    * Create task tests
    *******************************************************************************/
    @Test
    public void createTask() throws Exception {
    	TaskResource taskRes = new TaskResource("The task", "A simple task.");
    	taskRes.setTargetObjName("Invoice");
    	taskRes.setTargetObjUrl("http://invoice.com/443534wfwer24");
        String taskJson = payload(taskRes);
        this.mockMvc.perform(post("/api/tasks")
            .contentType(contentType)
            .content(taskJson))
            .andExpect(status().isCreated())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(taskRes.getName())))
	        .andExpect(jsonPath("$.description", is(taskRes.getDescription())))
	        .andExpect(jsonPath("$.created").isNotEmpty())
	        .andExpect(jsonPath("$.owner").value("bender"))
	        .andExpect(jsonPath("$.targetObjName", is(taskRes.getTargetObjName())))
	        .andExpect(jsonPath("$.targetObjUrl", is(taskRes.getTargetObjUrl())))
	        .andExpect(jsonPath("$._links.self.href", notNullValue()))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']").isNotEmpty())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']").isNotEmpty())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']").isNotEmpty())
        	.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']").doesNotExist());
    }
	
    /*******************************************************************************
    * Update task tests
    *******************************************************************************/
    @Test
    public void updateTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.UNASSIGNED);
		String url = "/api/tasks/" + task.getId();
		TaskResource taskRes = new TaskResource("The updated task", "A simple updated task.");
        String updatedTaskJson = payload(taskRes);
        this.mockMvc.perform(put(url)
    		.contentType(contentType)
            .content(updatedTaskJson))
            .andExpect(status().isOk())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(taskRes.getName())))
	        .andExpect(jsonPath("$.description", is(taskRes.getDescription())))
	        .andExpect(jsonPath("$.created").isNotEmpty())
	        .andExpect(jsonPath("$.owner").value(task.getOwner()))
	        .andExpect(jsonPath("$._links.self.href", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']", endsWith(url+"/assign{?user}")))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']").doesNotExist());
    }
	
    @Test
    public void updateAssignedTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
		String url = "/api/tasks/" + task.getId();
		TaskResource taskRes = new TaskResource("The updated task", "A simple updated task.");
        String updatedTaskJson = payload(taskRes);
        this.mockMvc.perform(put(url)
    		.contentType(contentType)
            .content(updatedTaskJson))
            .andExpect(status().isBadRequest());
    }
	
    @Test
    public void updateCompletedTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.COMPLETED);
		String url = "/api/tasks/" + task.getId();
		TaskResource taskRes = new TaskResource("The updated task", "A simple updated task.");
        String updatedTaskJson = payload(taskRes);
        this.mockMvc.perform(put(url)
    		.contentType(contentType)
            .content(updatedTaskJson))
            .andExpect(status().isBadRequest());
    }
	
    @Test
    public void updateNoneExistedTask() throws Exception {
		String url = "/api/tasks/10000";
		TaskResource taskRes = new TaskResource("The updated task", "A simple updated task.");
        String updatedTaskJson = payload(taskRes);
        this.mockMvc.perform(put(url)
    		.contentType(contentType)
            .content(updatedTaskJson))
            .andExpect(status().isNotFound());
    }
    
    /*******************************************************************************
    * Assign task tests
    *******************************************************************************/
    @Test
    public void assignTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.UNASSIGNED);
		String url = "/api/tasks/" + task.getId();
        this.mockMvc.perform(put(url + "/assign?user=leela"))
            .andExpect(status().isOk())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.description", is(task.getDescription())))
	        .andExpect(jsonPath("$._links.self.href", containsString(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']", endsWith(url+"/assign{?user}")))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']", endsWith(url+"/complete")));
    }
    
	//TODO Add reassign to the same user
    
    @Test
    public void reassignTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
		String url = "/api/tasks/" + task.getId();
        this.mockMvc.perform(put(url + "/assign?user=leela"))
            .andExpect(status().isOk())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.description", is(task.getDescription())))
	        .andExpect(jsonPath("$._links.self.href", containsString(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']", endsWith(url+"/assign{?user}")))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']", endsWith(url+"/complete")));
    }
	
    @Test
    public void unassignTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
		String url = "/api/tasks/" + task.getId();

		this.mockMvc.perform(get(url))
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.description", is(task.getDescription())))
	        .andExpect(jsonPath("$._links.self.href", containsString(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']", endsWith(url+"/assign{?user}")))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']", endsWith(url+"/complete")));

		this.mockMvc.perform(put(url + "/assign?user="))
            .andExpect(status().isOk())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.description", is(task.getDescription())))
	        .andExpect(jsonPath("$._links.self.href", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']", endsWith(url+"/assign{?user}")))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']").doesNotExist());
    }
	
    @Test
    public void assignCompletedTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.COMPLETED);
        this.mockMvc.perform(put("/api/tasks/" + task.getId() + "/assign?user=leela"))
                .andExpect(status().isBadRequest());
    }
	
    @Test
    public void assignNoneExistedTask() throws Exception {
        this.mockMvc.perform(put("/api/tasks/1000000/assign?user=leela"))
                .andExpect(status().isNotFound());
    }
	
    @Test
    public void assignTaskToNoneExistedUser() throws Exception {
		Task task = this.tasks.get(TaskStatus.UNASSIGNED);
        this.mockMvc.perform(put("/api/tasks/" + task.getId() + "/assign?user=me"))
                .andExpect(status().isBadRequest());
    }
	
    @Test
    public void assignIncompleteTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.UNASSIGNED);
		task.setTargetObjUrl(null);
		taskRepo.save(task);
        this.mockMvc.perform(put("/api/tasks/" + task.getId() + "/assign?user=leela"))
                .andExpect(status().isBadRequest());
    }
	
    /*******************************************************************************
    * Complete task tests
    *******************************************************************************/
    @Test
    public void completeTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
		String url = "/api/tasks/" + task.getId();
        this.mockMvc.perform(put("/api/tasks/" + task.getId() + "/complete"))
        	.andExpect(status().isOk())
	        .andExpect(content().contentType(contentType))
	        .andExpect(jsonPath("$.name", is(task.getName())))
	        .andExpect(jsonPath("$.description", is(task.getDescription())))
	        .andExpect(jsonPath("$._links.self.href", endsWith(url)))
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.EDIT.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.REMOVE.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.ASSIGN.getStringValue() + "']['href']").doesNotExist())
			.andExpect(jsonPath("$['_links']['" + ELinkRelation.COMPLETE.getStringValue() + "']['href']").doesNotExist());
    }
	
    @Test
    public void completeUnassignedTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.UNASSIGNED);
        this.mockMvc.perform(put("/api/tasks/" + task.getId() + "/complete"))
                .andExpect(status().isBadRequest());
    }
	
    @Test
    public void completeCompletedTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.COMPLETED);
        this.mockMvc.perform(put("/api/tasks/" + task.getId() + "/complete"))
                .andExpect(status().isBadRequest());
    }
	
    @Test
    public void completeNoneExistedTask() throws Exception {
        this.mockMvc.perform(put("/api/tasks/10000/complete"))
                .andExpect(status().isNotFound());
    }
	
    /*******************************************************************************
    * Delete task tests
    *******************************************************************************/
    @Test
    public void deleteTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.UNASSIGNED);
		String url = "/api/tasks/" + task.getId();
        this.mockMvc.perform(delete(url))
                .andExpect(status().isOk());
		this.mockMvc.perform(get(url))
        		.andExpect(status().isNotFound());
    }
	
    @Test
    public void deleteAssignedTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.ASSIGNED);
		String url = "/api/tasks/" + task.getId();
        this.mockMvc.perform(delete(url))
        		.andExpect(status().isOk());
        this.mockMvc.perform(get(url))
				.andExpect(status().isNotFound());
    }
	
    @Test
    public void deleteCompletedTask() throws Exception {
		Task task = this.tasks.get(TaskStatus.COMPLETED);
		String url = "/api/tasks/" + task.getId();
        this.mockMvc.perform(delete(url))
                .andExpect(status().isBadRequest());
        this.mockMvc.perform(get(url))
				.andExpect(status().isOk());
    }
	
    @Test
    public void deleteNoneExistedTask() throws Exception {
		String url = "/api/tasks/10000";
        this.mockMvc.perform(delete(url))
                .andExpect(status().isNotFound());
    }

    @SuppressWarnings("unchecked")
	private String payload(Object o) throws IOException {
        MockHttpOutputMessage mockHttpOutputMessage = new MockHttpOutputMessage();
        this.mappingJackson2HttpMessageConverter.write(
                o, MediaType.APPLICATION_JSON, mockHttpOutputMessage);
        return mockHttpOutputMessage.getBodyAsString();
    }

}
