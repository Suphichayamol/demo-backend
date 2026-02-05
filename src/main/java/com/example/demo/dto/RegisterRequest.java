package com.example.demo.dto;

import com.example.demo.validation.ValidPassword;

import javax.validation.constraints.NotBlank;

public class RegisterRequest {

    @NotBlank
    private String username;

    @ValidPassword
    private String password;

    // getter / setter
    public String getUsername() { 
        return username; 
    }
    public void setUsername(String username) { 
        this.username = username; 
    }

    public String getPassword() { 
        return password; 
    }

    public void setPassword(String password) { 
        this.password = password; 
    }
}