package agents;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class psi8_State {
  private final int reward = 1;
  private final double gamma = 0.5;
  private final double learningRate = 1.5;
  private int position;
  private ArrayList<psi8_Action> actions = new ArrayList<psi8_Action>();

  public psi8_State(int position, int length) {
    this.position = position;
    for (int i = 0; i < length; i++) {
      actions.add(new psi8_Action(i));
    }
  }

  public void updateActionQuality(psi8_Action psi8_action, psi8_Action bestActionNextState) {
    psi8_Action updatedAction = new psi8_Action(psi8_action.getCoins());
    double learnedValue = reward + (gamma * bestActionNextState.getQuality());
    double updatedQuality = psi8_action.getQuality() + learningRate * (learnedValue - psi8_action.getQuality());
    updatedAction.setQuality(updatedQuality);
    actions.set(psi8_action.getCoins(), updatedAction);
  }

  public psi8_Action getBestAction() {
    psi8_Action best;
    best = actions.get(0);
    for (int i = 0; i < actions.size(); i++) {
      if (actions.get(i).getQuality() > best.getQuality()) {
        best = actions.get(i);
      }
    }
    if (best.getQuality() == 0) {
      best = getRandomAction();
    }
    return best;
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