package websocketchat.data;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.websocket.*;

public class ChatDecoder implements Decoder.Text<ChatMessage> {

	@Override
	public void init(EndpointConfig config) {}

	@Override
	public void destroy() {}

	@Override
	public ChatMessage decode(String s) throws DecodeException {

		if (s.startsWith(NewUserMessage.USERNAME_MESSAGE)) {
			return new NewUserMessage(s.substring(NewUserMessage.USERNAME_MESSAGE.length()));
		} else if (s.startsWith(ChatMessage.SIGNOFF_REQUEST)) {
			return new UserSignoffMessage(s.substring(ChatMessage.SIGNOFF_REQUEST.length()));
		} else if (s.startsWith(ChatMessage.CHAT_DATA_MESSAGE)) {

			List<String> usernameUpdate = ChatDecoder.parseDataString(s.substring(ChatMessage.CHAT_DATA_MESSAGE.length()));
			String username = usernameUpdate.get(0);
			String message = usernameUpdate.get(1);

			return new ChatUpdateMessage(username, message);
		} else {
			throw new DecodeException(s, "Unknown message type");
		}
	}

	@Override
	public boolean willDecode(String s) {
		return s.startsWith(UserSignoffMessage.SIGNOFF_REQUEST) || s.startsWith(NewUserMessage.USERNAME_MESSAGE) || s.startsWith(UserSignoffMessage.CHAT_DATA_MESSAGE);
	}

	static List<String> parseDataString(String dataString) {

		List<String> dataList = new ArrayList<>();
		StringTokenizer st = new StringTokenizer(dataString, ChatEncoder.SEPARATOR);

		while (st.hasMoreTokens()) {
			String next = st.nextToken();
			if (!"".equals(next)) {
				dataList.add(next);
			}
		}

		return dataList;
	}
}
