package cn.revaria.chatplus.format.formats;

import cn.revaria.chatplus.format.ChatFormat;
import cn.revaria.chatplus.format.ChatFormatType;

import java.util.Map;

public class ChatColorFormat implements ChatFormat {

	private final int R, G, B;

	private final int startingIndex;
	private final ChatFormatType formatType = ChatFormatType.COLOR;

	public ChatColorFormat(int startingIndex, int R, int G, int B) {
		this.startingIndex = startingIndex;

		this.R = R;
		this.G = G;
		this.B = B;
	}
	public ChatColorFormat(int startingIndex, char colorCode) {
		this.startingIndex = startingIndex;

		if (VANILLA_COLOR_FORMATS.containsKey(colorCode)) {
			this.R = VANILLA_COLOR_FORMATS.get(colorCode).getR();
			this.G = VANILLA_COLOR_FORMATS.get(colorCode).getG();
			this.B = VANILLA_COLOR_FORMATS.get(colorCode).getB();
		}
		else {
			this.R = 0;
			this.G = 0;
			this.B = 0;
		}
	}

	public int getR() {
		return R;
	}
	public int getG() {
		return G;
	}
	public int getB() {
		return B;
	}

	@Override
	public int startingIndex() {
		return this.startingIndex;
	}
	@Override
	public ChatFormatType formatType() {
		return this.formatType;
	}
	@Override
	public ChatColorFormat copy() {
		return new ChatColorFormat(this.startingIndex, this.R, this.G, this.B);
	}

	public static final Map<Character, ChatColorFormat> VANILLA_COLOR_FORMATS = Map.ofEntries(
		Map.entry('0', new ChatColorFormat(0, 0, 0, 0)),
		Map.entry('1', new ChatColorFormat(0, 0, 0, 170)),
		Map.entry('2', new ChatColorFormat(0, 0, 170, 0)),
		Map.entry('3', new ChatColorFormat(0, 0, 170, 170)),
		Map.entry('4', new ChatColorFormat(0, 170, 0, 0)),
		Map.entry('5', new ChatColorFormat(0, 170, 0, 170)),
		Map.entry('6', new ChatColorFormat(0, 255, 170, 0)),
		Map.entry('7', new ChatColorFormat(0, 170, 170, 170)),
		Map.entry('8', new ChatColorFormat(0, 85, 85, 85)),
		Map.entry('9', new ChatColorFormat(0, 85, 85, 255)),
		Map.entry('a', new ChatColorFormat(0, 85, 255, 85)),
		Map.entry('b', new ChatColorFormat(0, 85, 255, 255)),
		Map.entry('c', new ChatColorFormat(0, 255, 85, 85)),
		Map.entry('d', new ChatColorFormat(0, 255, 85, 255)),
		Map.entry('e', new ChatColorFormat(0, 255, 255, 85)),
		Map.entry('f', new ChatColorFormat(0, 255, 255, 255))
	);
}
