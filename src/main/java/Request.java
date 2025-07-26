package com.aromaj;

import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Request {
    public String method;
    public String path;
    public Map<String, String> params = new HashMap<>();
    public Map<String, String> cookies = new HashMap<>();
    public HttpExchange exchange;

    public String sessionId;
    public Map<String, Object> session = new HashMap<>();

    private Map<String, String> queryParams;
    private Map<String, Object> bodyParams;

    public Request(HttpExchange exchange) {
        this.exchange = exchange;
        this.method = exchange.getRequestMethod();
        this.path = exchange.getRequestURI().getPath();
        parseCookies();
    }

    public void parseCookies() {
        String cookieHeader = exchange.getRequestHeaders().getFirst("Cookie");
        if (cookieHeader != null) {
            for (String cookie : cookieHeader.split(";")) {
                String[] parts = cookie.trim().split("=");
                if (parts.length == 2) cookies.put(parts[0], parts[1]);
            }
        }
    }

    public Map<String, String> query() {
        if (queryParams == null) {
            queryParams = new HashMap<>();
            String query = exchange.getRequestURI().getRawQuery();
            if (query != null) {
                for (String param : query.split("&")) {
                    String[] pair = param.split("=");
                    if (pair.length == 2) {
                        queryParams.put(decode(pair[0]), decode(pair[1]));
                    }
                }
            }
        }
        return queryParams;
    }

    public Map<String, Object> body() {
        if (bodyParams == null) {
            bodyParams = new HashMap<>();
            try (InputStream is = exchange.getRequestBody()) {
                String contentType = Optional.ofNullable(exchange.getRequestHeaders().getFirst("Content-Type")).orElse("");
                String bodyStr = new String(is.readAllBytes(), StandardCharsets.UTF_8);

                if (contentType.contains("application/json")) {
                    ObjectMapper mapper = new ObjectMapper();
                    bodyParams = mapper.readValue(bodyStr, Map.class);
                } else if (contentType.contains("application/x-www-form-urlencoded")) {
                    for (String pair : bodyStr.split("&")) {
                        String[] parts = pair.split("=");
                        if (parts.length == 2) {
                            bodyParams.put(decode(parts[0]), decode(parts[1]));
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bodyParams;
    }

    private String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return value;
        }
    }
}
