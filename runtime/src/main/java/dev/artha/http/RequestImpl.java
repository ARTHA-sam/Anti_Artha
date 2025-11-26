package dev.artha.http;

import io.javalin.http.Context;
import java.util.Map;

public class RequestImpl implements Request {
    private final Context ctx;

    public RequestImpl(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    public String query(String key) {
        return ctx.queryParam(key);
    }

    @Override
    public String query(String key, String defaultValue) {
        String value = ctx.queryParam(key);
        return value != null ? value : defaultValue;
    }

    @Override
    public String path(String key) {
        return ctx.pathParam(key);
    }

    @Override
    public <T> T body(Class<T> clazz) {
        return ctx.bodyAsClass(clazz);
    }

    @Override
    public String bodyAsString() {
        return ctx.body();
    }

    @Override
    public String header(String key) {
        return ctx.header(key);
    }

    @Override
    public String method() {
        return ctx.method().toString();
    }

    @Override
    public String url() {
        return ctx.url();
    }
}
