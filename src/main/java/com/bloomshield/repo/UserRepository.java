package com.bloomshield.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bloomshield.model.User;

public interface UserRepository extends JpaRepository<User, String>{
    // Using camelCase to avoid Spring Data JPA confusing underscores with nested property traversal
    User findByUserName(String userName);
}
