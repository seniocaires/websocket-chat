package websocketchat.chat;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.EndpointConfig;

public class Transcript {

	private List<String> messages = new ArrayList<>();
	private List<String> usernames = new ArrayList<>();
	private int maxLines;
	private static String TRANSCRIPT_ATTRIBUTE_NAME = "CHAT_TRANSCRIPT_AN";

	public static Transcript getTranscript(EndpointConfig ec) {

		if (!ec.getUserProperties().containsKey(TRANSCRIPT_ATTRIBUTE_NAME)) {
			ec.getUserProperties().put(TRANSCRIPT_ATTRIBUTE_NAME, new Transcript(20));  
		}

		return (Transcript) ec.getUserProperties().get(TRANSCRIPT_ATTRIBUTE_NAME);
	}

	Transcript(int maxLines) {
		this.maxLines = maxLines;
	}

	public String getLastUsername() {
		return usernames.get(usernames.size() -1);
	}

	public String getLastMessage() {
		return messages.get(messages.size() -1);
	}

	public void addEntry(String username, String message) {

		if (usernames.size() > maxLines) {
			usernames.remove(0);
			messages.remove(0);
		}

		usernames.add(username);
		messages.add(message);
	}
}
