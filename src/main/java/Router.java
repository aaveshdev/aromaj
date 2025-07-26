package com.aromaj;

import com.aromaj.interfaces.Handler;
import com.aromaj.utils.Next;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Router {
    private final List<Route> routes = new ArrayList<>();

    public Router get(String path, Handler... handlers) {
        return route("GET", path, handlers);
    }

    public Router post(String path, Handler... handlers) {
        return route("POST", path, handlers);
    }

    public Router put(String path, Handler... handlers) {
        return route("PUT", path, handlers);
    }

    public Router delete(String path, Handler... handlers) {
        return route("DELETE", path, handlers);
    }

    public Router all(String path, Handler... handlers) {
        return route("*", path, handlers);
    }

    public Router route(String method, String path, Handler... handlers) {
        routes.add(new Route(method, path, Arrays.asList(handlers)));
        return this;
    }

    public void handle(Request req, Response res, Next next) throws Exception {
        for (Route route : routes) {
            if (route.matches(req.method, req.path)) {
                route.extractParams(req);
                runHandlers(route.getHandlers(), req, res, 0, next);
                return;
            }
        }
        next.next(); 
    }

    private void runHandlers(List<Handler> handlers, Request req, Response res, int index, Next parentNext) throws Exception {
           if (index >= handlers.size()) {
        if (!res.isSent()) {
            parentNext.next(); 
        }
        return;
    }

        Handler current = handlers.get(index);
        current.handle(req, res, () -> runHandlers(handlers, req, res, index + 1, parentNext));
    }
}
