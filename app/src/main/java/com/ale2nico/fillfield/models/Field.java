package com.ale2nico.fillfield.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a soccer field.
 */
@IgnoreExtraProperties
public class Field {

    private String userId;
    private String position;
    private String name;

    public Field() {
        // Default constructor required for calls to DataSnapshot.getValue(Field.class)
    }

    public Field(String userId, String position, String name) {
        this.userId = userId;
        this.position = position;
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public String getPosition() {
        return position;
    }

    public String getName() {
        return name;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("userId", userId);
        result.put("position", position);
        result.put("name", name);

        return result;
    }
}
