package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import java.util.concurrent.ThreadLocalRandom;

import java.awt.List;
import java.util.*;

public class psi8_Intel extends Agent {
  private int id;
  private int position;
  private int myBet;
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
          updateQualities(content[4].split(","));
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
        for (psi8_State state : p.getStates()) {
          if (state.getBestAction().getQuality() > s.getBestAction().getQuality()) {
            s = state;
          }
        }
      }

      psi8_Action best = s.getBestAction();
      /*System.out
          .println("I think player " + p.getId() + " has " + best.getCoins() + " coins in position " + s.getPosition());*/
      guess += best.getCoins();
    }

    while (Arrays.stream(previousGuesses).anyMatch(String.valueOf(guess)::equals)) {
      guess = ThreadLocalRandom.current().nextInt(myBet, (playersPlaying.size() * 3) + myBet + 1);
    }
    return guess;
  }

  private synchronized void updateQualities(String coins[]) {
    int i = -1;
    for (psi8_IPlayer p : playersPlaying.values()) {
      i++;
      if (i == position) {
        continue;
      }
      psi8_State s = p.getCurrentState();
      psi8_State nextS = p.getNextState();
      psi8_Action a = s.getAction(Integer.parseInt(coins[i]));
      s.updateActionQuality(a, nextS.getBestAction());
    }
  }

  private synchronized int myCoins() {
    myBet = ThreadLocalRandom.current().nextInt(0, 3 + 1);
    return myBet;
  }
}