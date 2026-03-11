public class Karte {
    private Farbe farbe;  // Enum oder Klasse Farbe
    private int wert;

    public Karte(Farbe farbe, int wert) {
        this.farbe = farbe;
        this.wert = wert;
    }

    // Getter für die KI
    public Farbe getFarbe() {
        return farbe;
    }

    public int getWert() {
        return wert;
    }
}