import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.Semaphore;

import java.awt.List;
import java.util.*;

public class psi8_Intel0 extends Agent {
  private int id;
  private int position;
  private int myBet;
  private int loseStreak = 0;
  private Semaphore semaphore = new Semaphore(0);
  private LinkedHashMap<Integer, psi8_IPlayer> players = new LinkedHashMap<Integer, psi8_IPlayer>();
  private LinkedHashMap<Integer, psi8_IPlayer> playersPlaying = new LinkedHashMap<Integer, psi8_IPlayer>();

  protected void setup() {
    System.out.println("Hello! Fixed Agent " + getAID().getName() + " is ready.");
    registerAgent();
    addBehaviour(new InformServer());
    addBehaviour(new RequestServer());
  }

  protected void takeDown() {
    // Deregister Agent from yellow pages
    try {
      DFService.deregister(this);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }
    // Printout a dismissal message
    System.out.println("Fixed Agent " + getAID().getName() + "terminating");
  }

  /* 
    * Register Agent in yellow pages (DF)
  */
  private void registerAgent() {
    DFAgentDescription dfd = new DFAgentDescription();
    dfd.setName(getAID());
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Player");
    sd.setName("Psi-Game");
    dfd.addServices(sd);
    try {
      DFService.register(this, dfd);
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }
  }

  private class InformServer extends CyclicBehaviour {
    public void action() {
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      ACLMessage msg = myAgent.receive(mt);
      if (msg != null) {
        String[] content = msg.getContent().split("#");
        switch (content[0]) {
        case "Id":
          id = Integer.parseInt(content[1]);
          break;
        case "Result":
          if (!semaphore.tryAcquire()) {
            break;
          }
          if (content[1].equals("")) {
            updateQualities(content[4].split(","), false);
          } else {
            int winner = Integer.parseInt(content[1]);
            updateQualities(content[4].split(","), winner == position);
            if (winner == position) {
              loseStreak = 0;
            } else {
              loseStreak++;
            }
          }
          break;
        default:
          System.out.println(msg.getContent());
          break;
        }
      } else {
        block();
      }
    }
  }

  private class RequestServer extends CyclicBehaviour {
    public void action() {
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
      ACLMessage msg = myAgent.receive(mt);
      if (msg != null) {
        String[] content = msg.getContent().split("#");
        switch (content[0]) {
        case "GetCoins":
          position = Integer.parseInt(content[2]) - 1;
          savePlayers(content);
          sendReply(msg.createReply(), "MyCoins#" + myCoins());
          semaphore.release();
          break;
        case "GuessCoins":
          String[] previousGuesses = new String[] {};
          if (content.length == 2) {
            previousGuesses = content[1].split(",");
          }
          sendReply(msg.createReply(), "MyBet#" + guessCoins(previousGuesses));
          break;
        default:
          System.out.println(msg.getContent());
          break;
        }
      } else {
        block();
      }
    }
  }

  private void sendReply(ACLMessage msg, String content) {
    msg.setPerformative(ACLMessage.INFORM);
    msg.setContent(content);
    send(msg);
  }

  private synchronized void savePlayers(String content[]) {
    playersPlaying = new LinkedHashMap<Integer, psi8_IPlayer>();
    String[] ids = content[1].split(",");
    for (int position = 0; position < ids.length; position++) {
      int id = Integer.parseInt(ids[position]);
      if (this.id == id) {
        continue;
      }
      if (!players.containsKey(id)) {
        players.put(id, new psi8_IPlayer(id, ids.length));
      }
      psi8_IPlayer p = players.get(id);
      p.setCurrentState(position);
      playersPlaying.put(id, p);
    }
  }

  private synchronized int guessCoins(String previousGuesses[]) {
    int guess = myBet;
    for (psi8_IPlayer p : playersPlaying.values()) {
      psi8_State s = p.getCurrentState();

      if (s.getBestAction().getQuality() == 0) {
        s = p.getPrevState();
      }

      if (s.getBestAction().getQuality() == 0) {
        for (psi8_State state : p.getStates()) {
          if (state.getBestAction().getQuality() > s.getBestAction().getQuality()) {
            s = state;
          }
        }
      }

      psi8_Action best = s.getBestAction();
      guess += best.getCoins();
    }

    while (Arrays.stream(previousGuesses).anyMatch(String.valueOf(guess)::equals)) {
      guess = ThreadLocalRandom.current().nextInt(myBet, (playersPlaying.size() * 3) + myBet + 1);
    }
    return guess;
  }

  private synchronized void updateQualities(String coins[], boolean winner) {
    int i = 0;
    double winnerMultiplier = 2;
    double reward = 1;

    if (winner) {
      reward = winnerMultiplier * reward;
    }

    for (psi8_IPlayer p : playersPlaying.values()) {
      if (i == position) {
        i++;
      }
      if (i >= coins.length) {
        break;
      }

      psi8_State s = p.getCurrentState();
      psi8_State nextS = p.getNextState();
      psi8_Action a = s.getAction(Integer.parseInt(coins[i]));
      s.updateActionQuality(reward, a, nextS.getRealBestAction());
      i++;
    }
  }

  private synchronized int myCoins() {
    if (loseStreak > 2) {
      myBet = ThreadLocalRandom.current().nextInt(0, 3 + 1);
      loseStreak = 0;
      return myBet;
    }

    this.doWait((position % 3) + 1);
    int div = position == 0 ? 4 : (position % 3) + 1;
    myBet = (int) (System.currentTimeMillis() % div);

    return myBet;
  }
}