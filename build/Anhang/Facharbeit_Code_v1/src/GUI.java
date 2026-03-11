import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


public class GUI extends JFrame {


    private ArrayList<Karte> deck = new ArrayList<>();
    private Spieler spieler1 = new Spieler();
    private Spieler spieler2 = new Spieler();
    private Spieler startspieler;
    private Farbe angespielteFarbe = null;
    private NPC npc = new NPC();
    private boolean warteAufStichAuswertung = false;
    private JButton weiterButton;

    private Karte trumpf;
    private Karte mitte1 = null;
    private Karte mitte2 = null;

    private boolean spieler1AmZug = true;
    private int runde = 1;
    private int maxRunden = 10;


    private GamePanel panel = new GamePanel();

    public GUI() {
        setTitle("Wizard Grafisch");
        setSize(1000, 700);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(new BorderLayout());   // WICHTIG

        panel = new GamePanel();
        add(panel, BorderLayout.CENTER);

        weiterButton = new JButton("Weiter");
        weiterButton.setEnabled(false);
        weiterButton.addActionListener(e -> weiterGedrueckt());

        add(weiterButton, BorderLayout.SOUTH);  // unten platzieren

        starteRunde();
        setVisible(true);
    }

    private void starteRunde() {
        if (runde > maxRunden) {
            JOptionPane.showMessageDialog(this,
                    "Spiel beendet!\n\n" +
                            "Endstand:\n" +
                            "Spieler 1 Punkte: " + spieler1.punkte + "\n" +
                            "Spieler 2 Punkte: " + spieler2.punkte);
            return;
        }

        deck.clear();
        spieler1.hand.clear();
        spieler2.hand.clear();
        spieler1.stiche = 0;
        spieler2.stiche = 0;
        mitte1 = null;
        mitte2 = null;
        // Startspieler der ersten Runde = Spieler 1 (oder letzter Stich-Gewinner)
        if (startspieler == null) {
            startspieler = spieler1;
        }
        spieler1AmZug = (startspieler == spieler1);


        // Deck erstellen
        for (Farbe f : Farbe.values()) {
            for (int i = 1; i <= 13; i++) {
                deck.add(new Karte(f, i));
            }
        }

        Collections.shuffle(deck);

        // Karten austeilen (Runde = Anzahl Karten)
        for (int i = 0; i < runde; i++) {
            spieler1.hand.add(deck.remove(0));
            spieler2.hand.add(deck.remove(0));
        }

        // Trumpf ziehen
        trumpf = deck.remove(0);

        // Erst anzeigen lassen
        panel.repaint();

        //Ansage erst NACH dem Zeichnen der Karten
        SwingUtilities.invokeLater(() -> ansagePhase());
    }

    private void npcSpieleZug() {

        // Stichanzage berechnen
        spieler2.angesagteStiche = npc.berechneStichAnzahl(spieler2.hand, trumpf.getFarbe());

        if (spieler2.hand.isEmpty()) return;

        // Karte von der KI auswählen
        Karte karte = npc.waehleKarte(
                spieler2.hand,
                mitte1,
                trumpf.getFarbe(),
                spieler2.stiche,
                spieler2.angesagteStiche
        );

        // Index der Karte in der Hand ermitteln
        int index = -1;
        if (karte != null) {
            index = spieler2.hand.indexOf(karte);
        }

        // Karte spielen
        if (index != -1) {
            spieleKarte(spieler2, index);
        }
    }


    private void ansagePhase() {

        String a1 = JOptionPane.showInputDialog(this,
                "Runde " + runde + "\nSpieler 1: Wie viele Stiche sagst du an?");
        spieler1.ansage = Integer.parseInt(a1);

        // NPC sagt an
        spieler2.ansage = npc.berechneStichAnzahl(spieler2.hand, trumpf.getFarbe());


        if (startspieler == spieler2) {
            SwingUtilities.invokeLater(() -> npcSpieleZug());
        }
    }

    private Color farbeZuColor(Farbe f) {
        switch (f) {
            case ROT: return Color.RED;
            case GRUEN: return Color.GREEN;
            case GELB: return Color.YELLOW;
            case BLAU: return Color.BLUE;
        }
        return Color.WHITE;
    }

    private void spieleKarte(Spieler s, int index) {
        // Erste Karte im Stich
        if (mitte1 == null) {
            if ((startspieler == spieler1 && s == spieler1) ||
                    (startspieler == spieler2 && s == spieler2)) {

                Karte gewaehlteKarte = s.hand.get(index);

                mitte1 = s.hand.remove(index);

                //Angespielte Farbe festlegen
                angespielteFarbe = gewaehlteKarte.getFarbe();

                // Jetzt ist der andere Spieler dran
                spieler1AmZug = (s != spieler1);
            }
        }
        // Zweite Karte im Stich
        // Zweite Karte im Stich
        else if (mitte2 == null) {
            // Nur der andere Spieler darf spielen
            if ((startspieler == spieler1 && s == spieler2) ||
                    (startspieler == spieler2 && s == spieler1)) {

                Karte gewaehlteKarte = s.hand.get(index);

                //Prüfen ob Farbe bedient werden muss
                if (mussFarbeBedienen(s) && gewaehlteKarte.getFarbe() != angespielteFarbe) {
                    // Falsche Karte -> nichts passiert
                    return;
                }

                mitte2 = s.hand.remove(index);


                warteAufStichAuswertung = true;
                weiterButton.setEnabled(true);


            }
        }


        panel.repaint();

        if (!spieler1AmZug) {
            SwingUtilities.invokeLater(() -> npcSpieleZug());
        }
    }


    private void werteStichAus() {

        if (mitte1 == null || mitte2 == null) return;

        Spieler spielerMitte1 = startspieler;
        Spieler spielerMitte2 = (startspieler == spieler1) ? spieler2 : spieler1;

        Karte k1 = mitte1;
        Karte k2 = mitte2;

        Spieler gewinner = null;

        boolean k1Trumpf = k1.getFarbe() == trumpf.getFarbe();
        boolean k2Trumpf = k2.getFarbe() == trumpf.getFarbe();

        if (k1Trumpf && k2Trumpf) {
            gewinner = (k1.getWert() > k2.getWert()) ? spielerMitte1 : spielerMitte2;
        }
        else if (k1Trumpf) {
            gewinner = spielerMitte1;
        }
        else if (k2Trumpf) {
            gewinner = spielerMitte2;
        }
        else {
            boolean k1Bedient = k1.getFarbe() == angespielteFarbe;
            boolean k2Bedient = k2.getFarbe() == angespielteFarbe;

            if (k1Bedient && k2Bedient) {
                gewinner = (k1.getWert() > k2.getWert()) ? spielerMitte1 : spielerMitte2;
            }
            else if (k1Bedient) {
                gewinner = spielerMitte1;
            }
            else if (k2Bedient) {
                gewinner = spielerMitte2;
            }
            else {
                gewinner = spielerMitte1;
            }
        }

        gewinner.stiche++;

        startspieler = gewinner;
        spieler1AmZug = (startspieler == spieler1);

        mitte1 = null;
        mitte2 = null;
        angespielteFarbe = null;


        if (spieler1.hand.isEmpty() && spieler2.hand.isEmpty()) {
            rundeBeenden();
            return;
        }
        spieler1AmZug = (startspieler == spieler1);

        if (!spieler1AmZug) {
            SwingUtilities.invokeLater(() -> npcSpieleZug());
        }
    }





    private void rundeBeenden() {
        // Punkte berechnen (Wizard-System)
        berechnePunkte(spieler1);
        berechnePunkte(spieler2);

        JOptionPane.showMessageDialog(this,
                "Runde " + runde + " beendet!\n\n" +
                        "Stiche:\n" +
                        "Spieler 1: " + spieler1.stiche + " (Ansage: " + spieler1.ansage + ")\n" +
                        "Spieler 2: " + spieler2.stiche + " (Ansage: " + spieler2.ansage + ")\n\n" +
                        "AKTUELLER PUNKTSTAND:\n" +
                        "Spieler 1: " + spieler1.punkte + " Punkte\n" +
                        "Spieler 2: " + spieler2.punkte + " Punkte"
        );

        runde++;
        starteRunde();
    }


    private void berechnePunkte(Spieler s) {
        if (s.stiche == s.ansage) {
            s.punkte += 20 + (10*s.stiche);
        } else {
            s.punkte -= Math.abs(10*(s.stiche - s.ansage));
        }
    }
    private boolean mussFarbeBedienen(Spieler s) {
        if (angespielteFarbe == null) return false;

        for (Karte k : s.hand) {
            if (k.getFarbe() == angespielteFarbe) {
                return true; // Spieler hat passende Farbe
            }
        }
        return false;
    }

    private void weiterGedrueckt() {

        if (warteAufStichAuswertung) {
            werteStichAus();
            warteAufStichAuswertung = false;
            weiterButton.setEnabled(false);
        }
    }



    class GamePanel extends JPanel {


        public GamePanel() {

            setLayout(new BorderLayout());  // WICHTIG

            weiterButton = new JButton("Weiter");
            weiterButton.setEnabled(false);
            weiterButton.addActionListener(e -> weiterGedrueckt());

            add(weiterButton, BorderLayout.SOUTH);

            addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    int y = e.getY();
                    int x = e.getX();

                    if (spieler1AmZug && y > getHeight() - 150) {
                        int index = (x - 100) / 70;
                        if (index >= 0 && index < spieler1.hand.size()) {
                            spieleKarte(spieler1, index);
                        }
                    }
                }
            });
        }

        private void drawBackside(Graphics g, int x, int y) {
            g.setColor(Color.DARK_GRAY);
            g.fillRect(x, y, 60, 90);

            g.setColor(Color.BLACK);
            g.drawRect(x, y, 60, 90);

            g.setColor(Color.WHITE);
            g.drawString("?", x + 25, y + 50);
        }

        protected void paintComponent(Graphics g) {
            super.paintComponent(g);





            // Trumpf links
            if (trumpf != null) {
                drawCard(g, trumpf, 20, getHeight() / 2 - 50);
                g.setColor(Color.BLACK);
                g.drawString("Trumpf", 20, getHeight() / 2 - 60);
            }

            // Mitte Karten
            if (mitte1 != null) {
                drawCard(g, mitte1, getWidth()/2 - 80, getHeight()/2 - 50);
            }
            if (mitte2 != null) {
                drawCard(g, mitte2, getWidth()/2 + 20, getHeight()/2 - 50);
            }

            for (int i = 0; i < spieler2.hand.size(); i++) {
                drawBackside(g, 100 + i * 70, 20);
            }


            // Spieler 1 Karten (unten)
            for (int i = 0; i < spieler1.hand.size(); i++) {
                Karte k = spieler1.hand.get(i);
                boolean istAmZug = spieler1AmZug;
                boolean darfSpielen = !mussFarbeBedienen(spieler1) ||
                        angespielteFarbe == null ||
                        k.getFarbe() == angespielteFarbe;

                drawCardMitTransparenz(g, k, 100 + i * 70, getHeight() - 120,
                        istAmZug && !darfSpielen);
            }


            g.setColor(Color.BLACK);
            g.drawString("Runde: " + runde, getWidth()/2 - 30, 20);

            // Punkte & Stiche Anzeige
            g.drawString("Stiche P2: " + spieler2.stiche + " | Ansage: " + spieler2.ansage,
                    getWidth() - 260, 30);
            g.drawString("Punkte P2: " + spieler2.punkte,
                    getWidth() - 260, 50);

            g.drawString("Stiche P1: " + spieler1.stiche + " | Ansage: " + spieler1.ansage,
                    getWidth() - 260, getHeight() - 50);
            g.drawString("Punkte P1: " + spieler1.punkte,
                    getWidth() - 260, getHeight() - 30);
            // Startspieler Anzeige rechts mittig
            g.setColor(Color.BLACK);
            g.setFont(new Font("Arial", Font.BOLD, 18));

            String startText = "Startspieler: " +
                    (startspieler == spieler1 ? "Spieler 1" : "Spieler 2");

            int x = getWidth() - 220;
            int y = getHeight() / 2;

            g.drawString(startText, x, y);
            String amZugText = "Am Zug: " + (spieler1AmZug ? "Spieler 1" : "Spieler 2");
            g.drawString(amZugText, getWidth() - 220, getHeight()/2 + 30);


        }

        private void drawCard(Graphics g, Karte k, int x, int y) {
            g.setColor(farbeZuColor(k.getFarbe()));
            g.fillRect(x, y, 60, 90);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, 60, 90);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString(String.valueOf(k.getWert()), x + 22, y + 50);
        }

        private void drawCardMitTransparenz(Graphics g, Karte k, int x, int y, boolean ausgegraut) {
            Graphics2D g2 = (Graphics2D) g.create();

            if (ausgegraut) {
                // 50% Transparenz
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
            }

            g2.setColor(farbeZuColor(k.getFarbe()));
            g2.fillRect(x, y, 60, 90);

            g2.setColor(Color.BLACK);
            g2.drawRect(x, y, 60, 90);
            g2.setFont(new Font("Arial", Font.BOLD, 20));
            g2.drawString(String.valueOf(k.getWert()), x + 22, y + 50);

            g2.dispose();
        }


    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GUI::new);
    }
}
