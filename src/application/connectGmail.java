package application;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.google.api.services.gmail.model.MessagePartHeader;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Krista Puķe
 */
public class connectGmail {

  private static final String APPLICATION_NAME = "NGUIproject";

  // Check https://developers.google.com/gmail/api/auth/scopes for all available scopes
  private static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
  
  // Email address of the user, or "me" can be used to represent the currently authorized user.
  private static final String USER = "nguiproject@gmail.com";
  
  /**
   * Client secret path .json file location
   */
  private static final String CLIENT_SECRET_PATH = "JSON/client_secret_326005122275-te2r3ju1hu0bqminu3jjgodooj7eo56b.apps.googleusercontent.com.json";
		  //"C:\\Users\\Krista\\Desktop\\For NGUI project\\Workspace\\testGAPI\\JSON\\client_secret_326005122275-te2r3ju1hu0bqminu3jjgodooj7eo56b.apps.googleusercontent.com.json";
  /**
   * Globally shared instance across your application.
   */
//  private static FileDataStoreFactory dataStoreFactory;
 
  /** Global instance of the HTTP transport. */
  private static HttpTransport httpTransport;

  /** Global instance of the JSON factory. */
  private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

  //Global service
  private static GoogleClientSecrets clientSecrets;
  public static Gmail service;
  public static GoogleAuthorizationCodeFlow flow;
  
  /** Authorization. */
  public void authorize() throws Exception {	  
      // initialize the transport
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();  
	  
	  // load client secrets
		  clientSecrets = GoogleClientSecrets.load(JSON_FACTORY,  new FileReader(CLIENT_SECRET_PATH));
		  
		  if (clientSecrets.getDetails().getClientId().startsWith("Enter")
		        || clientSecrets.getDetails().getClientSecret().startsWith("Enter ")) {
		      System.out.println("Enter Client ID and Secret");
		      System.exit(0);
		    }
	    
	    // Allow user to authorize via url.
	    flow = new GoogleAuthorizationCodeFlow.Builder(
	        httpTransport, JSON_FACTORY, clientSecrets, Arrays.asList(SCOPE))
	        .setAccessType("online")
	        .setApprovalPrompt("auto").build();
	    
	    //Build the url
	    String url = flow.newAuthorizationUrl().setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI)
	            .build();
	    //Open url in the default browser
		java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
		
//	    System.out.println("Please add the authorization code and press enter!\nCode: ");
  }
  
  public GoogleCredential ReadCodeAuthorize(String code) throws IOException{
	    
	    // Generate Credential using retrieved code.
	    GoogleTokenResponse response = flow.newTokenRequest(code)
	        .setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
	    GoogleCredential credential = new GoogleCredential()
	        .setFromTokenResponse(response); 
	  
	    // authorize
	    return credential;
  }

  
  public void PreMain(String code) { 
//  public static void main (String [] args) throws IOException {

    try {
      // initialize the transport
      httpTransport = GoogleNetHttpTransport.newTrustedTransport();

      // authorization
      Credential credential = ReadCodeAuthorize(code);     

      // Create a new authorized Gmail API client
     service = new Gmail.Builder(httpTransport, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build();
      
      if(service.equals(null)){
    	  System.out.println("Failed to connect to Gmail");
      }
      
      // run commands
     showMessages();
      
    } catch (IOException e) {
      System.err.println(e.getMessage());
    } catch (Throwable t) {
      t.printStackTrace();
    }
    //System.exit(0);
  }

	
  public ArrayList<ArrayList<Philosopher>> showMessages() throws IOException { 
//public static void showMessages() throws IOException { //
	  //Get all messages  
	  ListMessagesResponse messagesResponse = service.users().messages().list(USER).execute();
	    List<Message> messages = messagesResponse.getMessages();
	    
	    //List of all email lists
	    ArrayList<ArrayList<Philosopher>> messagesList = new ArrayList<ArrayList<Philosopher>>();

	    //Messages No in the row for MessagesList
	    Integer No = 0;
	    
	    //Fore each message search for To, subject, From, snippet - > take messages from INBOX
	    for (Message message : messages) {
	    	
		    //List of one email (id, snippet, From, To, Subject, Received)
	    	ArrayList<Philosopher> messageList = new ArrayList<>();
	    	
	    	//Get one messages all details
		    Message messageCont = service.users().messages().get(USER, message.getId()).execute();
	    
		    String Snippet = messageCont.getSnippet();  
		    String id = message.getId();
		    String Subject = null;
		    String From = null;
		    String To = null;
		    String Received = null;
		    
		    //Get one messages all headers
		    List<MessagePartHeader> messageHeaders = messageCont.getPayload().getHeaders();		    
		    
		    //List of READ UNREAD INBOX...
		    List<String> messageLabel = messageCont.getLabelIds(); 
		    
		    for(String lab : messageLabel){
			    if(lab.contains("INBOX")){	 
			    	
				    for (MessagePartHeader header: messageHeaders){				    	
				        if (header.getName().equals("Subject")){
				        	Subject = header.getValue();
				        }
				        else if (header.getName().equals("From")){
							Pattern MY_PATTERN = Pattern.compile("\\<(.*?)\\>");
							
							Matcher m = MY_PATTERN.matcher(header.getValue());
							while (m.find()) {
							    From = m.group(1);
							}
				        }
				        else if (header.getName().equals("To")){
				        	To = header.getValue();
				        }
				        else if (header.getName().equals("Date")){
				        	Received = header.getValue();
				        }  	
				    }
				    messageList.add(new Philosopher(No, id, Snippet, Subject, From, To, Received, messageLabel));
				    No ++;
				    
				    messagesList.add(messageList);
				}	
		    }
	 }
  
	    return messagesList;
  }
	
}