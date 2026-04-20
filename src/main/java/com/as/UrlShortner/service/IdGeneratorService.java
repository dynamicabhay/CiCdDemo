package com.as.UrlShortner.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Service
@Slf4j
public class IdGeneratorService {
    private JdbcTemplate jdbcTemplate;
    @Value("${id.range.increment.window.size}")
    private long idRangeincrementWindowSize;
    @Value("${id.range.increment.enabled:true}")
    private boolean idRangeIncrementEnabled;
    private AtomicLong currentId;
    private volatile long maxId;
    //private String RANGE_UPDATE_QUERY = "UPDATE ID_RANGE_ALLOCATOR SET=CURRENT_MAX_ID+" + idRangeincrementWindowSize + " RETURNING CURRENT_MAX_ID ";

    public IdGeneratorService(JdbcTemplate template){
        this.jdbcTemplate = template;
        this.currentId = new AtomicLong();
    }

    @PostConstruct
    public void fetchIdRange(){
        try {
            //System.out.println("idRangeIncrementEnabled: " + idRangeIncrementEnabled);
            if (idRangeIncrementEnabled) {
                String sql = "UPDATE ID_RANGE_ALLOCATOR SET CURRENT_MAX_ID = CURRENT_MAX_ID + ? RETURNING CURRENT_MAX_ID";
                this.maxId = jdbcTemplate.queryForObject(sql, Long.class, idRangeincrementWindowSize);
            } else {
                String sql = "SELECT CURRENT_MAX_ID FROM ID_RANGE_ALLOCATOR";
                this.maxId = jdbcTemplate.queryForObject(sql, Long.class);
            }
            this.currentId.set(maxId - idRangeincrementWindowSize + 1);
            //log.info("maxId & currentId are set: " + maxId + ", " + currentId.get());
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }

    }

    public long getNextid(){

        if(currentId.get() > maxId){
            synchronized (this){
                if(currentId.get() > maxId){
                    if (!idRangeIncrementEnabled) {
                        throw new IllegalStateException("ID range exhausted while id.range.increment.enabled=false");
                    }
                    fetchIdRange();
                }
            }
        }
        return currentId.getAndIncrement();
    }

}
