package com.as.UrlShortner.service;

import org.springframework.stereotype.Service;

@Service
public class Base62 {

    private String ALPHABHET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private int BASE = ALPHABHET.length();

    public String encode(long id){
        if(id == 0) return "0";
        StringBuilder sb = new StringBuilder();

        while(id > 0){
            sb.append(ALPHABHET.charAt((int)id%BASE));
            id = id / BASE;
        }

        return sb.reverse().toString();
    }

    public long decode(String key){
        long num = 0;
        for(int i=key.length()-1; i >= 0; i--){
            num += (long)Math.pow(BASE,key.length()-1-i)*ALPHABHET.indexOf(key.charAt(i));
        }
        return num;
    }

}
