package com.aromaj;

import com.aromaj.interfaces.Handler;

import java.util.*;
import java.util.regex.*;

public class Route {
    private String method;
    private Pattern pathPattern;
    private List<String> paramNames = new ArrayList<>();
    private List<Handler> handlers;

    public Route(String method, String path, List<Handler> handlers) {
        this.method = method;
        this.handlers = handlers;
        Matcher matcher = Pattern.compile(":([a-zA-Z0-9_]+)").matcher(path);
        while (matcher.find()) paramNames.add(matcher.group(1));
        String regex = path.replaceAll(":([a-zA-Z0-9_]+)", "([^/]+)");
        this.pathPattern = Pattern.compile("^" + regex + "$");
    }

    public boolean matches(String reqMethod, String reqPath) {
        return method.equalsIgnoreCase(reqMethod) && pathPattern.matcher(reqPath).matches();
    }

    public void extractParams(Request req) {
        Matcher matcher = pathPattern.matcher(req.path);
        if (matcher.matches()) {
            for (int i = 0; i < paramNames.size(); i++) {
                req.params.put(paramNames.get(i), matcher.group(i + 1));
            }
        }
    }

    public List<Handler> getHandlers() {
        return handlers;
    }
}
