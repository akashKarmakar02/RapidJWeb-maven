package com.neo.RapidJWeb.db;

import jakarta.persistence.Entity;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private SessionFactory sessionFactory;

    private List<Class<?>> getClassesWithAnnotation(String packageName) {
        List<Class<?>> classes = new ArrayList<>();

        try {
            // Get all classes in the package
            ClassPathScanner classPathScanner = new ClassPathScanner();
            List<Class<?>> allClasses = classPathScanner.getClasses(packageName);

            // Check each class for the annotation
            for (Class<?> clazz : allClasses) {
                if (clazz.isAnnotationPresent(Entity.class)) {
                    classes.add(clazz);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classes;
    }

    private List<Class<?>> getClasses(String packageName) {

        return getClassesWithAnnotation(packageName);
    }

    public Database() {
        String packageName = "";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length > 2) {
            String callerClassName = stackTrace[2].getClassName();
            try {
                packageName = Class.forName(callerClassName).getPackage().getName();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            packageName = getClass().getPackage().getName();
        }

        var classes = getClasses(packageName);

        if (sessionFactory == null) {
            try {
                Configuration configuration = getConfiguration();

                for (var dbClass: classes) {
                    configuration.addAnnotatedClass(dbClass);
                }

                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(registryBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Configuration getConfiguration() {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
        configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:database.db");
        configuration.setProperty("hibernate.hbm2ddl.auto", "update");
        configuration.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");

        return configuration;
    }

    public void save(Object entity) {
        var session = sessionFactory.openSession();
        session.beginTransaction();

        session.save(entity);

        session.getTransaction().commit();

        session.close();
    }

    public <E> List<E> getAll(Class<E> entityClass) {
        var session = sessionFactory.openSession();
        var transaction = session.beginTransaction();

        Query<E> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
        List<E> resultList = query.list();

        transaction.commit();
        session.close();

        return resultList;
    }

    public <E> E getById(Class<E> entityClass, Serializable id) {
        var session = sessionFactory.openSession();
        var transaction = session.beginTransaction();

        E entity = session.get(entityClass, id);

        transaction.commit();
        session.close();

        return entity;
    }

    public void delete(Object entity) {
        var session = sessionFactory.openSession();
        var transaction = session.beginTransaction();

        session.delete(entity);

        transaction.commit();
        session.close();
    }

    public <E> QueryBuilder<E> get(Class<E> entityClass) {
        return new QueryBuilder<>(entityClass, sessionFactory);
    }

    public void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    public static class QueryBuilder<E> {
        private final Class<E> entityClass;
        private final SessionFactory sessionFactory;
        private final StringBuilder whereClause = new StringBuilder();

        public QueryBuilder(Class<E> entityClass, SessionFactory sessionFactory) {
            this.entityClass = entityClass;
            this.sessionFactory = sessionFactory;
        }

        public QueryBuilder<E> where(String property) {
            whereClause.append(" WHERE ").append(property);
            return this;
        }

        public QueryBuilder<E> greaterThan(Object value) {
            whereClause.append(" > :value");
            return this;
        }

        public List<E> list() {
            var session = sessionFactory.openSession();
            var transaction = session.beginTransaction();

            Query<E> query = session.createQuery("FROM " + entityClass.getSimpleName() + whereClause.toString(), entityClass);
            // Set parameters if any
             query.setParameter("value", 17);

            List<E> resultList = query.list();

            transaction.commit();
            session.close();

            return resultList;
        }
    }
}
