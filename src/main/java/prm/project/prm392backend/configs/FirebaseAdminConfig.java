package prm.project.prm392backend.configs;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;

@Configuration
public class FirebaseAdminConfig {

    @PostConstruct
    public void init() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) return;

        String saPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (saPath == null || saPath.isBlank()) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS is not set");
        }

        try (var in = new FileInputStream(saPath)) {
            GoogleCredentials creds = GoogleCredentials.fromStream(in);

            // KHÔNG set projectId cứng; Admin SDK sẽ đọc project_id từ file JSON
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(creds)
                    .build();

            FirebaseApp.initializeApp(options);
        }

        var fs = FirestoreClient.getFirestore();
        System.out.println("[Firebase] project=" + fs.getOptions().getProjectId());
    }
}
