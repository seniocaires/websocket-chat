package websocketchat.data;

public abstract class ChatMessage {

	public static final String USERLIST_UPDATE = "ulupd";
	public static final String CHAT_DATA_MESSAGE = "ctmsg";
	public static final String USERNAME_MESSAGE = "unmsg";
	public static final String SIGNOFF_REQUEST = "sorq";

	String type;

	protected ChatMessage(String type) {
		this.type = type;
	}

	public String getType() {
		return type;
	}
}
