/**
 * Rappresentazione dell'oggeto utente all'interno dell'applicazione.
 */

public class User {

    private final String email;

    private final String name;

    private final String surname;

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

}
