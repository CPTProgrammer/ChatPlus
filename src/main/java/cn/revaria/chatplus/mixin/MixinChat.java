package cn.revaria.chatplus.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.item.ItemStack;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.network.message.MessageChainTaskQueue;
import net.minecraft.server.MinecraftServer;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextContent;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class MixinChat {
	@Shadow @Final
	static Logger LOGGER;

	@Final
	@Shadow
	private MinecraftServer server;

	@Shadow
	public abstract void disconnect(Text reason);

	@Final
	@Shadow
	private MessageChainTaskQueue messageChainTaskQueue;

	@Shadow
	private ServerPlayerEntity player;

	@Shadow protected abstract Optional<LastSeenMessageList> validateMessage(String message, Instant timestamp, LastSeenMessageList.Acknowledgment acknowledgment);

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
			Optional<LastSeenMessageList> optional = this.validateMessage(packet.chatMessage(), packet.timestamp(), packet.acknowledgment());
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

					MutableText changedText = MutableText.of(TextContent.EMPTY);
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
						// handleDecoratedMessage(signedMessage.withUnsignedContent(changedText));
						try {
							Class<?> DiscordIntegrationMod = Class.forName("de.erdbeerbaerlp.dcintegration.fabric.DiscordIntegrationMod");
							Method handleChatMessage = DiscordIntegrationMod.getMethod("handleChatMessage", SignedMessage.class, ServerPlayerEntity.class);
							handleChatMessage.invoke(null, signedMessage.withUnsignedContent(changedText), player);
						} catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException |
								 IllegalAccessException ignored) { }
					} catch (MessageChain.MessageChainException e) {
						handleMessageChainException(e);
					}
				} else {
					this.server.submit(() -> {
						SignedMessage signedMessage;
						try {
							signedMessage = this.getSignedMessage(packet, (LastSeenMessageList) optional.get());
						} catch (MessageChain.MessageChainException var6) {
							handleMessageChainException(var6);
							return;
						}

						CompletableFuture<FilteredMessage> completableFuture = filterText(signedMessage.getSignedContent());
						CompletableFuture<Text> completableFuture2 = this.server.getMessageDecorator().decorate(this.player, signedMessage.getContent());
						messageChainTaskQueue.append((executor) -> {
							return CompletableFuture.allOf(completableFuture, completableFuture2).thenAcceptAsync((void_) -> {
								SignedMessage signedMessage2 = signedMessage.withUnsignedContent((Text) completableFuture2.join()).withFilterMask(((FilteredMessage) completableFuture.join()).mask());
								handleDecoratedMessage(signedMessage2);
							}, executor);
						});
					});
				}
			}

		}

		ci.cancel();
	}

	private static boolean hasIllegalCharacter(String message) {
		for(int i = 0; i < message.length(); ++i) {
			if (!SharedConstants.isValidChar(message.charAt(i))) {
				return true;
			}
		}

		return false;
	}
}