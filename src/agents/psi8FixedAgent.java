package agents;

import jade.core.Agent;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import java.util.HashMap;

public class psi8FixedAgent extends Agent {
  private int id;
  private int position;
  private HashMap<Integer, psi8Player> players = new HashMap<Integer, psi8Player>();

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
          System.out.println("Result");
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
          for (String s : content[1].split(",")) {
            int id = Integer.parseInt(s);
            if (!players.containsKey(id)) {
              players.put(id, new psi8Player(id));
            }
          }
          position = Integer.parseInt(content[2]);
          sendReply(msg.createReply(), "MyCoins#3");
          break;
        case "GuessCoins":
          sendReply(msg.createReply(), "MyBet#3");
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
}