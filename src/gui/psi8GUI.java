package gui;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import agents.*;

public class psi8GUI extends Application {

    public static boolean ready = false;
    protected static boolean verbose = false;
    protected static SimpleIntegerProperty totalGames = new SimpleIntegerProperty(0);
    protected static SimpleIntegerProperty gamesPlayed = new SimpleIntegerProperty(0);
    protected static SimpleIntegerProperty totalPlayers = new SimpleIntegerProperty(0);
    protected static SimpleIntegerProperty playersLeft = new SimpleIntegerProperty(0);
    protected static ObservableList<psi8Player> playersPlaying = FXCollections.observableArrayList();
    protected static ObservableList<psi8Player> playersNotPlaying = FXCollections.observableArrayList();
    private VBox root;
    private static ListView<String> console;
    private static psi8MainAgent agent;

    public psi8GUI(psi8MainAgent agent) {
        this.agent = agent;
        System.out.println("GUI started!");
    }

    public psi8GUI() {
    }

    public void show() {
        launch();
    }

    @Override
    public void start(Stage primaryStage) {
        root = new VBox();
        root.getChildren().addAll(buildMenu(), buildNumbers(), buildTables(), buildConsole());
        root.setVgrow(console, Priority.ALWAYS);
        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(this.getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Psi 8!");
        primaryStage.setScene(scene);
        primaryStage.show();
        ready = true;
    }

    public static void main(String[] args) {
        launch(args);
    }

    private MenuBar buildMenu() {
        MenuBar menuBar = new MenuBar();
        Menu run = new Menu("Run");
        MenuItem runNew = new MenuItem("New");
        runNew.setOnAction(event -> {
            if (totalGames.get() == 0) {
                buildPopup();
                root.setDisable(false);
            }
            log("New series of " + String.valueOf(totalGames.get()) + " games started!");
            log(String.valueOf(playersPlaying.size()) + " players playing\n");
            this.agent.newSeries(totalGames.get());
        });
        MenuItem runStop = new MenuItem("Stop");
        runStop.setOnAction(event -> {
            this.agent.doSuspend();
            log("Game stopped");
        });
        MenuItem runContinue = new MenuItem("Continue");
        runContinue.setOnAction(event -> {
            this.agent.doActivate();
            log("Game continued");
        });
        MenuItem gamesNumber = new MenuItem("Change number of games");
        gamesNumber.setOnAction(event -> {
            buildPopup();
            root.setDisable(false);
        });
        run.getItems().addAll(runNew, runStop, runContinue, gamesNumber);
        Menu window = new Menu("Window");
        CheckMenuItem windowVerbose = new CheckMenuItem("Verbose");
        windowVerbose.setOnAction(event -> {
            verbose = windowVerbose.isSelected();
            log("Verbose enabled");
        });
        window.getItems().addAll(windowVerbose);
        Menu help = new Menu("Help");
        MenuItem about = new MenuItem("About");
        about.setOnAction(event -> {
            buildHelpPopup();
            root.setDisable(false);
        });
        help.getItems().addAll(about);
        menuBar.getMenus().addAll(run, window, help);
        return menuBar;
    }

    private HBox buildNumbers() {
        HBox hbox = new HBox();
        hbox.getStyleClass().add("hbox");
        Label leftPLabel = new Label("0");
        leftPLabel.textProperty().bind(playersLeft.asString());
        Label playersLabel = new Label("0");
        playersLabel.textProperty().bind(totalPlayers.asString());
        playersLabel.getStyleClass().add("margin-right");
        Label leftGLabel = new Label("0");
        leftGLabel.textProperty().bind(gamesPlayed.asString());
        leftGLabel.getStyleClass().add("margin-left");
        Label gamesLabel = new Label("0");
        gamesLabel.getStyleClass().add("margin-right");
        gamesLabel.textProperty().bind(totalGames.asString());
        hbox.getChildren().addAll(leftPLabel, new Label(" of "), playersLabel, new Label("players"), leftGLabel,
                new Label(" of "), gamesLabel, new Label("games"));
        return hbox;
    }

    private ListView buildConsole() {
        console = new ListView();
        console.getItems().add("Information will be shown here if verbose option is enabled\n");
        return console;
    }

    private HBox buildTables() {
        HBox hbox = new HBox();
        Label stillPlaying = new Label("Players still playing:");
        stillPlaying.getStyleClass().add("bold");
        Label notPlaying = new Label("Players not playing:");
        notPlaying.getStyleClass().add("bold");
        VBox vboxPlaying = new VBox(stillPlaying, buildPlayersTable(playersPlaying));
        VBox vboxNotPlaying = new VBox(notPlaying, buildPlayersTable(playersNotPlaying));
        vboxPlaying.getStyleClass().add("vbox");
        vboxNotPlaying.getStyleClass().add("vbox");
        hbox.getChildren().addAll(vboxPlaying, vboxNotPlaying);
        hbox.getStyleClass().add("hbox-tables");
        hbox.setHgrow(vboxPlaying, Priority.ALWAYS);
        hbox.setHgrow(vboxNotPlaying, Priority.ALWAYS);
        return hbox;
    }

    private TableView buildPlayersTable(ObservableList<psi8Player> list) {
        TableView<psi8Player> table = new TableView<>();
        TableColumn<psi8Player, Integer> idColumn = new TableColumn<>("ID");
        idColumn.setCellValueFactory(new PropertyValueFactory<psi8Player, Integer>("id"));
        TableColumn<psi8Player, Integer> victoriesColumn = new TableColumn<>("Victories");
        victoriesColumn.setCellValueFactory(new PropertyValueFactory<psi8Player, Integer>("victories"));
        TableColumn<psi8Player, Integer> defeatsColumn = new TableColumn<>("Defeats");
        defeatsColumn.setCellValueFactory(new PropertyValueFactory<psi8Player, Integer>("defeats"));
        TableColumn<psi8Player, Integer> coinsColumn = new TableColumn<>("Coins");
        coinsColumn.setCellValueFactory(new PropertyValueFactory<psi8Player, Integer>("coins"));
        TableColumn<psi8Player, Integer> betColumn = new TableColumn<>("Bet");
        betColumn.setCellValueFactory(new PropertyValueFactory<psi8Player, Integer>("bet"));
        table.getColumns().addAll(idColumn, victoriesColumn, defeatsColumn, coinsColumn, betColumn);
        table.setItems(list);
        return table;
    }

    public void log(String msg) {
        if (!verbose || console == null) {
            return;
        }
        Platform.runLater(() -> {
            console.getItems().add(msg);
        });
    }

    private void buildPopup() {
        root.setDisable(true);
        Stage stage = new Stage();
        VBox vbox = new VBox();
        TextField field = new TextField();
        Button btn = new Button("Apply");
        btn.setOnAction(event -> {
            try {
                totalGames.set(Integer.parseInt(field.getText()));
                gamesPlayed.set(0);
                log("Number of games changed to " + String.valueOf(totalGames.get()));
                stage.close();
            } catch (Exception e) {
            }
        });
        vbox.getStyleClass().add("vbox");
        vbox.getChildren().addAll(field, btn);
        Scene s = new Scene(vbox, 300, 200);
        s.getStylesheets().add(psi8GUI.class.getResource("style.css").toExternalForm());
        stage.setTitle("Change number of games");
        stage.setScene(s);
        stage.showAndWait();
    }

    private void buildHelpPopup() {
        root.setDisable(true);
        Stage stage = new Stage();
        VBox vbox = new VBox();
        vbox.getStyleClass().add("vbox");
        Hyperlink link = new Hyperlink("https://github.com/brapifra");
        link.setOnAction(event -> {
            HostServicesDelegate hostServices = HostServicesFactory.getInstance(this);
            hostServices.showDocument("https://github.com/brapifra");
            event.consume();
        });
        vbox.getChildren().addAll(new Label("Brais Piñeiro Fraga"), link);
        Scene s = new Scene(vbox, 300, 200);
        s.getStylesheets().add(psi8GUI.class.getResource("style.css").toExternalForm());
        stage.setTitle("Help");
        stage.setScene(s);
        stage.showAndWait();
    }

    public void addPlayer(psi8Player p) {
        Platform.runLater(() -> {
            playersPlaying.add(p);
            totalPlayers.set(playersPlaying.size() + playersNotPlaying.size());
            playersLeft.set(playersPlaying.size());
        });
    }

    public void setGamesPlayed(int i) {
        Platform.runLater(() -> {
            playersLeft.set(playersPlaying.size());
            gamesPlayed.set(i);
        });
    }

    public void setPlayersLeft(int i) {
        Platform.runLater(() -> {
            playersLeft.set(i);
        });
    }
}
