package com.stanbic.internMs.intern.controller;

import com.stanbic.internMs.intern.dto.DtoMapper;
import com.stanbic.internMs.intern.dto.GenericDTO;
import com.stanbic.internMs.intern.dto.StandardAPIResponse;
import com.stanbic.internMs.intern.exception.ValidationException;
import com.stanbic.internMs.intern.models.User;
import com.stanbic.internMs.intern.service.UserService;
import com.stanbic.internMs.intern.utils.ValidationUtil;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/list")
    public ResponseEntity<StandardAPIResponse> all() {
        try {
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(userService.listUsers())
                            .message("Successfully fetched applications")
                            .successful(true)
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()

            );
        }
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<StandardAPIResponse> getUser(@PathVariable("id") @Min(value = 1, message = "Application id must be positive") Long id) {
        try {
            Optional<User> user = userService.findById(id);
            if (!user.isPresent()) {
                throw new ValidationException(Map.of("id", "User not found with id" + id));
            }
            return ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(user.get())
                            .message("Successfully retrieved user record")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve) {
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors().get("id"))
                            .successful(false)
                            .message("User record was not found. Try again with another ID")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<StandardAPIResponse> update(@PathVariable Long id, GenericDTO dto){
        try{
            Optional<User> user=userService.findById(id);
            if(!user.isPresent()){
                throw new ValidationException(Map.of("id", "user not found with id" + id));
            }
            ValidationUtil.validateProvidedFields(dto, User.class);
            DtoMapper.mapNonNullFields(dto, user.get()); //Only overwrite present fields
            return  ResponseEntity.status(200).body(
                    StandardAPIResponse.builder()
                            .data(userService.save(user.get()))
                            .message("Successfully updated user")
                            .successful(true)
                            .build()
            );
        } catch (ValidationException ve){
            return ResponseEntity.status(404).body(
                    StandardAPIResponse.builder()
                            .errors(ve.getErrors().get("id"))
                            .successful(false)
                            .message("User record was not found. Please try again with another ID")
                            .build()
            );
        } catch(Exception e){
            return ResponseEntity.status(500).body(
                    StandardAPIResponse.builder()
                            .errors(e.getMessage())
                            .successful(false)
                            .message("Something went wrong")
                            .build()
            );
        }

    }
}