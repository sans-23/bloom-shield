package com.bloomshield.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bloomshield.model.User;
import com.bloomshield.repo.UserRepository;

import com.bloomshield.filter.Filter;
import com.bloomshield.metrics.MetricsService;

import jakarta.annotation.PostConstruct;

@Service
public class LookupService {

    private final UserRepository userRepository;
    private final Filter filter;

    @Autowired
    private MetricsService metricsService;

    public LookupService(UserRepository userRepository, Filter filter){
        this.userRepository = userRepository;
        this.filter = filter;
    }
    
    @PostConstruct
    public void initFilter() {
        // Load all existing users into the Bloom Filter on startup
        List<User> users = userRepository.findAll();
        for(User u : users) {
            filter.add(u.getUserName());
        }
    }

    public boolean checkIfUserExits(String user){
        long startTime = System.nanoTime();
        User u = userRepository.findByUserName(user);
        long endTime = System.nanoTime();
        long timeElapsed = endTime - startTime;
        metricsService.recordDbQueryLatency(timeElapsed);
        return u!=null;
    }

    public boolean registerUser(String user){
        if(checkIfUserExits(user)) return false;
        User u = new User(user);
        userRepository.save(u);
        filter.add(user); // Add to filter when successfully registered
        return true;
    }

    public List<User> listUsers(){
        return userRepository.findAll();
    }
}
