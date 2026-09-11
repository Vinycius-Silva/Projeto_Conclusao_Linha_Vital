package com.linhavital.backend.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream

@Configuration
@ConditionalOnProperty(name = ["firebase.enabled"], havingValue = "true")
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        val credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS")
            ?: throw IllegalStateException(
                "FIREBASE_ENABLED=true exige GOOGLE_APPLICATION_CREDENTIALS"
            )

        val options = FileInputStream(credentialsPath).use { stream ->
            FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(stream))
                .build()
        }

        return if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options)
        } else {
            FirebaseApp.getInstance()
        }
    }
}
