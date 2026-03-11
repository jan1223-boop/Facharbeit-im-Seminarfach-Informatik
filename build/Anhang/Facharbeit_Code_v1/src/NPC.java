import java.util.ArrayList;
import java.util.List;





public class NPC {


    // --------------------- Hauptmethoden ---------------------
    // Karte auswählen (wird jede Runde aufgerufen)
    public Karte waehleKarte(
            List<Karte> hand,
            Karte ersteKarteImStich,
            Farbe trumpf,
            int gewonneneStiche,
            int angesagteStiche) {

        if (hand.isEmpty()) return null;

        if (ersteKarteImStich == null) {
            return entscheideAlsStartspieler(hand, trumpf, gewonneneStiche, angesagteStiche);
        } else {
            return entscheideAlsMitspieler(hand, ersteKarteImStich, trumpf, gewonneneStiche, angesagteStiche);
        }
    }



    // Stichansage berechnen (wird vor Rundenstart aufgerufen)
    public int berechneStichAnzahl(List<Karte> hand, Farbe trumpf) {
        double stiche = 0;

        for (Karte k : hand) {
            if (k.getFarbe() == trumpf) {
                // Hohe Trumpfkarte = sicherer Stich
                if (k.getWert() >= 10) stiche += 1;
                    // Mittlere Trumpfkarte = halber Stich
                else if (k.getWert() >= 7) stiche += 0.5;
            } else {
                // Hohe Nicht-Trumpf-Karte
                if (k.getWert() >= 12) stiche += 0.8;
                else if (k.getWert() >= 9) stiche += 0.5;
                else if (k.getWert() >= 7) stiche += 0.3;
                // Niedrige Karten → fast keine Chance
            }
        }

        return Math.max(0, (int)Math.round(stiche));
    }

    // --------------------- Startspieler-Logik ---------------------
    private Karte entscheideAlsStartspieler(
            List<Karte> hand,
            Farbe trumpf,
            int gewonneneStiche,
            int angesagteStiche) {

        if (gewonneneStiche < angesagteStiche) {
            Karte besteTrumpf = findeHoechsteTrumpf(hand, trumpf);
            if (besteTrumpf != null) return besteTrumpf;
            return findeHoechste(hand);
        } else {
            return findeNiedrigste(hand);
        }
    }

    // --------------------- Mitspieler-Logik ---------------------
    private Karte entscheideAlsMitspieler(
            List<Karte> hand,
            Karte ersteKarteImStich,
            Farbe trumpf,
            int gewonneneStiche,
            int angesagteStiche) {

        Farbe angespielteFarbe = ersteKarteImStich.getFarbe();

        // Karten, die bedienen können
        List<Karte> passendeKarten = new ArrayList<>();
        for (Karte k : hand) {
            if (k.getFarbe() == angespielteFarbe) {
                passendeKarten.add(k);
            }
        }

        if (!passendeKarten.isEmpty()) {
            if (gewonneneStiche < angesagteStiche) {
                return findeHoechste(passendeKarten);
            } else {
                return findeNiedrigste(passendeKarten);
            }
        }

        // Kann nicht bedienen → Trumpf prüfen
        List<Karte> trumfKarten = new ArrayList<>();
        for (Karte k : hand) {
            if (k.getFarbe() == trumpf) trumfKarten.add(k);
        }

        if (!trumfKarten.isEmpty() && gewonneneStiche < angesagteStiche) {
            return findeNiedrigste(trumfKarten);
        }

        return findeNiedrigste(hand);
    }

    // --------------------- Hilfsmethoden ---------------------
    private Karte findeHoechste(List<Karte> karten) {
        Karte beste = karten.get(0);
        for (Karte k : karten) {
            if (k.getWert() > beste.getWert()) beste = k;
        }
        return beste;
    }

    private Karte findeNiedrigste(List<Karte> karten) {
        Karte schlechteste = karten.get(0);
        for (Karte k : karten) {
            if (k.getWert() < schlechteste.getWert()) schlechteste = k;
        }
        return schlechteste;
    }

    private Karte findeHoechsteTrumpf(List<Karte> hand, Farbe trumpf) {
        Karte beste = null;
        for (Karte k : hand) {
            if (k.getFarbe() == trumpf) {
                if (beste == null || k.getWert() > beste.getWert()) beste = k;
            }
        }
        return beste;
    }
}

