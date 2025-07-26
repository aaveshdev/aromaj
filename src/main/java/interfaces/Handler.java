package com.aromaj.interfaces;

import com.aromaj.Request;
import com.aromaj.Response;
import com.aromaj.utils.Next;

@FunctionalInterface
public interface Handler {
    void handle(Request req, Response res, Next next) throws Exception;
}
