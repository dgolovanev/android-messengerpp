package org.solovyev.android.messenger.chats;

import com.google.inject.Inject;
import junit.framework.Assert;
import org.solovyev.android.messenger.AbstractMessengerTestCase;
import org.solovyev.android.messenger.realms.RealmEntity;
import org.solovyev.android.messenger.realms.TestRealm;
import org.solovyev.android.messenger.realms.TestRealmDef;

import java.util.ArrayList;

public class SqliteChatDaoTest extends AbstractMessengerTestCase {

    @Inject
    private ChatDao chatDao;

    @Inject
    private TestRealmDef testRealmDef;

    @Inject
    private TestRealm testRealm;

    public void setUp() throws Exception {
        super.setUp();
        chatDao.deleteAllChats();
    }

    public void testChatOperation() throws Exception {

        final ArrayList<ApiChat> chats = new ArrayList<ApiChat>();
        final RealmEntity realmUser = testRealm.newRealmEntity("01");
        final String userId = realmUser.getEntityId();
        chatDao.mergeUserChats(userId, chats);

        Assert.assertTrue(chatDao.loadUserChats(userId).isEmpty());

        chats.add(ApiChatImpl.newInstance(testRealm.newRealmEntity("01"), 10, false));
        chats.add(ApiChatImpl.newInstance(testRealm.newRealmEntity("02"), 10, false));
        chats.add(ApiChatImpl.newInstance(testRealm.newRealmEntity("03"), 10, false));
        final RealmEntity realmChat4 = testRealm.newRealmEntity("04");
        chats.add(ApiChatImpl.newInstance(realmChat4, 10, false));
        chatDao.mergeUserChats(userId, chats);

        Chat chat = chatDao.loadChatById(realmChat4.getEntityId());
        Assert.assertNotNull(chat);
        Assert.assertEquals(realmChat4.getEntityId(), chat.getRealmChat().getEntityId());
    }

    @Override
    public void tearDown() throws Exception {
        chatDao.deleteAllChats();
        super.tearDown();
    }
}