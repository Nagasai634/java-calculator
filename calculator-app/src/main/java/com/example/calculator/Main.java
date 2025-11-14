package com.example.calculator;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) throws Exception {
        Calculator calc = new Calculator();

        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        System.out.println("Calculator service started on port " + port);

        server.createContext("/calc", (HttpExchange exchange) -> {

            if (!exchange.getRequestMethod().equals("GET")) {
                sendResponse(exchange, 405, "Only GET allowed");
                return;
            }

            Map<String, String> query = parseQuery(exchange.getRequestURI().getQuery());

            try {
                String op = query.get("op");
                double a = Double.parseDouble(query.get("a"));
                double b = Double.parseDouble(query.get("b"));

                double result;

                switch (op) {
                    case "add": result = calc.add(a, b); break;
                    case "sub": result = calc.subtract(a, b); break;
                    case "mul": result = calc.multiply(a, b); break;
                    case "div": result = calc.divide(a, b); break;
                    default:
                        sendResponse(exchange, 400, "Invalid operator");
                        return;
                }

                sendResponse(exchange, 200, "Result = " + result);

            } catch (Exception e) {
                sendResponse(exchange, 400, "Invalid query params");
            }
        });

        server.start();

        // 🔥 Keeps the container alive
        Thread.currentThread().join();
    }

    private static Map<String, String> parseQuery(String query) {
        if (query == null) return Map.of();
        return Arrays.stream(query.split("&"))
                .map(s -> s.split("="))
                .collect(Collectors.toMap(a -> a[0], a -> a[1]));
    }

    private static void sendResponse(HttpExchange ex, int status, String text) throws IOException {
        ex.sendResponseHeaders(status, text.length());
        OutputStream os = ex.getResponseBody();
        os.write(text.getBytes());
        os.close();
    }
}

