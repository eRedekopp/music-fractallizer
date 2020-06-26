import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.stage.Stage;

import java.io.File;

public class MainView extends Application {

    private Button playButton;
    private Button stopButton;

    private AudioController audioController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create Objects
        this.playButton = new Button("Play");
        this.stopButton = new Button("Stop");
        this.audioController = new AudioController();

        // Get Melody from file
        File xmlFile = new File("./test-files/Licc.mscx");
        Melody melody = FileReader.readXmlFile(xmlFile);
        System.out.println(melody.toString());

        // Set listeners
        playButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                audioController.startAudioLoop(melody);
            }
        });

        stopButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                audioController.stopAudioLoop();
            }
        });

        // Setup view
        Scene scene = new Scene(new Group(new ToolBar(playButton, stopButton)));
        primaryStage.setTitle("Music Fractallizer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
