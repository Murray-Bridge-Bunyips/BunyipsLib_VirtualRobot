package com.acmerobotics.dashboard.message;

//import com.google.gson.*;
//import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * Custom deserializer for dashboard messages.
 */
public class MessageDeserializer /*implements JsonDeserializer<Message>*/ {
//    @Override
//    public Message deserialize(JsonElement jsonElement, Type type,
//                               JsonDeserializationContext jsonDeserializationContext)
//        throws JsonParseException {
//        JsonObject messageObj = jsonElement.getAsJsonObject();
//        MessageType messageType = jsonDeserializationContext.deserialize(
//            messageObj.get("type"), MessageType.class);
//        if (messageType == null || messageType.msgClass == null) {
//            return null;
//        }
//        Type msgType = TypeToken.get(messageType.msgClass).getType();
//        return jsonDeserializationContext.deserialize(jsonElement, msgType);
//    }
}
