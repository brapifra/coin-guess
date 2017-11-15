package agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.domain.FIPAAgentManagement.*;
import jade.domain.FIPAException;
import jade.domain.DFService;
import javafx.stage.Stage;
import java.util.*;

import agents.psi8Player;
import gui.psi8GUI;

public class psi8MainAgent extends Agent {
  private psi8GUI gui;
  private Thread t;
  private HashMap<AID, psi8Player> players = new HashMap<AID, psi8Player>();
  private int playersReady = 0;
  private int totalCoins = 0;
  private psi8Player winner;
  private int next = 0;

  protected void setup() {
    System.out.println("Hello! Main Agent " + getAID().getName() + " is ready.");
    launchGUI();
    searchAgentsDF();
    addBehaviour(new InformServer());
  }

  protected void takeDown() {
    // Printout a dismissal message
    System.out.println("Main Agent " + getAID().getName() + "terminating");
  }

  /*
    * Launch GUI in a new Thread
  */
  private void launchGUI() {
    gui = new psi8GUI(this);
    new Thread() {
      public void run() {
        gui.show();
      }
    }.start();
    while (!gui.ready) {
      try {
        Thread.sleep(1000);
      } catch (Exception e) {

      }
    }
  }

  private class InformServer extends CyclicBehaviour {
    public void action() {
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      ACLMessage msg = myAgent.receive(mt);
      if (msg != null) {
        String[] content = msg.getContent().split("#");
        psi8Player p = players.get(msg.getSender());
        switch (content[0]) {
        case "MyCoins":
          p.setCoins(Integer.parseInt(content[1]));
          gui.log("#" + String.valueOf(p.getId()) + " has " + content[1] + " coins");
          totalCoins += Integer.parseInt(content[1]);
          playersReady++;
          if (playersReady == players.size() && next < players.size()) {
            gui.log("\nThere are " + String.valueOf(totalCoins) + " coins");
            gui.log("Time to guess!\n");
            requestGuessCoins();
          }
          break;
        case "MyBet":
          int bet = Integer.parseInt(content[1]);
          gui.log("#" + String.valueOf(p.getId()) + " Bet: " + content[1]);
          p.setBet(bet);
          if (bet == totalCoins && winner == null) {
            winner = p;
          }
          if (next < players.size()) {
            requestGuessCoins();
          } else {
            gui.log("\nBets have been made!\n");
            if (winner != null) {
              gui.log("************* Winner: #" + String.valueOf(winner.getId()) + " *************\n");
            } else {
              gui.log("************* No winner :( *************\n");
            }
            sendResult();
            playersReady = 0;
            totalCoins = 0;
            winner = null;
            next = 0;
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

  private void searchAgentsDF() {
    gui.log("Looking for players...");
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Player");
    template.addServices(sd);
    try {
      DFAgentDescription[] result = DFService.search(this, template);
      gui.log("Sending IDs to players...");
      for (int i = 0; i < result.length; ++i) {
        psi8Player p = new psi8Player(result[i].getName(), players.size());
        players.put(result[i].getName(), p);
        gui.addPlayer(p);
        sendMessage(result[i].getName(), "Id#" + String.valueOf(players.size() - 1), ACLMessage.INFORM);
      }
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }
    gui.log("Players ready!\n");
  }

  private void sendMessage(AID aid, String content, int performative) {
    ACLMessage msg = new ACLMessage(performative);
    msg.addReceiver(aid);
    msg.setContent(content);
    send(msg);
  }

  public void requestCoins() {
    String content = "GetCoins#";
    for (psi8Player p : players.values()) {
      content += p.getId() + ",";
    }
    content = content.substring(0, content.length() - 1) + "#";
    int i = 1;
    for (psi8Player p : players.values()) {
      sendMessage(p.getName(), content + String.valueOf(i), ACLMessage.REQUEST);
      i++;
    }
    gui.log("New series of " + String.valueOf(0) + " games started!");
    gui.log(String.valueOf(players.size()) + " players playing\n");
  }

  private void requestGuessCoins() {
    String req = "GuessCoins#";
    ArrayList<psi8Player> array = new ArrayList<psi8Player>(players.values());
    for (int i = 0; i < next; i++) {
      req += String.valueOf(array.get(i).getCoins());
      if ((i + 1) != next) {
        req += ",";
      }
    }
    sendMessage(array.get(next).getName(), req, ACLMessage.REQUEST);
    next++;
  }

  private void sendResult() {
    String content = "Result#" + (this.winner != null ? winner.getId() : "") + "#"
        + (this.winner != null ? this.totalCoins : "") + "#";
    String bets = "";
    String coins = "";
    for (psi8Player p : players.values()) {
      bets += p.getBet() + ",";
      coins += p.getCoins() + ",";
    }
    bets = bets.substring(0, bets.length() - 1) + "#";
    coins = coins.substring(0, coins.length() - 1);
    for (psi8Player p : players.values()) {
      sendMessage(p.getName(), content + bets + coins, ACLMessage.INFORM);
    }
  }
}