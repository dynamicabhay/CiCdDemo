package com.as.CiCdDemo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimiterNonAuthenticatedService {

    private Map<String, AtomicInteger> requests = new HashMap<>();

    @Value("${total.api.request.allowed}")
    public int totalRequests;

    public boolean isAllowed(String key){
        AtomicInteger currReqs = requests.getOrDefault(key,new AtomicInteger(0));
       // System.out.println(totalRequests);
      //  System.out.println("current request count for key " + key + " " + currReqs.get());

          if(currReqs.get() < totalRequests){
              currReqs.incrementAndGet();
              requests.put(key,currReqs);
            } else return false;

          return true;

    }

    public void flush(){
        requests.clear();
    }
}
