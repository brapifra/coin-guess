public class psi8_Action {
  private int coins;
  private double quality;

  public psi8_Action(int coins) {
    this.coins = coins;
    this.quality = 0;
  }

  public int getCoins() {
    return coins;
  }

  public void setCoins(int coins) {
    this.coins = coins;
  }

  public double getQuality() {
    return quality;
  }

  public void setQuality(double quality) {
    this.quality = quality;
  }
}