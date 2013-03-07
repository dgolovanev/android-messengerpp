package org.solovyev.android.messenger.realms.vk.longpoll;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.solovyev.android.messenger.MessengerApplication;
import org.solovyev.android.messenger.chats.Chat;
import org.solovyev.android.messenger.chats.ChatEventType;
import org.solovyev.android.messenger.chats.ChatService;
import org.solovyev.android.messenger.realms.Realm;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.messenger.users.UserEventType;
import org.solovyev.android.messenger.users.UserService;

import java.lang.reflect.Type;

/**
 * User: serso
 * Date: 6/24/12
 * Time: 12:53 AM
 */
public interface LongPollUpdate {

    void doUpdate(@Nonnull User user, @Nonnull Realm realm);

    public static class Adapter implements JsonDeserializer<LongPollUpdate> {

        @Override
        public LongPollUpdate deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            if (json.isJsonArray()) {

                final JsonArray jsonArray = json.getAsJsonArray();

                switch (jsonArray.get(0).getAsInt()) {
                    case 0:
                        return new RemoveMessage(jsonArray.get(1).getAsInt());
                    //case 3: todo serso: implement ONE message update
                    case 4:
                        int flags = jsonArray.get(2).getAsInt();
                        int chatUserId = jsonArray.get(3).getAsInt();
                        // todo serso: uncomment after answer
                        if (/*MessageFlag.chat.isApplied(flags) && */chatUserId >= 2000000000) {

                            // MAGIC MAGIC MAGIC
                            int chatId = chatUserId - 2000000000;

                            return MessageAdded.forChat(String.valueOf(chatId));

                        } else {
                            int userId = chatUserId;
                            return MessageAdded.forFriend(String.valueOf(userId));
                        }
                    case 8:
                        return new FriendOnline(String.valueOf(-jsonArray.get(1).getAsInt()), true);
                    case 9:
                        return new FriendOnline(String.valueOf(-jsonArray.get(1).getAsInt()), false);
                    case 51:
                        return new ChatChanged(jsonArray.get(1).getAsString());
                    case 61:
                        return new UserStartTypingInPrivateChat(jsonArray.get(1).getAsString());
                    case 62:
                        return new UserStartTypingInChat(jsonArray.get(1).getAsString(), jsonArray.get(2).getAsString());
                }

                return new EmptyLongPollUpdate();


                /*final JsonArray responseArray = response.getAsJsonArray("response");

                boolean first = true;

                result.response = new ArrayList<JsonMessage>();
                for (JsonElement e : responseArray.getAsJsonArray()) {
                    if (first) {
                        result.count = e.getAsInt();
                        first = false;
                    } else {
                        result.response.add((JsonMessage) context.deserialize(e, JsonMessage.class));
                    }
                }*/

            } else {
                throw new JsonParseException("Unexpected JSON type: " + json.getClass());
            }
        }
    }

    static class UserStartTypingInChat implements LongPollUpdate {

        @Nonnull
        private final String realmUserId;

        @Nonnull
        private final String realmChatId;

        public UserStartTypingInChat(@Nonnull String realmUserId, @Nonnull String realmChatId) {
            this.realmUserId = realmUserId;
            this.realmChatId = realmChatId;
        }

        @Override
        public void doUpdate(@Nonnull User user, @Nonnull Realm realm) {
            // not self
            if (!user.getRealmEntity().getRealmEntityId().equals(realmUserId)) {
                Chat chat = getChatService().getChatById(realm.newRealmEntity(realmChatId));
                if (chat != null) {
                    getChatService().fireChatEvent(chat, ChatEventType.user_start_typing, realmUserId);
                }
            }
        }


        @Nonnull
        private static ChatService getChatService() {
            return MessengerApplication.getServiceLocator().getChatService();
        }
    }

    static class UserStartTypingInPrivateChat implements LongPollUpdate {

        @Nonnull
        private String realmUserId;

        public UserStartTypingInPrivateChat(@Nonnull String realmUserId) {
            this.realmUserId = realmUserId;
        }

        @Override
        public void doUpdate(@Nonnull User user, @Nonnull Realm realm) {
            // not self
            if (!user.getRealmEntity().getRealmEntityId().equals(realmUserId)) {
                final RealmEntity secondRealmUser = realm.newRealmEntity(realmUserId);

                final RealmEntity realmChat = getChatService().createPrivateChatId(user.getRealmEntity(), secondRealmUser);
                Chat chat = getChatService().getChatById(realmChat);
                if (chat != null) {
                    getChatService().fireChatEvent(chat, ChatEventType.user_start_typing, secondRealmUser);
                }
            }
        }


        @Nonnull
        private static ChatService getChatService() {
            return MessengerApplication.getServiceLocator().getChatService();
        }
    }

    static class ChatChanged implements LongPollUpdate {

        @Nonnull
        private final String realmChatId;

        public ChatChanged(@Nonnull String realmChatId) {
            this.realmChatId = realmChatId;
        }

        @Override
        public void doUpdate(@Nonnull User user, @Nonnull Realm realm) {
            getChatService().syncChat(realm.newRealmEntity(realmChatId), user.getRealmEntity());
        }


        @Nonnull
        private ChatService getChatService() {
            return MessengerApplication.getServiceLocator().getChatService();
        }
    }

    static class EmptyLongPollUpdate implements LongPollUpdate {

        @Override
        public void doUpdate(@Nonnull User user, @Nonnull Realm realm) {
            // do nothing
        }


    }

    static class MessageAdded implements LongPollUpdate {

        @Nullable
        private String realmFriendId;

        @Nullable
        private String realmChatId;

        private MessageAdded() {
        }

        public static MessageAdded forChat(@Nonnull String realmChatId) {
            final MessageAdded result = new MessageAdded();

            result.realmFriendId = null;
            result.realmChatId = realmChatId;

            return result;
        }

        public static MessageAdded forFriend(@Nonnull String realmFriendId) {
            final MessageAdded result = new MessageAdded();

            result.realmFriendId = realmFriendId;
            result.realmChatId = null;

            return result;
        }

        @Override
        public void doUpdate(@Nonnull User user, @Nonnull Realm realm) {
            final RealmEntity realmChat;
            if (this.realmChatId != null) {
                realmChat = realm.newRealmEntity(this.realmChatId);
            } else {
                assert realmFriendId != null;
                realmChat = getChatService().createPrivateChatId(user.getRealmEntity(), realm.newRealmEntity(realmFriendId));
            }

            getChatService().syncNewerChatMessagesForChat(realmChat, user.getRealmEntity());
        }

        @Nonnull
        private ChatService getChatService() {
            return MessengerApplication.getServiceLocator().getChatService();
        }
    }

    static class FriendOnline implements LongPollUpdate {

        @Nonnull
        private final String realmFriendId;

        private final boolean online;

        public FriendOnline(@Nonnull String realmFriendId, boolean online) {
            this.realmFriendId = realmFriendId;
            this.online = online;
        }

        @Override
        public void doUpdate(@Nonnull User user, @Nonnull Realm realm) {
            final User friend = getUserService().getUserById(realm.newRealmEntity(realmFriendId)).cloneWithNewStatus(online);
            if (online) {
                getUserService().fireUserEvent(user, UserEventType.contact_online, friend);
            } else {
                getUserService().fireUserEvent(user, UserEventType.contact_offline, friend);
            }
        }

        private UserService getUserService() {
            return MessengerApplication.getServiceLocator().getUserService();
        }
    }

    static class RemoveMessage implements LongPollUpdate {

        @Nonnull
        private final Integer messageId;

        public RemoveMessage(@Nonnull Integer messageId) {
            this.messageId = messageId;
        }

        @Override
        public void doUpdate(@Nonnull User user, @Nonnull Realm realm) {
            // todo serso: implement
        }
    }

}
