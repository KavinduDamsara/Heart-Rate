package com.example.user.heartratemonitor;

public class UserDetails {
    public String firstName, lastName, gender;
    public int age;
    public float weight;

    public UserDetails(){

    }

    public UserDetails(String firstName, String lastName, String gender, int age, float weight) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.age = age;
        this.weight = weight;
    }
}
