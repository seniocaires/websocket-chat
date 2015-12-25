package websocketchat.chat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.CloseReason;
import javax.websocket.EncodeException;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import websocketchat.data.ChatDecoder;
import websocketchat.data.ChatEncoder;
import websocketchat.data.ChatMessage;
import websocketchat.data.ChatUpdateMessage;
import websocketchat.data.NewUserMessage;
import websocketchat.data.UserListUpdateMessage;
import websocketchat.data.UserSignoffMessage;

@ServerEndpoint(value = "/chat-server", subprotocols={"chat"}, decoders = {ChatDecoder.class}, encoders = {ChatEncoder.class})
public class ChatServer {

	private static String USERNAME_KEY = "username";
	private static String USERNAMES_KEY = "usernames";
	private Session session;
	private Transcript transcript;
	private EndpointConfig endpointConfig;

	@OnOpen
	public void startChatChannel(EndpointConfig endpointConfig, Session session) {

		this.endpointConfig = endpointConfig;
		this.transcript = Transcript.getTranscript(endpointConfig);
		this.session = session;
	}

	@OnMessage
	public void handleChatMessage(ChatMessage message) {

		switch (message.getType()){
			case NewUserMessage.USERNAME_MESSAGE:
				this.processNewUser((NewUserMessage) message);
				break;
			case ChatMessage.CHAT_DATA_MESSAGE:
				this.processChatUpdate((ChatUpdateMessage) message);
				break;
			case ChatMessage.SIGNOFF_REQUEST:
				this.processSignoffRequest((UserSignoffMessage) message);
		}
	}

	@OnError
	public void myError(Throwable t) {
		System.out.println("Error: " + t.getMessage());
	}

	@OnClose
	public void endChatChannel() {

		if (this.getCurrentUsername() != null) {
			this.addMessage(" just left...without even signing out !");
			this.removeUser();
		}
	}

	void processNewUser(NewUserMessage message) {

		String newUsername = this.validateUsername(message.getUsername());
		NewUserMessage uMessage = new NewUserMessage(newUsername);

		try {
			session.getBasicRemote().sendObject(uMessage);
		} catch (IOException | EncodeException ioe) {
			System.out.println("Error signing " + message.getUsername() + " into chat : " + ioe.getMessage());
		} 

		this.registerUser(newUsername);
		this.broadcastUserListUpdate();
		this.addMessage(" just joined.");
	}

	void processChatUpdate(ChatUpdateMessage message) {
		this.addMessage(message.getMessage());
	}

	void processSignoffRequest(UserSignoffMessage drm) {

		this.addMessage(" just left.");
		this.removeUser();   
	}

	private String getCurrentUsername() {
		return (String) session.getUserProperties().get(USERNAME_KEY);
	}

	private void registerUser(String username) {

		session.getUserProperties().put(USERNAME_KEY, username);
		this.updateUserList();
	}

	private void updateUserList() {

		List<String> usernames = new ArrayList<>();
		for (Session s : session.getOpenSessions()) {
			String uname = (String) s.getUserProperties().get(USERNAME_KEY);
			usernames.add(uname);
		}

		this.endpointConfig.getUserProperties().put(USERNAMES_KEY, usernames);
	}

	private List<String> getUserList() {

		List<String> userList = (List<String>) this.endpointConfig.getUserProperties().get(USERNAMES_KEY);

		return (userList == null) ? new ArrayList<String>() : userList;
	}

	private String validateUsername(String newUsername) {

		if (this.getUserList().contains(newUsername)) {
			return this.validateUsername(newUsername + "1");
		}

		return newUsername;
	}

	private void broadcastUserListUpdate() {

		UserListUpdateMessage ulum = new UserListUpdateMessage(this.getUserList());

		for (Session nextSession : session.getOpenSessions()) {
			try {
				nextSession.getBasicRemote().sendObject(ulum);
			} catch (IOException | EncodeException ex) {
				System.out.println("Error updating a client : " + ex.getMessage());
			}
		}
	}

	private void removeUser() {

		try {
			this.updateUserList();
			this.broadcastUserListUpdate();
			this.session.getUserProperties().remove(USERNAME_KEY);
			this.session.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "User logged off"));
		} catch (IOException e) {
			System.out.println("Error removing user");
		}
	}

	private void broadcastTranscriptUpdate() {

		for (Session nextSession : session.getOpenSessions()) {
			ChatUpdateMessage cdm = new ChatUpdateMessage(this.transcript.getLastUsername(), this.transcript.getLastMessage());
			try {
				nextSession.getBasicRemote().sendObject(cdm);
			} catch (IOException | EncodeException ex) {
				System.out.println("Error updating a client : " + ex.getMessage());
			}   
		}
	}

	private void addMessage(String message) {

		this.transcript.addEntry(this.getCurrentUsername(), message);
		this.broadcastTranscriptUpdate();
	}

}
