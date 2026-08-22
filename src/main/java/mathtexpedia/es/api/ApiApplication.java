package mathtexpedia.es.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class ApiApplication {

	public static void main(String[] args) throws IOException {
		loadDotEnv();
		SpringApplication.run(ApiApplication.class, args);
	}

	private static void loadDotEnv() throws IOException {
		Path envPath = Path.of(".env");
		if (!Files.exists(envPath)) {
			return;
		}
		List<String> lines = Files.readAllLines(envPath);
		for (String line : lines) {
			line = line.trim();
			if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
				continue;
			}
			int idx = line.indexOf('=');
			String key = line.substring(0, idx).trim();
			String value = line.substring(idx + 1).trim();
			System.setProperty(key, value);
		}
	}
}