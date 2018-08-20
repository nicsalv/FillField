package com.ale2nico.fillfield.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Rappresentazione dell'oggeto utente all'interno dell'applicazione.
 */

@IgnoreExtraProperties
public class User {

    private String email;
    private String name;
    private String surname;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    /**
     * costruttore con parametri
     * @param email email dell'utente
     * @param name nome dell'utente
     * @param surname cognome dell'utente
     */
    public User(String email, String name, String surname){
        this.email = email;
        this.name = name;
        this.surname = surname;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("name", name);
        result.put("surname", surname);

        return result;
    }

}
