package com.as.CiCdDemo.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${fb.config.path}")
    public String fbConfigPath;

    @PostConstruct
    public void init() {
        try {
            String fileContent = new String(Files.readAllBytes(Paths.get(fbConfigPath)));
            log.info("===================== FIREBASE CONFIG FILE CONTENT =====================");
            log.info(fileContent);
            log.info("========================================================================");
            if (FirebaseApp.getApps().isEmpty()) {
                FileInputStream serviceAccount = new FileInputStream(fbConfigPath);
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();
                FirebaseApp.initializeApp(options);
            }
        }catch (Exception ex){
            log.error("!!!!!!!!!! CRITICAL ERROR INITIALIZING FIREBASE !!!!!!!!!", ex);
        }
    }
}
