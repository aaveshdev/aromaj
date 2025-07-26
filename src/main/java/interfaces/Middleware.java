package com.aromaj.interfaces;

import com.aromaj.Request;
import com.aromaj.Response;
import com.aromaj.utils.Next;

@FunctionalInterface
public interface Middleware {
    void apply(Request req, Response res, Next next) throws Exception;
}
