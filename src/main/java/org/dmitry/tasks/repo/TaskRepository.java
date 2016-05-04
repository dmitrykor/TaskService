package org.dmitry.tasks.repo;

import java.util.List;

import org.dmitry.tasks.domain.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

public interface TaskRepository extends CrudRepository<Task, Long> {
	@Query("SELECT t FROM Task t WHERE t.assigned IS NULL AND t.owner = ?1")
	List<Task> findUnassignedByOwner(String owner);
	
	@Query("SELECT t FROM Task t WHERE t.assigned IS NOT NULL AND completed IS NULL AND t.owner = ?1")
	List<Task> findAssignedByOwner(String owner);
	
	@Query("SELECT t FROM Task t WHERE t.completed IS NOT NULL AND t.owner = ?1")
	List<Task> findCompletedByOwner(String owner);
	
	@Query("SELECT t FROM Task t WHERE t.assignee = ?1 AND completed IS NULL")
	List<Task> findAssignedTo(String assignee);
}
