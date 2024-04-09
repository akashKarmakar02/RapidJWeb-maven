package org.example.RapidJWeb.server;

import com.sun.net.httpserver.HttpServer;
import org.example.RapidJWeb.http.HttpRequest;
import org.example.RapidJWeb.http.HttpResponse;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

import static java.lang.System.out;


interface Callback {
    void function();
}

public class RapidWebServer {
    private final HttpServer server;
    private final int port;
    private final HashMap<String, RouteHandler> routeHandlerMap;
    private final ExecutorService executor;

    public RapidWebServer(int port) throws IOException {
        this.routeHandlerMap = new HashMap<>();
        this.server = HttpServer.create(new InetSocketAddress(port), 512);
        this.port = port;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public RapidWebServer(int port, int backlog) throws IOException {
        this.routeHandlerMap = new HashMap<>();
        this.server = HttpServer.create(new InetSocketAddress(port), backlog);
        this.port = port;
        this.executor = Executors.newVirtualThreadPerTaskExecutor();
    }

    public void get(String route, BiConsumer<HttpRequest, HttpResponse> handler) {
        if (routeHandlerMap.containsKey(route)) {
            routeHandlerMap.put(route, routeHandlerMap.get(route).get(handler));
        } else {
            routeHandlerMap.put(route, RouteHandler.create(route).get(handler));
        }
    }

    public void post(String route, BiConsumer<HttpRequest, HttpResponse> handler) {
        if (routeHandlerMap.containsKey(route)) {
            routeHandlerMap.put(route, routeHandlerMap.get(route).post(handler));
        } else {
            routeHandlerMap.put(route, RouteHandler.create(route).post(handler));
        }
    }

    public void run(Callback function) {
        routeHandlerMap.keySet().forEach((route) -> {
            server.createContext(route, routeHandlerMap.get(route));
        });

        server.setExecutor(executor);
        server.start();
        function.function();
    }

    public void run() {
        run(() -> out.println("Server is listening on: http://localhost:" + this.port));
    }
}

