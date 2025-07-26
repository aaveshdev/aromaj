package com.aromaj.interfaces;

import com.aromaj.Request;
import com.aromaj.Response;

@FunctionalInterface
public interface ErrorHandler {
    void handle(Exception err, Request req, Response res) throws Exception;
}
