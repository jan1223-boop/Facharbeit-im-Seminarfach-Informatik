import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NPC {

    public Karte waehleKarte(
            List<Karte> hand,
            Farbe angespielteFarbe,
            Farbe trumpf,
            int eigeneStiche,
            int angesagteStiche,
            Karte aktuelleHoechsteImStich) {

        // Grundlegende Bedarfsprüfung
        int sichereRestStiche = schaetzeSichereStiche(hand, trumpf);
        boolean brauchtStiche = eigeneStiche + sichereRestStiche <= angesagteStiche;
        boolean nurNochEinStichBenoetigt = (angesagteStiche - eigeneStiche) == 1;

        // --- START DES FLUSSDIAGRAMMS ---

        // Startspieler? [cite: 75]
        if (angespielteFarbe == null) {
            if (brauchtStiche) { // [cite: 76]
                if (nurNochEinStichBenoetigt) { // [cite: 72]
                    if (hatFarbe(hand, trumpf)) { // [cite: 71]
                        return findeHoechste(filterFarbe(hand, trumpf)); // Spiele höchste Trumpf [cite: 69]
                    } else {
                        return staerksteNichtFrueherTrumpf(hand, trumpf); // Spiele höchste nicht Trumpf [cite: 90]
                    }
                } else {
                    if (istNurTrumpf(hand, trumpf)) { // [cite: 79]
                        return findeHoechste(hand); // Spiele höchste Trumpf [cite: 91]
                    } else {
                        return staerksteNichtFrueherTrumpf(hand, trumpf); // Spiele höchste nicht Trumpf [cite: 86]
                    }
                }
            } else { // Brauche Stiche? Nein [cite: 78]
                if (istNurTrumpf(hand, trumpf)) {
                    return findeNiedrigste(hand); // Spiele niedrigste Trumpf [cite: 84]
                } else {
                    return findeNiedrigste(filterNichtFarbe(hand, trumpf)); // Spiele niedrigste nicht Trumpf [cite: 92]
                }
            }
        }

        // --- NACHFOLGENDE SPIELER ---
        List<Karte> passende = filterFarbe(hand, angespielteFarbe);

        if (!passende.isEmpty()) { // Kann bedienen? Ja [cite: 101]
            if (brauchtStiche) {
                if (nurNochEinStichBenoetigt) {
                    if (hatFarbe(hand, trumpf)) {
                        if (kannGewinnen(passende, aktuelleHoechsteImStich)) {
                            return findeHoechste(passende); // Spiele höchste gewinnende [cite: 93]
                        } else {
                            if (filterWertGroesser(passende, 10).size() > 1) { // [cite: 118]
                                return findeZweitHoechste(passende); // Spiele 2. höchste [cite: 115]
                            } else {
                                return findeNiedrigste(passende); // Spiele niedrigste [cite: 123]
                            }
                        }
                    } else {
                        return findeHoechste(passende); // Spiele höchste gewinnende [cite: 108]
                    }
                } else {
                    return findeNiedrigsteGewinnende(passende, aktuelleHoechsteImStich); //
                }
            } else { // Brauche Stiche? Nein [cite: 95]
                if (istNurTrumpf(hand, trumpf)) {
                    return findeNiedrigste(hand); // [cite: 119]
                } else {
                    if (kannVerlieren(passende, aktuelleHoechsteImStich)) { // [cite: 96]
                        return findeHoechsteVerlierende(passende, aktuelleHoechsteImStich); // [cite: 130]
                    } else {
                        return findeHoechste(passende); // Spiele höchste [cite: 142]
                    }
                }
            }
        } else { // Kann NICHT bedienen
            if (brauchtStiche) {
                if (nurNochEinStichBenoetigt) {
                    return findeHoechsteGewinnende(hand, aktuelleHoechsteImStich, trumpf);
                } else {
                    if (kannOhneTrumpfGewinnen(hand, aktuelleHoechsteImStich, trumpf)) { // [cite: 132]
                        return findeNiedrigsteGewinnendeNichtTrumpf(hand, aktuelleHoechsteImStich, trumpf); // [cite: 136]
                    } else if (kannMitTrumpfGewinnen(hand, aktuelleHoechsteImStich, trumpf)) { // [cite: 138]
                        return findeNiedrigsteGewinnendeTrumpf(hand, aktuelleHoechsteImStich, trumpf); // [cite: 143]
                    } else {
                        return findeHoechsteNichtGewinnende(hand, aktuelleHoechsteImStich, trumpf); // [cite: 140]
                    }
                }
            } else { // Brauche Stiche? Nein
                if (kannVerlieren(hand, aktuelleHoechsteImStich)) { // [cite: 128]
                    if (kannMitTrumpfVerlieren(hand, aktuelleHoechsteImStich, trumpf)) { // [cite: 133]
                        return findeHoechsteVerlierendeTrumpf(hand, aktuelleHoechsteImStich, trumpf); // [cite: 139]
                    } else {
                        return findeHoechsteVerlierendeNichtTrumpf(hand, aktuelleHoechsteImStich, trumpf); // [cite: 135]
                    }
                } else {
                    if (hatFarbe(hand, trumpf)) {
                        return findeHoechste(filterFarbe(hand, trumpf)); // [cite: 148]
                    } else {
                        return findeNiedrigste(hand); // [cite: 146]
                    }
                }
            }
        }
    }

    public int berechneStichAnzahl(List<Karte> hand, Farbe trumpf, int r, int spielerAnsage) {

        double stiche = 0;

        // falls in der ersten Runde ein Trumpf auf der Hand ist, wird immer 1 Stich angesagt
        if(r==1 && hand.getFirst().getFarbe() == trumpf){
            return 1;
        }

        int trumpfCount = 0;
        int[] farben = new int[4];

        for (Karte k : hand) {

            if (k.getFarbe() == trumpf) {

                trumpfCount++;

                if (k.getWert() >= 13) stiche += 1.1;
                else if (k.getWert() >= 11) stiche += 0.9;
                else if (k.getWert() >= 9) stiche += 0.7;
                else if (k.getWert() >= 7) stiche += 0.5;

            } else {

                if (k.getWert() >= 13) stiche += 1.0;
                else if (k.getWert() >= 11) stiche += 0.7;
                else if (k.getWert() >= 9) stiche += 0.4;
                else if (k.getWert() >= 7) stiche += 0.2;
            }

            farben[k.getFarbe().ordinal()]++;
        }

        // Trumpfbonus
        stiche += trumpfCount * 0.25;

        if (trumpfCount >= 3) stiche += 0.5;
        if (trumpfCount >= 4) stiche += 0.8;

        // frühe Runden
        if (r <= 4) {
            stiche += trumpfCount * 0.2;
        }

        // Farbkontrolle
        for (int f : farben) {
            if (f >= 2) stiche += 0.3;
            if (f >= 3) stiche += 0.2;
        }

        // Sicherheitsfaktor
        stiche *= 0.9;

        int ansage = (int)Math.round(stiche);


        // wenn Spieler viele Stiche ansagt → KI vorsichtiger
        if (spielerAnsage > r / 2) {
            ansage = (int)Math.floor(ansage * 0.8);
        }

        // wenn Spieler wenig ansagt → KI etwas aggressiver
        if (spielerAnsage <= r / 3) {
            ansage = (int)Math.ceil(ansage * 1.2);
        }



        while(ansage>r){
            ansage--;
        }

        return Math.max(0, ansage);
    }

    // --- HILFSMETHODEN FÜR DIE LOGIK ---

    private boolean hatFarbe(List<Karte> hand, Farbe f) {
        return hand.stream().anyMatch(k -> k.getFarbe() == f);
    }

    private boolean istNurTrumpf(List<Karte> hand, Farbe trumpf) {
        return hand.stream().allMatch(k -> k.getFarbe() == trumpf);
    }

    private List<Karte> filterFarbe(List<Karte> hand, Farbe f) {
        return hand.stream().filter(k -> k.getFarbe() == f).collect(Collectors.toList());
    }

    private List<Karte> filterNichtFarbe(List<Karte> hand, Farbe f) {
        return hand.stream().filter(k -> k.getFarbe() != f).collect(Collectors.toList());
    }

    private List<Karte> filterWertGroesser(List<Karte> karten, int wert) {
        return karten.stream().filter(k -> k.getWert() > wert).collect(Collectors.toList());
    }

    private Karte findeHoechste(List<Karte> karten) {
        return karten.stream().max((k1, k2) -> Integer.compare(k1.getWert(), k2.getWert())).orElse(null);
    }

    private Karte findeZweitHoechste(List<Karte> karten) {
        return karten.stream()
                .sorted((k1, k2) -> Integer.compare(k2.getWert(), k1.getWert()))
                .skip(1).findFirst().orElse(findeHoechste(karten));
    }

    private boolean kannGewinnen(List<Karte> karten, Karte ziel) {
        if (ziel == null) return true;
        return karten.stream().anyMatch(k -> k.getWert() > ziel.getWert());
    }

    private boolean kannVerlieren(List<Karte> karten, Karte ziel) {
        if (ziel == null) return false;
        return karten.stream().anyMatch(k -> k.getWert() < ziel.getWert());
    }

    private Karte findeNiedrigsteGewinnende(List<Karte> karten, Karte ziel) {
        return karten.stream()
                .filter(k -> ziel == null || k.getWert() > ziel.getWert())
                .min((k1, k2) -> Integer.compare(k1.getWert(), k2.getWert()))
                .orElse(findeNiedrigste(karten));
    }

    private Karte findeHoechsteVerlierende(List<Karte> karten, Karte ziel) {
        return karten.stream()
                .filter(k -> ziel != null && k.getWert() < ziel.getWert())
                .max((k1, k2) -> Integer.compare(k1.getWert(), k2.getWert()))
                .orElse(findeHoechste(karten));
    }

    // Zusätzliche Logik für Trumpf/Nicht-Trumpf beim Abwerfen (Rechte Seite)
    private boolean kannOhneTrumpfGewinnen(List<Karte> hand, Karte ziel, Farbe trumpf) {
        return filterNichtFarbe(hand, trumpf).stream().anyMatch(k -> ziel == null || k.getWert() > ziel.getWert());
    }

    private boolean kannMitTrumpfGewinnen(List<Karte> hand, Karte ziel, Farbe trumpf) {
        return filterFarbe(hand, trumpf).stream().anyMatch(k -> true); // Vereinfacht: Jeder Trumpf sticht Fehlfarbe
    }



    /**
     * Findet die niedrigste Karte, die ohne Trumpf gewinnt.
     */
    private Karte findeNiedrigsteGewinnendeNichtTrumpf(List<Karte> hand, Karte ziel, Farbe trumpf) {
        Karte kleinste = null;
        for (Karte k : hand) {
            if (k.getFarbe() != trumpf && (ziel == null || k.getWert() > ziel.getWert())) {
                if (kleinste == null || k.getWert() < kleinste.getWert()) {
                    kleinste = k;
                }
            }
        }
        return (kleinste != null) ? kleinste : findeNiedrigste(hand);
    }

    /**
     * Findet die höchste Karte, die den aktuellen Stich NICHT gewinnt.
     */
    private Karte findeHoechsteNichtGewinnende(List<Karte> hand, Karte ziel, Farbe trumpf) {
        if (ziel == null) return findeNiedrigste(hand);
        Karte hoechste = null;
        for (Karte k : hand) {
            if (k.getWert() < ziel.getWert()) {
                if (hoechste == null || k.getWert() > hoechste.getWert()) {
                    hoechste = k;
                }
            }
        }
        return (hoechste != null) ? hoechste : findeNiedrigste(hand);
    }

    /**
     * Prüft spezifisch, ob man mit einer Trumpfkarte den Stich verlieren kann (Unterstechen).
     */
    private boolean kannMitTrumpfVerlieren(List<Karte> hand, Karte ziel, Farbe trumpf) {
        if (ziel == null) return false;
        for (Karte k : hand) {
            if (k.getFarbe() == trumpf && k.getWert() < ziel.getWert()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Findet die höchste Trumpfkarte, die den Stich trotzdem verliert.
     */
    private Karte findeHoechsteVerlierendeTrumpf(List<Karte> hand, Karte ziel, Farbe trumpf) {
        Karte hoechste = null;
        for (Karte k : hand) {
            if (k.getFarbe() == trumpf && (ziel != null && k.getWert() < ziel.getWert())) {
                if (hoechste == null || k.getWert() > hoechste.getWert()) {
                    hoechste = k;
                }
            }
        }
        return (hoechste != null) ? hoechste : findeNiedrigste(filterFarbe(hand, trumpf));
    }

    /**
     * Findet die höchste Nicht-Trumpfkarte, die den Stich verliert.
     */
    private Karte findeHoechsteVerlierendeNichtTrumpf(List<Karte> hand, Karte ziel, Farbe trumpf) {
        Karte hoechste = null;
        for (Karte k : hand) {
            if (k.getFarbe() != trumpf && (ziel != null && k.getWert() < ziel.getWert())) {
                if (hoechste == null || k.getWert() > hoechste.getWert()) {
                    hoechste = k;
                }
            }
        }
        return (hoechste != null) ? hoechste : findeNiedrigste(filterNichtFarbe(hand, trumpf));
    }

    /**
     * Findet die absolut niedrigste Karte in der Liste (unabhängig von der Farbe).
     */
    private Karte findeNiedrigste(List<Karte> karten) {
        if (karten == null || karten.isEmpty()) return null;
        Karte schlechteste = karten.get(0);
        for (Karte k : karten) {
            if (k.getWert() < schlechteste.getWert()) {
                schlechteste = k;
            }
        }
        return schlechteste;
    }



    /**
     * Findet die niedrigste gewinnende Trumpfkarte.
     * (Pfad: NB_MitTrump -> Ja)
     */
    private Karte findeNiedrigsteGewinnendeTrumpf(List<Karte> hand, Karte ziel, Farbe trumpf) {
        List<Karte> truempfe = filterFarbe(hand, trumpf);
        // Jeder Trumpf gewinnt gegen eine Fehlfarbe (vereinfacht für das Diagramm)
        return findeNiedrigste(truempfe);
    }
    /**
     * Findet die absolut höchste Karte in der Hand, die den aktuellen Stich gewinnt.
     * Berücksichtigt dabei die Trumpffarbe, falls man nicht bedienen kann.
     */
    private Karte findeHoechsteGewinnende(List<Karte> hand, Karte ziel, Farbe trumpf) {
        Karte beste = null;
        for (Karte k : hand) {
            // Eine Karte gewinnt, wenn:
            // 1. Sie Trumpf ist und die Zielkarte kein Trumpf ist
            // 2. Sie Trumpf ist, die Zielkarte auch, aber sie einen höheren Wert hat
            // 3. Sie die gleiche Farbe wie die Zielkarte hat und einen höheren Wert hat
            if (istStaerker(k, ziel, trumpf)) {
                if (beste == null || k.getWert() > beste.getWert()) {
                    beste = k;
                }
            }
        }
        // Falls keine Karte gewinnen kann, wird die höchste verfügbare Karte gespielt
        return (beste != null) ? beste : findeHoechste(hand);
    }
    /**
     * Hilfsmethode zum Vergleich zweier Karten unter Berücksichtigung von Trumpf.
     */
    private boolean istStaerker(Karte k, Karte ziel, Farbe trumpf) {
        if (ziel == null) return true;

        // Fall 1: Eigene Karte ist Trumpf
        if (k.getFarbe() == trumpf) {
            if (ziel.getFarbe() != trumpf) return true;
            return k.getWert() > ziel.getWert();
        }

        // Fall 2: Eigene Karte ist kein Trumpf
        if (ziel.getFarbe() == trumpf) return false;
        if (k.getFarbe() != ziel.getFarbe()) return false;

        return k.getWert() > ziel.getWert();
    }

    /**
     * Schätzt, wie viele Stiche mit der aktuellen Hand noch sicher
     * oder sehr wahrscheinlich gewonnen werden können.
     */
    private int schaetzeSichereStiche(List<Karte> hand, Farbe trumpf) {
        int geschaetzteStiche = 0;

        for (Karte k : hand) {
            // Logik für sichere Stiche:
            // 1. Ein Ass (Wert 14) ist fast immer ein sicherer Stich
            if (k.getWert() == 13) {
                geschaetzteStiche++;
            }
            // 2. Hohe Trümpfe (König, Ass) zählen ebenfalls als sehr sicher
            else if (k.getFarbe() == trumpf && k.getWert() >= 10) {
                geschaetzteStiche++;
            }
            // 3. Optional: Man könnte hier noch "Buben" oder "Damen"
            // der Trumpffarbe mit 0.5 Stichen gewichten,
            // aber für die Basislogik reicht diese Schätzung.
        }

        return geschaetzteStiche;
    }

    /**
     * Sucht die stärkste Karte, die kein Trumpf ist.
     * Falls keine Fehlfarbe vorhanden ist, wird der kleinste Trumpf gewählt,
     * um die starken Trümpfe für später zu sparen.
     */
    private Karte staerksteNichtFrueherTrumpf(List<Karte> hand, Farbe trumpf) {
        Karte besteNichtTrumpf = null;

        // 1. Suche nach der höchsten Karte, die KEIN Trumpf ist
        for (Karte k : hand) {
            if (k.getFarbe() != trumpf) {
                if (besteNichtTrumpf == null || k.getWert() > besteNichtTrumpf.getWert()) {
                    besteNichtTrumpf = k;
                }
            }
        }

        // Wenn eine Nicht-Trumpfkarte existiert -> diese spielen (Phase 1)
        if (besteNichtTrumpf != null) {
            return besteNichtTrumpf;
        }

        // 2. Falls NUR Trümpfe auf der Hand sind -> kleinsten Trumpf spielen (Phase 2)
        // Das entspricht der Logik im Diagramm, wenn man gezwungen ist, Trumpf zu spielen
        Karte kleinsterTrumpf = hand.get(0);
        for (Karte k : hand) {
            if (k.getFarbe() == trumpf && k.getWert() < kleinsterTrumpf.getWert()) {
                kleinsterTrumpf = k;
            }
        }

        return kleinsterTrumpf;
    }


}