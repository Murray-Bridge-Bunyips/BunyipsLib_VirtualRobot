package com.google.gson;

public class GsonBuilder {
    public GsonBuilder registerTypeAdapter(Object a, Object b) {
        return this;
    }
    public GsonBuilder setPrettyPrinting() {
        return this;
    }
    public Gson create() {
        return new Gson();
    }
}
