package mailer.controllers;

import mailer.crypt.DataTypeEnum;
import mailer.crypt.EmailModel;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.io.FilenameUtils;

import javax.mail.*;
import javax.mail.search.MessageIDTerm;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

import static mailer.crypt.Gestion.deleteMail;
import static mailer.crypt.Gestion.getEmailsByFolder;
import static mailer.crypt.Set.getEmailSession;

public class AccController extends TransitionController {
    @FXML
    public Button newMessage;
    public Button reply;
    public Button forward;
    public Button delete;
    public ListView<EmailModel> mailList;
    public Button receptionButton;
    public SplitPane emailSplitPane;
    public TextField subjectField;
    public TextField senderField;

    @FXML
    public ListView<String> attachmentListView;

    public TextArea textArea;
    private EmailModel selectedEmail;


    @FXML
    private void initialize() {
        if (mailList == null) {
            mailList = new ListView<>();
        }

        mailList.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        mailList.getSelectionModel().selectedItemProperty().addListener(this::onSelectionChange);
    }

    @FXML
    public void gestionBoutton(ActionEvent event) throws IOException {
        setPrevStage((Stage) newMessage.getScene().getWindow());
        NextWin("NouvMess.fxml", false);
    }


    @FXML
    public void listMail() {
        BouttonActiver(false);

        this.selectedEmail = null;

        if (mailList == null) {
            mailList = new ListView<>();
        }
        mailList.setItems(getEmailsByFolder("INBOX"));
    }


    @FXML
    public void MailsEnvoye() {
        BouttonActiver(false);

        this.selectedEmail = null;

        if (mailList == null) {
            mailList = new ListView<>();
        }
        mailList.setItems(getEmailsByFolder("Sent"));
    }



    public void Supp() {
        try {
            Store store = getEmailSession().getStore("imaps");
            store.connect();
            Folder folder = store.getFolder(this.selectedEmail.getFolderName());
            folder.open(Folder.READ_WRITE);
            Message[] messages = folder.search(new MessageIDTerm(this.selectedEmail.getId()));
            Message foundMessage = messages[0];
            deleteMail(foundMessage.getHeader("Message-ID")[0], this.selectedEmail.getFolderName());
            folder.close(true);
            store.close();
            listMail();
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
    }


    private void replyMail(DataTypeEnum type) {
        try {
            Stage stage = new Stage();
            FXMLLoader loader = new FXMLLoader(Objects.requireNonNull(getClass().getResource("/com/example/mailer/NouvMess.fxml")));
            Pane pane = loader.load();
            NouvMessController newMessage = loader.getController();
            newMessage.setData(this.selectedEmail, type);
            stage.setScene(new Scene(pane));
            stage.show();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void replyto() {
        replyMail(DataTypeEnum.REPLY);
    }

    public void forwardto() {
        replyMail(DataTypeEnum.FORWARD);
    }

    private Path Filesave(String fileName) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Télécharger la pièce jointe");
        fileChooser.setInitialFileName(fileName);

        FileChooser.ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter(
                "Allowed file types", "*." + FilenameUtils.getExtension(fileName));
        fileChooser.getExtensionFilters().add(extensionFilter);

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            String extension = FilenameUtils.getExtension(file.getName());
            if (!extension.equalsIgnoreCase(FilenameUtils.getExtension(fileName))) {
                String newFileName = FilenameUtils.getBaseName(file.getName()) + "." +
                        FilenameUtils.getExtension(fileName);
                file = new File(file.getParent(), newFileName);
            }
            return file.toPath();
        } else {
            return null;
        }
    }


    @FXML
    private void onSelectionChange(ObservableValue<? extends EmailModel> observable, EmailModel oldValue, EmailModel newValue) {
        if (newValue != null) {
            BouttonActiver(true);

            try {
                Store store = getEmailSession().getStore("imaps");
                store.connect();
                Folder folder = store.getFolder(newValue.getFolderName());
                folder.open(Folder.READ_WRITE);
                Message[] messages = folder.search(new MessageIDTerm(newValue.getId()));
                Message foundMessage = messages[0];
                this.selectedEmail = newValue;

                subjectField.setText(foundMessage.getSubject());

                senderField.setText(foundMessage.getFrom()[0].toString());

                textArea.setText(newValue.getText());

                Object content = foundMessage.getContent();
                if (content instanceof Multipart) {
                    Multipart multipart = (Multipart) content;

                    attachmentListView.getItems().clear();

                    for (int i = 0; i < multipart.getCount(); i++) {
                        BodyPart bodyPart = multipart.getBodyPart(i);

                        if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {

                            String fileName = bodyPart.getFileName();

                            attachmentListView.getItems().add(fileName);

                            attachmentListView.setOnMouseClicked(event -> {
                                if (event.getClickCount() == 1) {
                                    try {
                                        Object selectedItem = attachmentListView.getSelectionModel().getSelectedItem();
                                        if (selectedItem instanceof String) {
                                            String selectedFileName = (String) selectedItem;

                                            BodyPart selectedBodyPart = null;
                                            for (int j = 0; j < multipart.getCount(); j++) {
                                                BodyPart bp = multipart.getBodyPart(j);
                                                if (Part.ATTACHMENT.equalsIgnoreCase(bp.getDisposition()) &&
                                                        selectedFileName.equalsIgnoreCase(bp.getFileName())) {
                                                    selectedBodyPart = bp;
                                                    break;
                                                }
                                            }

                                            if (selectedBodyPart != null) {
                                                InputStream is = selectedBodyPart.getInputStream();
                                                Path saveFilePath = Filesave(selectedFileName);
                                                if (saveFilePath != null) {
                                                    Files.copy(is, saveFilePath, StandardCopyOption.REPLACE_EXISTING);
                                                }
                                            }
                                        }
                                    } catch (IOException | MessagingException ex) {
                                        String errorMsg = "Failed to download attachment: " + ex.getMessage();

                                    }
                                }
                            });

                        }
                    }
                }


            } catch (Exception ex) {
            }
        }
    }

    private void BouttonActiver(boolean state) {
        emailSplitPane.setVisible(state);
        forward.setDisable(!state);
        reply.setDisable(!state);
        delete.setDisable(!state);
    }
}