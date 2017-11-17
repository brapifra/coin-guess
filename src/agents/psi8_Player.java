package agents;

import jade.core.AID;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class psi8_Player {
    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleIntegerProperty victories = new SimpleIntegerProperty();
    private SimpleIntegerProperty defeats = new SimpleIntegerProperty();
    private SimpleStringProperty localName = new SimpleStringProperty();
    private int coins;
    private int bet;
    private AID name;

    public psi8_Player(int id) {
        this.id.set(id);
    }

    public psi8_Player(AID name, int id) {
        this.name = name;
        this.id.set(id);
        this.localName.set(name.getLocalName());
    }

    public int getId() {
        return id.get();
    }

    public void setId(int id) {
        this.id.set(id);
    }

    public int getVictories() {
        return victories.get();
    }

    public void setVictories(int victories) {
        this.victories.set(victories);
    }

    public int getDefeats() {
        return defeats.get();
    }

    public void setDefeats(int defeats) {
        this.defeats.set(defeats);
    }

    public int getCoins() {
        return coins;
    }

    public void setCoins(int coins) {
        this.coins = coins;
    }

    public int getBet() {
        return bet;
    }

    public void setBet(int bet) {
        this.bet = bet;
    }

    public AID getName() {
        return name;
    }

    public void setName(AID name) {
        this.name = name;
        this.localName.set(name.getLocalName());
    }

    public SimpleIntegerProperty victoriesProperty() {
        return this.victories;
    }

    public SimpleIntegerProperty defeatsProperty() {
        return this.defeats;
    }

    public SimpleIntegerProperty idProperty() {
        return this.id;
    }

    public SimpleStringProperty localNameProperty() {
        return this.localName;
    }
}
