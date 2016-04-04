package app;

import actors.ControllerActor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import messages.system.*;
import utils.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class App extends Application {

    TextArea logArea;

    @Override
    public void start(Stage primaryStage) throws Exception {

        Scene scene = new Scene(configurateUI(), 1366, 768);
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public Parent configurateUI() {

        ActorSystem actorSystem = ActorSystem.create("commit_system");

        ActorRef controllerActor = actorSystem.actorOf(Props.create(ControllerActor.class));

        HBox hBox = new HBox();
        hBox.setSpacing(40);
        hBox.setMaxHeight(Double.MAX_VALUE);
        Node left = createCoordinatorView(controllerActor);
        HBox.setHgrow(left, Priority.ALWAYS);
        Node right = createParticipantViews(controllerActor);
        HBox.setHgrow(right, Priority.ALWAYS);
        hBox.getChildren().addAll(left, right);

        VBox vBox = new VBox();
        vBox.setPadding(new Insets(20));
        vBox.setSpacing(10);
        vBox.setMaxWidth(Double.MAX_VALUE);
        vBox.getChildren().addAll(hBox, logArea = createLogArea());

        return vBox;
    }

    private Node createParticipantViews(ActorRef controllerActor) {
        ScrollPane root = new ScrollPane ();
        root.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        root.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox container = new VBox();
        root.setContent(container);
        container.setSpacing(20);
        container.setMaxWidth(Double.MAX_VALUE);

        for (int i = 1; i <= Constants.PARTICIPANT_AMOUNT; i++) {
            container.getChildren().add( createParticipantView(i, controllerActor));
        }
        return root;
    }
    private Node createParticipantView(int index, ActorRef controller) {
        VBox root = new VBox();
        root.setMaxWidth(Double.MAX_VALUE);
        root.setSpacing(10);

        String participantName = "Computer #"+index;
        Label label = new Label(participantName);

        Label timeoutLabel = new Label("Таймаут");
        TextField participantTimeoutField = new TextField(String.valueOf(Constants.DEFAULT_TIMEOUT));
        HBox hb = new HBox();
        hb.getChildren().addAll(timeoutLabel, participantTimeoutField);
        hb.setSpacing(10);

        Label delayLabel = new Label("Задержка");
        TextField delayField = new TextField(String.valueOf(Constants.DEFAULT_DELAY));
        HBox delayhb = new HBox();
        delayhb.getChildren().addAll(delayLabel, delayField);
        delayhb.setSpacing(10);

        CheckBox participantOnlineStatus = new CheckBox("Online");
        participantOnlineStatus.setSelected(Constants.DEFAULT_ONLINE_STATUS);
        Button changePropsButton = new Button("Изменить параметры");
        controller.tell(new CreateParticipant(participantName, Constants.DEFAULT_TIMEOUT, Constants.DEFAULT_ONLINE_STATUS, Constants.DEFAULT_DELAY), ActorRef.noSender());
        changePropsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                long timeout;
                boolean status;
                long delay;
                try {
                    timeout = Long.valueOf(participantTimeoutField.getText());
                    delay =  Long.valueOf(delayField.getText());
                    status = participantOnlineStatus.isSelected();
                } catch (Exception e) {
                    return;
                }
                controller.tell(new ChangeParticipantProperties(participantName, timeout, status, delay), ActorRef.noSender());
            }
        });

        root.getChildren().addAll(label, hb, delayhb, participantOnlineStatus, changePropsButton);
        return root;
    }



    private VBox createCoordinatorView(ActorRef controller) {
        VBox coordinatorBox = new VBox();
        coordinatorBox.setMaxWidth(Double.MAX_VALUE);
        coordinatorBox.setSpacing(15);

        Label label = new Label("Координатор");
        Label timeoutLabel = new Label("Таймаут");
        TextField coordinatorTimeoutField = new TextField();
        HBox hb = new HBox();
        hb.getChildren().addAll(timeoutLabel, coordinatorTimeoutField);
        hb.setSpacing(10);
        coordinatorTimeoutField.setText(String.valueOf(Constants.DEFAULT_TIMEOUT));
        CheckBox coordinatorOnlineStatus = new CheckBox("Online");
        coordinatorOnlineStatus.setSelected(Constants.DEFAULT_ONLINE_STATUS);
        Button changePropsButton = new Button("Изменить параметры");
        changePropsButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                long timeout;
                boolean status;
                try {
                    timeout = Long.valueOf(coordinatorTimeoutField.getText());
                    status = coordinatorOnlineStatus.isSelected();
                } catch (Exception e) {
                    return;
                }
                controller.tell(new ChangeCoordinatorProperties(timeout, status), ActorRef.noSender());
            }
        });

        Button obtainTransactionButton = new Button("Запросить транзакцию");
        Button abortTransactionButton = new Button("Завершить транзакцию");

        obtainTransactionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.tell(new TransactionInit(null, new TransactionInit.Callback() {
                    @Override
                    public void onCommit() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("Transaction commit!");
                                alert.setHeaderText(null);
                                alert.showAndWait();
                            }
                        });

                    }

                    @Override
                    public void onAbort() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.ERROR);
                                alert.setTitle("Transaction abort.");
                                alert.setHeaderText(null);
                                alert.showAndWait();
                            }
                        });

                    }

                    @Override
                    public void onTimeout() {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                Alert alert = new Alert(Alert.AlertType.WARNING);
                                alert.setTitle("Connection problem...");
                                alert.setHeaderText(null);
                                alert.showAndWait();
                            }
                        });

                    }
                }), ActorRef.noSender());
                coordinatorBox.getChildren().remove(obtainTransactionButton);
                coordinatorBox.getChildren().add(abortTransactionButton);
            }
        });

        abortTransactionButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                controller.tell(new TransactionDestroy(), ActorRef.noSender());
                coordinatorBox.getChildren().remove(abortTransactionButton);
                coordinatorBox.getChildren().add(obtainTransactionButton);

            }
        });
        coordinatorBox.getChildren().addAll(label, hb, coordinatorOnlineStatus, changePropsButton, obtainTransactionButton);

        return coordinatorBox;
    }

    private TextArea createLogArea() {
        TextArea logArea = new TextArea("Waiting...\n");
        logArea.setMaxHeight(Double.MAX_VALUE);
        logArea.setMinHeight(240);
        OutputStream out = new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                synchronized (logArea) {
                    appendText(logArea, String.valueOf((char) b));
                }
            }
        };
        System.setOut(new PrintStream(out, true));
        return logArea;
    }
    public void appendText(TextArea logArea, String str) {
        Platform.runLater(() -> logArea.appendText(str));
    }

}
