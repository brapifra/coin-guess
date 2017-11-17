package agents;

import jade.core.AID;
import javafx.beans.property.SimpleIntegerProperty;

public class psi8_Player {
    private SimpleIntegerProperty id = new SimpleIntegerProperty();
    private SimpleIntegerProperty victories = new SimpleIntegerProperty();
    private SimpleIntegerProperty defeats = new SimpleIntegerProperty();
    private SimpleIntegerProperty coins = new SimpleIntegerProperty();
    private SimpleIntegerProperty bet = new SimpleIntegerProperty();
    private AID name;

    public psi8_Player(int id) {
        this.id.set(id);
    }

    public psi8_Player(AID name, int id) {
        this.name = name;
        this.id.set(id);
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
        return coins.get();
    }

    public void setCoins(int coins) {
        this.coins.set(coins);
    }

    public int getBet() {
        return bet.get();
    }

    public void setBet(int bet) {
        this.bet.set(bet);
    }

    public AID getName() {
        return name;
    }

    public void setName(AID name) {
        this.name = name;
    }

    public SimpleIntegerProperty coinsProperty() {
        return this.coins;
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

    public SimpleIntegerProperty betProperty() {
        return this.bet;
    }
}
