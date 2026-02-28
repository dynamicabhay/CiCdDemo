package com.as.UrlShortner.exceptions;

public class KeyNotFoundException extends RuntimeException{
    public KeyNotFoundException(String key){
        super("key : " + key + " not found");
    }
}
