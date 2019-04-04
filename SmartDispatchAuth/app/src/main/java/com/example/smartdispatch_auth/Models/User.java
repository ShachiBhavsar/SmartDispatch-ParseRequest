package com.example.smartdispatch_auth.Models;

import android.os.Parcelable;

public class User {

    private String name, email, user_id, aadhar_number, phone_number;

    public User(String name, String email, String user_id, String aadhar_number, String phone_number){
        this.name = name;
        this.email = email;
        this.user_id = user_id;
        this.aadhar_number = aadhar_number;
        this.phone_number = phone_number;
    }

    public User(){

    }

    public String getEmail() {
        return email;
    }

    public String getName(){
        return name;
    }

    public String getUser_id() {
        return user_id;
    }

    public String getAadhar_number() {
        return aadhar_number;
    }

    public String getPhone_number() {
        return phone_number;
    }

}
