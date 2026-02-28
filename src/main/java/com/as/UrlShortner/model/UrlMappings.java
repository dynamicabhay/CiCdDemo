package com.as.UrlShortner.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "urls")
public class UrlMappings {

    @Id
    private long id;

    @Column(unique = true,nullable = false,length = 7)
    private String shortKey;
    @Column(nullable = false)
    private String originalUrl;
    @Column(nullable = false)
    private Instant createdAt;
    @Column
    private Instant expiresAt;
    @Column
    private long visitCount;

    public UrlMappings(String originalUrl,Instant createdAt, String shortKey,long id){
        this.originalUrl = originalUrl;
        this.shortKey = shortKey;
        this.createdAt = createdAt;
        this.id = id;
    }

}
