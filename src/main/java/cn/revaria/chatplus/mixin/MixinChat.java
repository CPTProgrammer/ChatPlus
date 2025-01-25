package cn.revaria.chatplus.mixin;

import net.minecraft.MinecraftVersion;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.message.*;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ConnectedClientData;
import net.minecraft.server.network.ServerCommonNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinChat extends ServerCommonNetworkHandler {
	public MixinChat(MinecraftServer server, ClientConnection connection, ConnectedClientData clientData) {
		super(server, connection, clientData);
	}

	@Final
	@Shadow
	private MessageChainTaskQueue messageChainTaskQueue;

	@Shadow
	public ServerPlayerEntity player;

//	@Shadow protected abstract Optional<LastSeenMessageList> validateMessage(LastSeenMessageList.Acknowledgment acknowledgment);

	@Shadow protected abstract Optional<LastSeenMessageList> validateAcknowledgment(LastSeenMessageList.Acknowledgment acknowledgment);

	@Shadow protected abstract SignedMessage getSignedMessage(ChatMessageC2SPacket packet, LastSeenMessageList lastSeenMessages) throws MessageChain.MessageChainException;

	@Shadow protected abstract void handleMessageChainException(MessageChain.MessageChainException exception);

	@Shadow protected abstract void handleDecoratedMessage(SignedMessage message);

	@Shadow protected abstract CompletableFuture<FilteredMessage> filterText(String text);

	@Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
	public void onChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		// LOGGER.info("CHAT_MESSAGE: " + packet.chatMessage());

		if (hasIllegalCharacter(packet.chatMessage())) {
			disconnect(Text.translatable("multiplayer.disconnect.illegal_characters"));
		} else {
			Optional<LastSeenMessageList> optional = this.validateAcknowledgment(packet.acknowledgment());
			if (optional.isPresent()) {
				if (!packet.chatMessage().startsWith("/")){

					String changedMessage = packet.chatMessage().replace('&', '§');
					String regex = "\\[item(?:=([1-9]))?\\]";
					String[] messages = changedMessage.split(regex, -1);
					Deque<Integer> itemDeque = new ArrayDeque<>();

					Matcher matcher = Pattern.compile(regex).matcher(changedMessage);
					while (matcher.find()){
						String digit = matcher.group(1);
						if (digit == null){
							itemDeque.addLast(-1);
						}else {
							itemDeque.addLast(Integer.parseInt(digit));
						}
					}

					MutableText changedText = Text.empty();
					for (String message : messages) {
						changedText.append(Text.of(message));
						if (!itemDeque.isEmpty()) {
							ItemStack itemStack;
							if (itemDeque.getFirst() == -1) {
								itemStack = player.getMainHandStack();
							} else {
								itemStack = player.getInventory().getStack(itemDeque.getFirst() - 1);
							}
							changedText.append(itemStack.toHoverableText());
							itemDeque.removeFirst();
						}
					}

					try {
						SignedMessage signedMessage = getSignedMessage(packet, (LastSeenMessageList) optional.get());
						server.getPlayerManager().broadcast(signedMessage.withUnsignedContent(
							changedText
						), player, MessageType.params(MessageType.CHAT, player));

						// Compatible with mod "Discord Integration"
						try {
							Class<?> DiscordIntegrationMod = Class.forName("de.erdbeerbaerlp.dcintegration.architectury.DiscordIntegrationMod");
							Method handleChatMessage = DiscordIntegrationMod.getMethod("handleChatMessage", SignedMessage.class, ServerPlayerEntity.class);
							handleChatMessage.invoke(null, signedMessage.withUnsignedContent(changedText), player);
						} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
								 IllegalAccessException ignored) { }

						// Compatible with mod "Dynmap"
						String[] minecraftVersion = MinecraftVersion.CURRENT.getName().split("\\.");
						int minecraftPatchVersion = minecraftVersion.length == 3 ? Integer.parseInt(minecraftVersion[2]) : 0;
						for (int i = minecraftPatchVersion; i >= 0; i--){
							String version = minecraftVersion[0] + "." + minecraftVersion[1] + (i == 0 ? "" : ("." + i));
							try {
								/*
								  Warning: Using reflection can make the code harder to maintain, debug, and understand.
								  Statement: DynmapMod.plugin.chathandler.handleChat()
								 */
								Class<?> DynmapMod = Class.forName("org.dynmap.fabric_" + version.replaceAll("\\.", "_") + ".DynmapMod");
								Field pluginField = DynmapMod.getField("plugin");
								Object plugin = pluginField.get(null);
								Class<?> pluginClass = plugin.getClass();
								Field chatHandlerField = pluginClass.getDeclaredField("chathandler");
								chatHandlerField.setAccessible(true);
								Object chatHandler = chatHandlerField.get(plugin);
								Class<?> chatHandlerClass = chatHandler.getClass();
								Method handleChat = chatHandlerClass.getMethod("handleChat", ServerPlayerEntity.class, String.class);
								handleChat.invoke(chatHandler, player, signedMessage.withUnsignedContent(changedText).getContent().getString());
								break;
							} catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | NullPointerException |
									 IllegalAccessException | InvocationTargetException ignored) { }
						}

					} catch (MessageChain.MessageChainException e) {
						handleMessageChainException(e);
					}
				} else {
					this.server.submit(() -> {
						SignedMessage signedMessage;
						try {
							signedMessage = this.getSignedMessage(packet, optional.get());
						} catch (MessageChain.MessageChainException var6) {
							handleMessageChainException(var6);
							return;
						}

						CompletableFuture<FilteredMessage> completableFuture = filterText(signedMessage.getSignedContent());
						Text decoratedMessage = this.server.getMessageDecorator().decorate(this.player, signedMessage.getContent());
						messageChainTaskQueue.append(completableFuture, filteredMessage -> {
							SignedMessage message = signedMessage.withUnsignedContent(decoratedMessage).withFilterMask(filteredMessage.mask());
							this.handleDecoratedMessage(message);
						});
					});
				}
			}

		}

		ci.cancel();
	}

	private static boolean hasIllegalCharacter(String message) {
		for(int i = 0; i < message.length(); ++i) {
			if (!StringHelper.isValidChar(message.charAt(i))) {
				return true;
			}
		}

		return false;
	}
}