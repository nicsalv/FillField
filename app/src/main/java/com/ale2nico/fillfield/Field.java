import java.time.LocalTime;

/**
 * Rappresentazione di un campo
 */
public class Field {

    private Integer fieldId;

    private Position position;

    private Integer price;

    private String surface;

    // Numero di giocatori di una squadra.
    private Integer size;

    private LocalTime openingTime;

    private LocalTime closingTime;

    public Field(Integer fieldId, Position position, Integer price, String surface,
                 Integer size, LocalTime openingTime, LocalTime closingTime) {
        this.fieldId = fieldId;
        this.position = position;
        this.price = price;
        this.surface = surface;
        this.size = size;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
    }

    public Integer getFieldId() {
        return fieldId;
    }

    public Position getPosition() {
        return position;
    }

    public Integer getPrice() {
        return price;
    }

    public String getSurface() {
        return surface;
    }

    public Integer getSize() {
        return size;
    }

    public LocalTime getOpeningTime() {
        return openingTime;
    }

    public LocalTime getClosingTime() {
        return closingTime;
    }
}
