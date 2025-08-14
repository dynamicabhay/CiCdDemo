package com.as.CiCdDemo.exceptions;

public class KeyNotFoundException extends RuntimeException{
    public KeyNotFoundException(String key){
        super("key : " + key + " not found");
    }
}
