package cn.revaria.chatplus.format.formats;

import cn.revaria.chatplus.format.ChatFormat;
import cn.revaria.chatplus.format.ChatFormatType;
import net.minecraft.world.item.ItemStack;

public class ChatInsertItemFormat implements ChatFormat {

	private final ItemStack item;

	private final int startingIndex;
	private final ChatFormatType formatType = ChatFormatType.INSERT;

	public ChatInsertItemFormat(int startingIndex, ItemStack item) {
		this.startingIndex = startingIndex;

		this.item = item;
	}

	public ItemStack getItem() {
		return item;
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
	public ChatInsertItemFormat copy() {
		return new ChatInsertItemFormat(this.startingIndex, this.item);
	}
}
