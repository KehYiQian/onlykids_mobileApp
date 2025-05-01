package com.example.onlykids_mobile_application;

import org.json.JSONException;
import org.json.JSONObject;

public class chatRoom_model {
    private final String message;
    private final String sender; // user or bot

    //constructor
    public chatRoom_model(String message, String sender) {
        this.message = message;
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("message", message);
        obj.put("sender", sender);
        return obj;
    }

    public static chatRoom_model fromJson(JSONObject obj) throws JSONException {
        return new chatRoom_model(obj.getString("message"), obj.getString("sender"));
    }
}

