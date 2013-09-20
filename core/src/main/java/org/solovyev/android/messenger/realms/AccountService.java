package org.solovyev.android.messenger.realms;

import android.content.Context;
import org.solovyev.android.messenger.entities.Entity;
import org.solovyev.android.messenger.entities.EntityAware;
import org.solovyev.android.messenger.security.InvalidCredentialsException;
import org.solovyev.android.messenger.users.User;
import org.solovyev.android.properties.AProperty;
import org.solovyev.common.listeners.JEventListener;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;

/**
 * User: serso
 * Date: 7/22/12
 * Time: 12:57 AM
 */
public interface AccountService {

	@Nonnull
	static String TAG = "AccountService";

	/**
	 * Method initializes service, must be called once before any other operations with current service
	 */
	void init();

	/**
	 * Method restores service state (e.g. loads persistence data from database)
	 */
	void load();


	/**
	 * @return collection of all configured realms in application
	 */
	@Nonnull
	Collection<RealmDef<? extends AccountConfiguration>> getRealmDefs();

	@Nonnull
	Collection<Account> getAccounts();

	@Nonnull
	Collection<Account> getEnabledAccounts();

	/**
	 * @return collection of users in all configured realms
	 */
	@Nonnull
	Collection<User> getRealmUsers();

	/**
	 * @return collection of users in all configured ENABLED realms
	 */
	@Nonnull
	Collection<User> getEnabledRealmUsers();

	/**
	 * Method returns the realm which previously has been registered in this service
	 *
	 * @param realmDefId id of realm def
	 * @return realm
	 * @throws UnsupportedRealmException if realm hasn't been registered in this service
	 */
	@Nonnull
	RealmDef<? extends AccountConfiguration> getRealmDefById(@Nonnull String realmDefId) throws UnsupportedRealmException;

	@Nonnull
	Account getAccountById(@Nonnull String realmId) throws UnsupportedRealmException;

	@Nonnull
	Account getAccountByEntity(@Nonnull Entity entity) throws UnsupportedRealmException;

	@Nonnull
	Account getAccountByEntityAware(@Nonnull EntityAware entityAware) throws UnsupportedRealmException;

	@Nonnull
	Account saveAccount(@Nonnull RealmBuilder accountBuilder) throws InvalidCredentialsException, RealmAlreadyExistsException;

	@Nonnull
	Account changeAccountState(@Nonnull Account account, @Nonnull AccountState newState);

	void removeAccount(@Nonnull String accountId);

	boolean isOneAccount();

    /*
	**********************************************************************
    *
    *                           LISTENERS
    *
    **********************************************************************
    */

	void addListener(@Nonnull JEventListener<AccountEvent> listener);

	void removeListener(@Nonnull JEventListener<AccountEvent> listener);

	void stopAllRealmConnections();

	List<AProperty> getUserProperties(@Nonnull User user, @Nonnull Context context);
}