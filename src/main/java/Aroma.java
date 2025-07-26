package com.aromaj;

import com.sun.net.httpserver.HttpServer;
import com.aromaj.interfaces.Handler;
import com.aromaj.interfaces.Middleware;
import com.aromaj.interfaces.ErrorHandler;
import com.aromaj.utils.Next;
import com.aromaj.Router;

import java.net.InetSocketAddress;
import java.util.*;
import java.io.IOException;
import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Aroma {
    private List<Middleware> middlewares = new ArrayList<>();
    private List<Route> routes = new ArrayList<>();

    private Map<String, Map<String, Object>> sessions = new ConcurrentHashMap<>();

    public Aroma get(String path, Handler... handlers) {
        return route("GET", path, handlers);
    }

    public Aroma post(String path, Handler... handlers) {
        return route("POST", path, handlers);
    }

       public Aroma put(String path, Handler... handlers) {
        return route("PUT", path, handlers);
    }

    public Aroma delete(String path, Handler... handlers) {
        return route("DELETE", path, handlers);
    }

    public Aroma all(String path, Handler... handlers) {
        return route("*", path, handlers);
    }

    public Aroma route(String method, String path, Handler... handlers) {
        routes.add(new Route(method, path, Arrays.asList(handlers)));
        return this;
    }

    public Aroma use(Middleware middleware) {
        this.middlewares.add(middleware);
        return this;
    }


    public Aroma staticFiles(String folder) {
        File root = new File(folder).getAbsoluteFile();
        this.use((req, res, next) -> {
            File file = new File(root, req.path);
            if (file.exists() && file.isFile()) {
                byte[] content = Files.readAllBytes(file.toPath());
                String mime = Files.probeContentType(file.toPath());
                res.status(200);
                res.exchange.getResponseHeaders().add("Content-Type", mime);
                res.exchange.sendResponseHeaders(200, content.length);
                OutputStream os = res.exchange.getResponseBody();
                os.write(content);
                os.close();
            } else {
                next.next();
            }
        });
        return this;
    }

    public Aroma useCookies() {
        return this.use((req, res, next) -> {
            req.parseCookies();
            next.next();
        });
    }


    public Aroma use(String path, Router router) {
    this.use((req, res, next) -> {
        if (req.path.startsWith(path)) {
            String subPath = req.path.substring(path.length());
            req.path = subPath.isEmpty() ? "/" : subPath;
            router.handle(req, res, next);
        } else {
            next.next();
        }
    });
    return this;
}

    public void listen(int port, Runnable onStart) {
    try {
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0",port), 0);
        server.createContext("/", exchange -> {
            exchange.getResponseHeaders().add("X-Powered-By", "AromaJ/1.0");
            Request req = new Request(exchange);
            Response res = new Response(exchange);
            req.method = exchange.getRequestMethod();
            req.path = exchange.getRequestURI().getPath();

            String sessionId = req.cookies.get("SESSION_ID");
            if (sessionId == null || !sessions.containsKey(sessionId)) {
                sessionId = UUID.randomUUID().toString();
                sessions.put(sessionId, new HashMap<>());
            }
            req.sessionId = sessionId;
            req.session = sessions.get(sessionId);

            res.setCookie("SESSION_ID", sessionId);

            runMiddlewares(req, res, 0, () -> {
                for (Route route : routes) {
                    if (route.matches(req.method, req.path)) {
                        route.extractParams(req);
                        try {
                            runHandlers(route.getHandlers(), req, res, 0);
                        } catch (Exception e) {
                            e.printStackTrace();
                            try {
                                res.status(500).send("Internal Server Error");
                            } catch (IOException ioException) {
                                ioException.printStackTrace();
                            }
                        }

                        return;
                    }
                }
          
                try {
                    res.status(404).send("404 Not Found");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            });
        });
        server.setExecutor(null);
        server.start();
        onStart.run();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


    private void runHandlers(List<Handler> handlers, Request req, Response res, int index) throws Exception {
        if (index >= handlers.size()) return;
        handlers.get(index).handle(req, res, () -> runHandlers(handlers, req, res, index + 1));
    }

    private void runMiddlewares(Request req, Response res, int index, Runnable onDone) {
    if (index >= middlewares.size()) {
        onDone.run();
        return;
    }
    try {
        middlewares.get(index).apply(req, res, () -> runMiddlewares(req, res, index + 1, onDone));
    } catch (Exception e) {
        e.printStackTrace();
        try {
            res.status(500).send("Middleware Error");
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

    }
}

}
