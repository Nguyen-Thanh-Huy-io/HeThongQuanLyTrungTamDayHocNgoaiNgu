package vn.ute.db;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class Jpa {
    private static EntityManagerFactory emf;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            emf = Persistence.createEntityManagerFactory("ForeignLanguageCenterPU");
        }
        return emf;
    }

    public static void shutdown() {
        if (emf != null) {
            emf.close();
        }
    }
}
