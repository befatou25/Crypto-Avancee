package mailer.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import javax.mail.MessagingException;
import javax.mail.Transport;
import java.io.IOException;

import static mailer.crypt.Set.getEmailSession;
import static mailer.crypt.Set.setEmailSession;


public class AuthController extends TransitionController{
    @FXML
    private TextField usernameTextField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Button login;
    @FXML
    private ProgressIndicator progressIndicator;



    @FXML
    public void initialize() {
        progressIndicator.setVisible(false);
    }

    @FXML
    private void Login(ActionEvent event) throws IOException, MessagingException {

        login.setDisable(true);
        progressIndicator.setVisible(true);

        String username = usernameTextField.getText();
        String password = passwordField.getText();

        boolean isValid = OutlookMailUtils.verifyOutlookCredentials(username, password);
        System.out.println("isValid=" + isValid);
        System.out.println(username);
        System.out.println(password);
        if (isValid) {
            Parent parent = FXMLLoader.load(getClass().getResource("/com/example/mailer/Acc.fxml"));
            Scene scene = new Scene(parent);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            setEmailSession(username,password);
            Transport transport = getEmailSession().getTransport("smtp");
            transport.connect();
            transport.close();
            stage.show();
        } else {
            Alert error = new Alert(Alert.AlertType.ERROR, "Nom d'utilisateur ou mot de passe invalide");
            error.showAndWait();
        }
    }
}
