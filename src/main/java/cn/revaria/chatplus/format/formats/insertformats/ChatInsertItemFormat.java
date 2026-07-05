package cn.revaria.chatplus.format.formats.insertformats;

import cn.revaria.chatplus.format.ChatFormatType;
import cn.revaria.chatplus.format.formats.ChatInsertFormat;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

public class ChatInsertItemFormat implements ChatInsertFormat {

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
	public MutableComponent getInsertComponent() {
		return item.getDisplayName().copy();
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
