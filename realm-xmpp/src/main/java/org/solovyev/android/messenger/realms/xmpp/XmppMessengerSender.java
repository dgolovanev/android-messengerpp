package org.solovyev.android.messenger.realms.xmpp;

import javax.annotation.Nonnull;

import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.XMPPException;
import org.solovyev.android.messenger.accounts.Account;
import org.solovyev.android.messenger.accounts.AccountConnectionException;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.messages.Message;

final class XmppMessengerSender implements XmppConnectedCallable<String> {

	@Nonnull
	private final Chat chat;

	@Nonnull
	private final Message message;

	@Nonnull
	private final Account account;

	XmppMessengerSender(@Nonnull Chat chat, @Nonnull Message message, @Nonnull Account account) {
		this.chat = chat;
		this.message = message;
		this.account = account;
	}

	@Override
	public String call(@Nonnull Connection connection) throws AccountConnectionException, XMPPException {
		final ChatManager chatManager = connection.getChatManager();

		final Entity chatId = chat.getEntity();
		final XmppMessageListener messageListener = new XmppMessageListener(account, chatId);
		org.jivesoftware.smack.Chat smackChat = chatManager.getThreadChat(chatId.getAccountEntityId());
		if (smackChat == null) {
			// smack forget about chat ids after restart => need to create chat here
			smackChat = chatManager.createChat(chat.getSecondUser().getAccountEntityId(), chatId.getAccountEntityId(), messageListener);
		} else if (!smackChat.getListeners().contains(messageListener)) {
			smackChat.addMessageListener(messageListener);
		}

		smackChat.sendMessage(message.getBody());

		return null;
	}
}
