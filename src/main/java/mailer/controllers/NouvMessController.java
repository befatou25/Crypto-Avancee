package mailer.controllers;

import mailer.crypt.DataTypeEnum;
import mailer.crypt.EmailModel;
import mailer.crypt.Gestion;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.List;



public class NouvMessController {
    //region attributes
    public Button sendButton;
    public Button cancelButton;
    public TextField recipientTextField;
    public TextField bccTextField;
    public TextField subjectField;
    public Button joinAttachmentsButton;
    public TextField attachmentsField;
    public TextArea messageField;

    private EmailModel emailModel;
    //endregion

    //region methods

    public void AjoutFile() {
        FileChooser fileChooser = new FileChooser();
        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(joinAttachmentsButton.getScene().getWindow());
        if (selectedFiles != null) {
            String[] attachmentPaths = new String[selectedFiles.size()];
            for (int i = 0; i < selectedFiles.size(); i++) {
                attachmentPaths[i] = selectedFiles.get(i).getAbsolutePath();
            }
            attachmentsField.setText(String.join(";", attachmentPaths));
        }
    }
    public void sendMail() {
        String recipient = recipientTextField.getText();
        String subject = subjectField.getText();
        String message = messageField.getText();
        String cc = bccTextField.getText();
        String attachmentPath = attachmentsField.getText();

        String[] attachmentPaths = attachmentPath.split(";");
        if (attachmentPaths.length == 1 && attachmentPaths[0].isEmpty()) {
            Gestion.sendMail(recipient, subject, message, cc);
        } else {
            Gestion.sendMail(recipient, subject, message, attachmentPaths, cc);
        }

        Stage stage = (Stage) sendButton.getScene().getWindow();
        stage.close(); // Close the window
    }

    public void cancel() {
        cancelButton.getScene().getWindow().hide();
    }



    public void init() {
        cancelButton.setOnAction(event -> cancel());
        joinAttachmentsButton.setOnAction(event -> AjoutFile());
        sendButton.setOnAction(event -> sendMail());
    }

    public void setData(EmailModel email, DataTypeEnum type){
        this.emailModel=email;

        messageField.setText("\n\n *****************Voila le réponse*****************\n\n"+email.getText());

        if(type == DataTypeEnum.FORWARD) {
            subjectField.setText("Forward: " + email.getSubject());
            subjectField.setEditable(false);
        }
        else {
            subjectField.setText("Reply: " + email.getSubject());
            subjectField.setEditable(false);
            recipientTextField.setText(email.getFrom());
            recipientTextField.setEditable(false);
        }

    }


}


