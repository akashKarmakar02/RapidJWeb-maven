package org.example;

import org.example.RapidJWeb.db.Database;
import org.example.RapidJWeb.server.RapidWebServer;

import java.io.IOException;

import static java.lang.System.out;

public class Main {
    public static void main(String[] args) throws IOException {
        var server = new RapidWebServer(8000);
        var db = new Database("database");

        server.get("/", ((httpRequest, httpResponse) -> {
            var persons = db.getAll(Person.class);
            var data = new ResponseData(persons);


            httpResponse.render("index", data);
        }));

        server.post("/", ((httpRequest, httpResponse) -> {
            var body = httpRequest.getBody();
            var person = new Person((String) body.get("name"), Integer.parseInt((String) body.get("age")));
            
            db.save(person);

            httpResponse.redirect("/");
        }));

        server.run();
//        var arr = new int[]{};
//
//        out.println(arr instanceof Iterable);
    }
}