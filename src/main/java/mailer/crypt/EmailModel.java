package mailer.crypt;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmailModel {
    private final String id;
    private final String sender;
    private final String subject;
    private final String text;
    private final String folderName;
    private final List<MimeBodyPart> attachments;
    private final String from;

    public EmailModel(Message message) throws MessagingException, IOException {
        this.id = message.getHeader("Message-ID")[0];
        this.subject = message.getSubject();
        this.sender = message.getFrom()[0].toString();
        this.text = message.getContent().toString();
        this.folderName = message.getFolder().getFullName();
        this.attachments = getAttachments(message);
        this.from = message.getFrom()[0].toString();
    }

    private List<MimeBodyPart> getAttachments(Message message) throws MessagingException, IOException {
        List<MimeBodyPart> attachments = new ArrayList<>();
        Object content = message.getContent();
        if (content instanceof MimeBodyPart) {
            MimeBodyPart mimeBodyPart = (MimeBodyPart) content;
            if (Part.ATTACHMENT.equalsIgnoreCase(mimeBodyPart.getDisposition())) {
                attachments.add(mimeBodyPart);
            }
        } else if (content instanceof Multipart) {
            Multipart multipart = (Multipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())) {
                    attachments.add((MimeBodyPart) bodyPart);
                }
            }
        }
        return attachments;
    }

    public String getId() {
        return id;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getText() {
        return text;
    }

    public String getFrom() {
        return from;
    }

    public String getSubject() {
        return subject;
    }

    public List<MimeBodyPart> getAttachments() {
        return attachments;
    }

    @Override
    public String toString() {
        return this.sender + "\n" +
                this.subject + " - " + this.text;
    }
}
