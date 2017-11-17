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

public class psi8_Random extends Agent {
  private int id;
  private int position;
  private int myBet;
  private LinkedHashMap<Integer, psi8_Player> players = new LinkedHashMap<Integer, psi8_Player>();
  private LinkedHashMap<Integer, psi8_Player> playersPlaying;

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
          // Nothing to do here
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
          savePlayers(content);
          position = Integer.parseInt(content[2]);
          sendReply(msg.createReply(), "MyCoins#" + myCoins());
          break;
        case "GuessCoins":
          sendReply(msg.createReply(), "MyBet#" + guessCoins(content));
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

  private void savePlayers(String content[]) {
    playersPlaying = new LinkedHashMap<Integer, psi8_Player>();
    for (String s : content[1].split(",")) {
      int id = Integer.parseInt(s);
      if (!players.containsKey(id)) {
        players.put(id, new psi8_Player(id));
      }
      playersPlaying.put(id, new psi8_Player(id));
    }
  }

  private int guessCoins(String content[]) {
    int maxCoins;
    if (myBet == 0) {
      maxCoins = ThreadLocalRandom.current().nextInt(myBet, ((playersPlaying.size() - 1) * 3) + 1);
    } else {
      maxCoins = ThreadLocalRandom.current().nextInt(myBet, (playersPlaying.size() * 3) + 1);
    }
    if (content.length < 2) {
      return maxCoins;
    }
    content = content[1].split(",");
    boolean loop = true;
    while (Arrays.stream(content).anyMatch(String.valueOf(maxCoins)::equals)) {
      if (myBet == 0) {
        maxCoins = ThreadLocalRandom.current().nextInt(myBet, ((playersPlaying.size() - 1) * 3) + 1);
      } else {
        maxCoins = ThreadLocalRandom.current().nextInt(myBet, (playersPlaying.size() * 3) + 1);
      }
    }
    return maxCoins;
  }

  private int myCoins() {
    myBet = ThreadLocalRandom.current().nextInt(0, 3 + 1);
    return myBet;
  }
}