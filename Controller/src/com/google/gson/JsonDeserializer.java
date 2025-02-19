package com.google.gson;

import java.lang.reflect.Type;

public interface JsonDeserializer<T> {
    public Class<?> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException;
}
