package org.example.RapidJWeb.db;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;

import java.util.List;

public class Database {
    private static SessionFactory sessionFactory;

    private String url;

    public Database(String url) {
        this.url = url;
        getDatabaseFactory();
    }

    private void getDatabaseFactory() {
        if (sessionFactory == null) {
            try {
                Configuration configuration = new Configuration();

                configuration.setProperty("hibernate.connection.driver_class", "org.sqlite.JDBC");
                configuration.setProperty("hibernate.connection.url", "jdbc:sqlite:" + this.url + ".db");
                configuration.setProperty("hibernate.hbm2ddl.auto", "update");
                configuration.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect");

                configuration.addAnnotatedClass(org.example.Person.class);

                StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
                        .applySettings(configuration.getProperties());
                sessionFactory = configuration.buildSessionFactory(registryBuilder.build());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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

        // Retrieve all objects of the given entity class
        Query<E> query = session.createQuery("FROM " + entityClass.getSimpleName(), entityClass);
        List<E> resultList = query.list();

        transaction.commit();
        session.close();

        return resultList;
    }

    public static void shutdown() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }
}
