package mailer.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;


public class TransitionController {


    private Stage prevStage;

    public void setPrevStage(Stage s) { this.prevStage = s; }

    public void NextWin(String fxmlFileName, boolean closePrev) throws IOException {
        Stage stage = new Stage();
        Pane p = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/example/mailer/"+fxmlFileName)));
        stage.setScene(new Scene(p));
        if (closePrev) prevStage.close();
        stage.show();
    }
}