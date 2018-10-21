package com.revolut.task.utils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 * Created by Alexey on 20.10.2018.
 *
 * This class is used for Java SE environment
 */
public class JpaUtil {

    private static final String PERSISTENT_UNIT_NAME = "h2em";
    private static final EntityManagerFactory emf;

    private JpaUtil(){

    }

    static {
        try {
            emf = Persistence.createEntityManagerFactory(PERSISTENT_UNIT_NAME);
        } catch (Throwable ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static EntityManager getEm() {
        return emf.createEntityManager();
    }

}
