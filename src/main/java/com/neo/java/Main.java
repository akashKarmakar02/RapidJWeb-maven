package com.neo.java;

import com.neo.RapidJWeb.db.Database;

import static java.lang.System.out;

public class Main {

    public static void main(String[] args) {
        var db = new Database();

        var adults = db.get(Person.class)
                        .where("age")
                        .greaterThan(18)
                        .list();
        out.println(adults);
    }
}
