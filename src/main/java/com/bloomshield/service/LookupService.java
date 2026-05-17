package com.bloomshield.service;

import org.springframework.stereotype.Service;

import com.bloomshield.model.User;
import com.bloomshield.repo.UserRepository;

@Service
public class LookupService {

    private final UserRepository userRepository;

    public LookupService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    
    public boolean checkIfUserExits(String user){
        User u = userRepository.findByUser_name(user);
        return u!=null;
    }

    public boolean registerUser(String user){
        if(checkIfUserExits(user)) return false;
        User u = new User(user);
        userRepository.save(u);
        return true;
    }
}
