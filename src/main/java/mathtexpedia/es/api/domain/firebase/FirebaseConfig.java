package mathtexpedia.es.api.domain.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FirebaseConfig {

    @Value("${firebase.credentials.path}")
    private Resource firebaseCredentials;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        FirebaseOptions opt = FirebaseOptions.builder().setCredentials(GoogleCredentials.fromStream(firebaseCredentials.getInputStream())).build();

        if (FirebaseApp.getApps().isEmpty()) {
            return FirebaseApp.initializeApp(opt);
        }
        return FirebaseApp.getInstance();
    }
}
