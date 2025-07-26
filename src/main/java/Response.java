package com.aromaj;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Response {
    public HttpExchange exchange;
    private int statusCode = 200;
    private static final ObjectMapper mapper = new ObjectMapper();

    private boolean sent = false;



    public Response(HttpExchange exchange) {
        this.exchange = exchange;
    }

    public boolean isSent() {
        return sent;
    }

    public Response status(int code) {
        this.statusCode = code;
        return this;
    }

    public void send(String message) throws IOException {
        if (sent) return;
        sent = true;

        if (message == null) {
            message = ""; 
        }
        byte[] bytes = message.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public void json(Object obj) throws IOException {
        String json = mapper.writeValueAsString(obj);
        byte[] bytes = json.getBytes();
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    public void setCookie(String name, String value) {
        exchange.getResponseHeaders().add("Set-Cookie", name + "=" + value + "; Path=/; HttpOnly");
    }

    public void redirect(String url) throws IOException {
        redirect(302, url);
    }

    public void redirect(int statusCode, String url) throws IOException {
        exchange.getResponseHeaders().add("Location", url);
        exchange.sendResponseHeaders(statusCode, -1); 
        exchange.getResponseBody().close();
    }
}
