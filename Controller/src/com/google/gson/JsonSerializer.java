package com.google.gson;

import java.lang.reflect.Type;

public interface JsonSerializer<T> {

    public JsonElement serialize(Class<?> src, Type typeOfSrc, JsonSerializationContext context);
}
