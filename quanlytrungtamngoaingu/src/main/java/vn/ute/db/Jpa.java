package vn.ute.db;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class Jpa {
    private static EntityManagerFactory emf;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("ForeignLanguageCenterPU", buildOverrides());
        }
        return emf;
    }

    private static Map<String, Object> buildOverrides() {
        Map<String, Object> overrides = new HashMap<>();
        putIfPresent(overrides, "jakarta.persistence.jdbc.url", "DB_URL", "db.url");
        putIfPresent(overrides, "jakarta.persistence.jdbc.user", "DB_USER", "db.user");
        putIfPresent(overrides, "jakarta.persistence.jdbc.password", "DB_PASSWORD", "db.password");
        return overrides;
    }

    private static void putIfPresent(Map<String, Object> overrides, String jpaKey, String envKey, String systemPropertyKey) {
        String value = System.getenv(envKey);
        if (value == null || value.isBlank()) {
            value = System.getProperty(systemPropertyKey);
        }
        if (value != null && !value.isBlank()) {
            overrides.put(jpaKey, value.trim());
        }
    }

    public static void shutdown() {
        if (emf != null) {
            emf.close();
        }
    }
}
