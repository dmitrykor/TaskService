package org.dmitry.tasks.repo;

import java.util.Optional;

import org.dmitry.tasks.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUsername(String username);
}
