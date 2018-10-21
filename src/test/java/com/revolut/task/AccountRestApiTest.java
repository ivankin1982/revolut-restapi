package com.revolut.task;

import com.revolut.task.config.JerseyTestConfig;
import com.revolut.task.model.Account;
import com.revolut.task.model.Currency;
import com.revolut.task.utils.JpaUtil;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;


/**
 * Created by Alexey on 20.10.2018.
 *
 * Integrated tests for AccountRestService
 */
public class AccountRestApiTest extends JerseyTest {
    private final Logger log = LoggerFactory.getLogger(AccountRestApiTest.class);
    private Long accountNumber;
    private Account firstAccount;

    @Override
    public Application configure() {
        return JerseyTestConfig.getConfig();
    }

    /**
     * Create the first account for some tests
     */
    @Before
    public void createFirstAccount(){
        firstAccount = createAccount("some owner", 1000, Currency.RUR);
        // Send the account by POST method
        Response response = target("/account").request()
                .post(Entity.entity(firstAccount, MediaType.APPLICATION_JSON));
        URI accountUri = response.getLocation();
        // Get the number from the created account
        String path = accountUri.getPath();
        String accountIdStr = path.substring(path.lastIndexOf('/') + 1);
        accountNumber = Long.parseLong(accountIdStr);

    }

    /**
     * Create an account by POST method
     */
    @Test
    public void shouldAddAccount() {
        Account account = createAccount("some owner", 2000, Currency.RUR);

        Response response = target("/account").request()
                .post(Entity.entity(account, MediaType.APPLICATION_JSON));
        assertEquals("Should return status CREATED", Response.Status.CREATED.toString(), response.getStatusInfo().toString());
        assertNotNull("Should return location", response.getLocation());
        URI accountUri = response.getLocation();
        log.trace("account was created by POST method on {}", accountUri);

        // Check if the account exists in DB
        String path = accountUri.getPath();
        String accountIdStr = path.substring(path.lastIndexOf('/') + 1);
        Long accountId = Long.parseLong(accountIdStr);
        EntityManager em = JpaUtil.getEm();
        Account accountFromDb = em.find(Account.class, accountId);
        em.close();
        log.trace("account was fetched from DB = {}", accountFromDb);
        compareAccount(account, accountFromDb);
    }

    /**
     * Fetch the account by GET method
     */
    @Test
    public void shouldGetAccount(){
        Response response = target("/account/" + accountNumber).request().get();
        Account accountFromRest = response.readEntity(Account.class);
        assertEquals("Should return status OK", Response.Status.OK.toString(), response.getStatusInfo().toString());
        log.trace("account was obtained by Get method = {}", accountFromRest);
        compareAccount(firstAccount, accountFromRest);
    }

    /**
     * Delete the account by DELETE method
     */
    @Test
    public void shouldDeleteAccount(){
        Response response = target("/account/" + accountNumber).request().delete();
        assertEquals("Should return status NO_CONTENT", Response.Status.NO_CONTENT.toString(), response.getStatusInfo().toString());
        // Check if account exists in DB
        EntityManager em = JpaUtil.getEm();
        Account aDel = em.find(Account.class, accountNumber);
        em.close();
        assertNull("Account should not exist in DB", aDel);
        log.trace("account was deleted by DELETE method");
    }

    private Account createAccount(String owner, long balance, Currency currency) {
        Account account = new Account();
        account.setOwner(owner);
        account.setBalance(BigDecimal.valueOf(balance));
        account.setCurrency(currency);
        return account;
    }

    private void compareAccount(Account account1, Account account2) {
        assertTrue("Account balance should be equals", account1.getBalance().compareTo(account2.getBalance()) == 0);
        assertEquals("Account currency should be equals", account1.getCurrency(), account2.getCurrency());
        assertEquals("Account owner should be equals", account1.getOwner(), account2.getOwner());
        log.trace("Fetched account matches the sample account");
    }
}
