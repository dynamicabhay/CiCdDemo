package com.as.UrlShortner.exceptions;

public class AliasAlreadyTakenException extends RuntimeException {
    public AliasAlreadyTakenException(String alias){
        super("alias " + alias + " already taken :(");
    }
}
