package FlinkCommerce;

import javax.mail.*;
import javax.mail.internet.*;
import java.util.Properties;

public class EmailAlert {

    public static void sendAlert(String message) {

        String from = "kundana@alephys.com";
        String to = "kundana@alephys.com";

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.zoho.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.ssl.trust", "smtp.zoho.com");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");

        Session session = Session.getInstance(props,
            new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(
                        "kundana@alephys.com",
                        "Vikramarkudu@1234"
                    );
                }
            });

         try {

            Message msg = new MimeMessage(session);

            msg.setFrom(new InternetAddress(from));

            msg.setRecipients(
                    Message.RecipientType.TO,
                    InternetAddress.parse(to)
            );

            msg.setSubject("🚨 Flink Alert - Transaction Issue");

            msg.setText(message);


            Transport.send(msg);

            System.out.println("Zoho company email sent!");
            System.out.println("Sending email for: " + message);

        } catch (Exception e) {
            System.out.println("❌ EMAIL FAILED" + e.getMessage());
            e.printStackTrace();
        }
    }
}