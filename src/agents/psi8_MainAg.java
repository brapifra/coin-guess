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

import agents.psi8_Player;
import gui.psi8_GUI;

public class psi8_MainAg extends Agent {
  private psi8_GUI gui;
  private Thread t;
  private LinkedHashMap<AID, psi8_Player> players = new LinkedHashMap<AID, psi8_Player>();
  private LinkedHashMap<AID, psi8_Player> playersPlaying;
  private int playersReady = 0;
  private int totalCoins = 0;
  private int next = 0;
  private int totalGames = 0;
  private int gamesPlayed = 0;
  private psi8_Player winner;

  protected void setup() {
    System.out.println("Hello! Main Agent " + getAID().getName() + " is ready.");
    launchGUI();
    searchAgents();
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
    gui = new psi8_GUI(this);
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

  private void searchAgents() {
    gui.log("Looking for players...");
    DFAgentDescription template = new DFAgentDescription();
    ServiceDescription sd = new ServiceDescription();
    sd.setType("Player");
    template.addServices(sd);
    try {
      DFAgentDescription[] result = DFService.search(this, template);
      gui.log("Sending IDs to players...");
      for (int i = 0; i < result.length; ++i) {
        psi8_Player p = new psi8_Player(result[i].getName(), players.size());
        players.put(result[i].getName(), p);
        gui.addPlayer(p);
        sendMessage(result[i].getName(), "Id#" + String.valueOf(players.size() - 1), ACLMessage.INFORM);
      }
    } catch (FIPAException fe) {
      fe.printStackTrace();
    }
    gui.log("Players ready!\n");
  }

  private class InformServer extends CyclicBehaviour {
    public void action() {
      MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);
      ACLMessage msg = myAgent.receive(mt);
      if (msg != null) {
        String[] content = msg.getContent().split("#");
        psi8_Player p = playersPlaying.get(msg.getSender());
        switch (content[0]) {
        case "MyCoins":
          addCoins(p, Integer.parseInt(content[1]));
          break;
        case "MyBet":
          addBet(p, Integer.parseInt(content[1]));
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

  /*
    *
    *
    * Messaging methods
    *
    *
  */
  private void sendMessage(AID aid, String content, int performative) {
    ACLMessage msg = new ACLMessage(performative);
    msg.addReceiver(aid);
    msg.setContent(content);
    send(msg);
  }

  public void requestCoins() {
    String content = "GetCoins#";
    for (psi8_Player p : playersPlaying.values()) {
      content += p.getId() + ",";
    }
    content = content.substring(0, content.length() - 1) + "#";
    int i = 1;
    for (psi8_Player p : playersPlaying.values()) {
      sendMessage(p.getName(), content + String.valueOf(i), ACLMessage.REQUEST);
      i++;
    }
  }

  private void requestNextGuess() {
    String req = "GuessCoins#";
    ArrayList<psi8_Player> array = new ArrayList<psi8_Player>(playersPlaying.values());
    for (int i = 0; i < next; i++) {
      req += String.valueOf(array.get(i).getBet());
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
    for (psi8_Player p : playersPlaying.values()) {
      bets += p.getBet() + ",";
      coins += p.getCoins() + ",";
    }
    bets = bets.substring(0, bets.length() - 1) + "#";
    coins = coins.substring(0, coins.length() - 1);
    for (psi8_Player p : playersPlaying.values()) {
      sendMessage(p.getName(), content + bets + coins, ACLMessage.INFORM);
    }
  }

  /*
    *
    *
    * Game-logic methods
    *
    *
  */

  public void newSeries(int totalGames) {
    this.totalGames = totalGames;
    this.gamesPlayed = 0;
    for (psi8_Player p : players.values()) {
      p.setVictories(0);
      p.setDefeats(0);
    }
    this.playersPlaying = new LinkedHashMap<AID, psi8_Player>();
    this.playersPlaying.putAll(players);
    newGame();
  }

  private void newGame() {
    this.totalCoins = 0;
    this.winner = null;
    this.next = 0;
    this.playersReady = 0;
    requestCoins();
  }

  private void changeTurns() {
    LinkedHashMap<AID, psi8_Player> list = new LinkedHashMap<AID, psi8_Player>();
    Map.Entry<AID, psi8_Player> pair = players.entrySet().iterator().next();
    players.remove(pair.getKey());
    list.putAll(players);
    list.put(pair.getKey(), pair.getValue());
    players = list;
  }

  private void addCoins(psi8_Player p, int coins) {
    gui.log(p.getName().getLocalName() + "#" + String.valueOf(p.getId()) + " has " + coins + " coins");
    p.setCoins(coins);
    totalCoins += coins;
    playersReady++;
    if (playersReady == playersPlaying.size()) {
      gui.log("There are " + String.valueOf(totalCoins) + " coins");
      gui.log("Time to guess!\n");
      requestNextGuess();
    }
  }

  private void addBet(psi8_Player p, int bet) {
    gui.log(p.getName().getLocalName() + "#" + String.valueOf(p.getId()) + " Bet: " + bet);
    p.setBet(bet);
    if (bet == totalCoins && winner == null) {
      winner = p;
    }
    if (next < playersPlaying.size()) {
      requestNextGuess();
    } else {
      gui.log("Bets have been made!\n");
      sendResult();
      checkWinner();
      if (this.playersPlaying.size() == 1) {
        psi8_Player loser = this.playersPlaying.values().iterator().next();
        gui.log("************* Game Loser: " + loser.getName().getLocalName() + "#" + String.valueOf(loser.getId())
            + " *************\n");
        loser.setDefeats(loser.getDefeats() + 1);
        changeTurns();
        this.playersPlaying = new LinkedHashMap<AID, psi8_Player>();
        this.playersPlaying.putAll(players);
        this.gamesPlayed++;
      }
      if (!isSeriesFinished()) {
        newGame();
      }
    }
  }

  private void checkWinner() {
    if (winner != null) {
      gui.log("************* Partial Winner: " + winner.getName().getLocalName() + "#" + String.valueOf(winner.getId())
          + " *************\n");
      winner.setVictories(winner.getVictories() + 1);
      playersPlaying.remove(winner.getName());
    } else {
      gui.log("************* No partial winner :( *************\n");
    }
  }

  private boolean isSeriesFinished() {
    gui.setGamesPlayed(this.gamesPlayed);
    if (this.gamesPlayed == this.totalGames) {
      psi8_Player winner = this.players.values().iterator().next();
      for (psi8_Player p : this.players.values()) {
        if (p.getVictories() > winner.getVictories()) {
          winner = p;
        }
      }
      gui.log("\n\n\n\n");
      gui.log("************* Game finished! *************\n");
      gui.log("************* Winner: " + winner.getName().getLocalName() + "#" + String.valueOf(winner.getId())
          + " *************\n");
      gui.log("\n\n\n\n");
      return true;
    }
    return false;
  }
}