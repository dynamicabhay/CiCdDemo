package com.as.CiCdDemo.model;


public class UserDTO {
    int id;
    String name;
    public UserDTO(int id,String name){
        this.id = id;
        this.name = name;
    }

    public String toString(){
        return id + " " + name;
    }
}
