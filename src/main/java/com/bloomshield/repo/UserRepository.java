package com.bloomshield.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bloomshield.model.User;

public interface UserRepository extends JpaRepository<User, String>{
    User findByUser_name(String user_name);
}
