package com.example.belajar.model;

public class User {
    public String username; //field
    private String password; //field
    public String role; //field

    public User(String username, String password, String role) { //constructor
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public void printPassword() {           //method
        System.out.println(password);
    }

    public boolean checkPassword(String password) { //method
        return this.password.equals(password);
    }
}

