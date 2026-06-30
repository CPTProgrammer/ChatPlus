package cn.revaria.chatplus.format;

public interface ChatFormat {
	int startingIndex();
	ChatFormatType formatType();
	ChatFormat copy();
}
