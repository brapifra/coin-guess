import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class psi8_State {
  private final double gamma = 0.5;
  private double learningRate = 1;
  private final double minLearningRate = 0.25;
  private final double decFactorLR = 0.0005;
  private final double epsilon = 0.95;
  private int position;
  private ArrayList<psi8_Action> actions = new ArrayList<psi8_Action>();
  private psi8_Action lastBest;

  public psi8_State(int position, int length) {
    this.position = position;
    for (int i = 0; i < length; i++) {
      actions.add(new psi8_Action(i));
    }
  }

  public void updateActionQuality(double reward, psi8_Action psi8_action, psi8_Action bestActionNextState) {
    psi8_Action updatedAction = new psi8_Action(psi8_action.getCoins());
    double learnedValue = reward + (gamma * bestActionNextState.getQuality());
    double updatedQuality = psi8_action.getQuality() + learningRate * (learnedValue - psi8_action.getQuality());
    updatedAction.setQuality(updatedQuality);
    actions.set(psi8_action.getCoins(), updatedAction);

    if (learningRate > minLearningRate) {
      learningRate = learningRate - decFactorLR;
    }
  }

  public psi8_Action getBestAction() {
    if (Math.random() > epsilon) {
      lastBest = getRandomAction();
      return lastBest;
    }
    lastBest = actions.get(0);
    for (int i = 0; i < actions.size(); i++) {
      if (actions.get(i).getQuality() > lastBest.getQuality()) {
        lastBest = actions.get(i);
      }
    }
    if (lastBest.getQuality() == 0) {
      lastBest = getRandomAction();
    }
    return lastBest;
  }

  public psi8_Action getRealBestAction() {
    psi8_Action best = actions.get(0);
    for (int i = 0; i < actions.size(); i++) {
      if (actions.get(i).getQuality() > best.getQuality()) {
        best = actions.get(i);
      }
    }
    return best;
  }

  public psi8_Action getLastBestAction() {
    return lastBest;
  }

  private psi8_Action getRandomAction() {
    return actions.get(ThreadLocalRandom.current().nextInt(0, actions.size()));
  }

  public int getPosition() {
    return position;
  }

  public psi8_Action getAction(int coins) {
    return actions.get(coins);
  }
}