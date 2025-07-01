package com.as.CiCdDemo.model;


public class UserDTO {
    int id;
    int name;
    public UserDTO(int id,int name){
        this.id = id;
        this.name = name;
    }

    public String toString(){
        return id + " " + name;
    }
}
