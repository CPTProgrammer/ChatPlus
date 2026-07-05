package cn.revaria.chatplus.format.formats;

import cn.revaria.chatplus.format.ChatFormat;
import net.minecraft.network.chat.MutableComponent;

public interface ChatInsertFormat extends ChatFormat {
	MutableComponent getInsertComponent();
	ChatInsertFormat copy();
}
