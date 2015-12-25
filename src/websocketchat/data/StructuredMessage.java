package websocketchat.data;

import java.util.ArrayList;
import java.util.List;

abstract class StructuredMessage extends ChatMessage {

	protected List<String> dataList = new ArrayList<>();

	protected StructuredMessage(String type) {
		super(type);
	}

	protected StructuredMessage(String type, List dataList) {

		super(type);
		this.dataList = dataList;
	}

	List getList() {
		return dataList;
	}

}
