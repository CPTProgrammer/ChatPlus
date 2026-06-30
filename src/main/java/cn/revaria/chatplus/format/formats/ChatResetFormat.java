package cn.revaria.chatplus.format.formats;

import cn.revaria.chatplus.format.ChatFormat;
import cn.revaria.chatplus.format.ChatFormatType;

public class ChatResetFormat implements ChatFormat {

	private final int startingIndex;
	private final ChatFormatType formatType = ChatFormatType.RESET;

	public ChatResetFormat(int startingIndex) {
		this.startingIndex = startingIndex;
	}

	@Override
	public int startingIndex() {
		return startingIndex;
	}
	@Override
	public ChatFormatType formatType() {
		return formatType;
	}
	@Override
	public ChatResetFormat copy() {
		return new ChatResetFormat(this.startingIndex);
	}
}
