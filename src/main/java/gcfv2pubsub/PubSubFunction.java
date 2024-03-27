package gcfv2pubsub;

import com.google.cloud.functions.CloudEventsFunction;
import com.google.events.cloud.pubsub.v1.MessagePublishedData;

import com.google.gson.Gson;
import io.cloudevents.CloudEvent;
import java.util.Base64;
import java.util.logging.Logger;
import com.mailgun.api.v3.MailgunMessagesApi;
import com.mailgun.client.MailgunClient;
import com.google.gson.JsonObject;
import com.mailgun.model.message.Message;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;



public class PubSubFunction implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(PubSubFunction.class.getName());
    private static String domain = System.getenv("MAIL_GUN_DOMAIN_NAME");
    private static String apiKey = System.getenv("MAIL_GUN_API_KEY");


  @Override
  public void accept(CloudEvent event) {
    String cloudEventData = new String(event.getData().toBytes());
    // Decode JSON event data to the Pub/Sub MessagePublishedData type
    Gson gson = new Gson();
    MessagePublishedData data = gson.fromJson(cloudEventData, MessagePublishedData.class);
        // Check if data is not null and if it contains a message
    if (data != null && data.getMessage() != null) {
      // Get the message from the data
      com.google.events.cloud.pubsub.v1.Message message = data.getMessage();
      // Get the base64-encoded data from the message & decode it
      String encodedData = message.getData();
      String decodedData = new String(Base64.getDecoder().decode(encodedData));
      // Log the message
      logger.info("Pub/Sub message: " + decodedData);

      JsonObject jsonPayload=gson.fromJson(decodedData,JsonObject.class);
      String email=jsonPayload.get("email").getAsString();
      String activationLink = jsonPayload.get("activationLink").getAsString();
      String tokenId = jsonPayload.get("tokenId").getAsString();
      String body = "Click on the link within 2 minutes for activation : " + activationLink;
      String subject = "User Creation Activation Link";
      try {
        sendEmail(email,subject, body);
      } catch (Exception e) {
        logger.severe("Error sending verification email: " + e.getMessage());
      }
      updateExpirationTimeForToken(tokenId);

    }
    logger.info("Pub/Sub message: " + "test");
  }

  public static void sendEmail(String sender, String subject, String body){
    MailgunMessagesApi mailgunMessagesApi = MailgunClient.config(apiKey)
            .createApi(MailgunMessagesApi.class);
    Message message = Message.builder()
            .from("noreply@srivijaykalki.me")
            .to(sender)
            .subject(subject)
            .text(body)
            .build();

    mailgunMessagesApi.sendMessage(domain, message);
    logger.info("sent message");
  }

  public static void updateExpirationTimeForToken(String tokenId) {
    // Database connection details
    String dbUrl = "jdbc:mysql://" + System.getenv("SQL_HOST") + "/" + System.getenv("SQL_DATABASE");
    String dbUsername = System.getenv("SQL_USERNAME");
    String dbPassword = System.getenv("SQL_PASSWORD");
    logger.info("dbUrl: " + dbUrl);
    logger.info("dbUsername: " + dbUsername);
    logger.info("dbPassword: " + dbPassword);

    try {
        // Connect to the database
        Connection conn = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);

        // Prepare update statement
        String updateQuery = "UPDATE confirmation_token_detail SET expiration_date = ? WHERE confirmation_token = ?";
        PreparedStatement pstmt = conn.prepareStatement(updateQuery);

        // Set expiration date to 2 minutes from current time
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.MINUTE, 2);
        Timestamp expirationTime = new Timestamp(cal.getTime().getTime());

        // Set parameters for the update statement
        pstmt.setTimestamp(1, expirationTime);
        pstmt.setString(2, tokenId);

        // Execute the update statement
        int rowsAffected = pstmt.executeUpdate();

        // Check if update was successful
        if (rowsAffected > 0) {
            logger.info("Expiration time updated for token!");
        } else {
            logger.severe("Token not found: " + tokenId);
        }

        // Close resources
        pstmt.close();
        conn.close();

    } catch (SQLException e) {
        logger.severe("Error updating expiration time in database: " + e.getMessage());
    }
  }
}