package com.as.CiCdDemo.repository;

import com.as.CiCdDemo.model.UrlMappings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMappings,Long> {
    Optional<UrlMappings> findByShortKey(String shortKey);

    @Modifying
    @Query("UPDATE UrlMappings u SET u.visitCount = u.visitCount + 1 WHERE u.shortKey = :shortKey")
    int incrementVisitCount(@Param("shortKey") String shortKey);
}
