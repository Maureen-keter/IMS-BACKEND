package com.stanbic.internMs.intern.service;

import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository){
        this.userRepository=userRepository;
    }

    public List<User>listUsers(){
        return userRepository.findAll();
    }

    public Page<User> listUsers(Pageable pageable){
        return userRepository.findAll(pageable);
    }
    public Page<User>listUsers(Pageable pageable,String search){
        if(search ==null || search.isBlank()){
            return userRepository.findAll(pageable);
        }
        return userRepository.findByNameContainingIgnoreCase(search,pageable);
    }

    public User update(Long id, User user){
        return userRepository.findById(id)
                .map(existing->{
                    existing.setEmail(user.getEmail());
                    existing.setFullName(user.getFullName());
                    existing.setPassword(user.getPhoneNumber());
                    return userRepository.save(existing);
        })
                .orElseThrow(()-> new RuntimeException("User not found with id: " +id));
    }

    public Optional<User> findById(Long id){
        return userRepository.findById(id);
    }

    public Optional<User> findByUserID(String userID){
        return userRepository.findByUserID(userID);
    }

    public User createUser(User user){
        return userRepository.save(user);
    }

    public User save(User user){
        return userRepository.save(user);
    }

    public void delete(Long id){
        userRepository.deleteById(id);
    }
}
