package dev.artha.http;

import io.javalin.http.Context;

public class ResponseImpl implements Response {
    private final Context ctx;

    public ResponseImpl(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public Response status(int code) {
        ctx.status(code);
        return this;
    }

    @Override
    public Response json(Object obj) {
        ctx.json(obj);
        return this;
    }

    @Override
    public Response text(String text) {
        ctx.result(text);
        return this;
    }

    @Override
    public Response header(String key, String value) {
        ctx.header(key, value);
        return this;
    }
}
