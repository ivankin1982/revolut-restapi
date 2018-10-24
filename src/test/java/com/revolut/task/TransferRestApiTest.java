package com.revolut.task;

import com.revolut.task.config.JerseyTestConfig;
import com.revolut.task.exception.TransferErrorType;
import com.revolut.task.model.Account;
import com.revolut.task.model.Currency;
import com.revolut.task.model.Transfer;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Alexey on 21.10.2018.
 *
 * Integrated tests for TransferRestService
 */
public class TransferRestApiTest extends JerseyTest {
    private final Logger log = LoggerFactory.getLogger(TransferRestApiTest.class);
    private Map<String, Account> preparedAccounts = new HashMap<>();

    @Override
    public Application configure() {
        return JerseyTestConfig.getConfig();
    }

    /**
     * Create 4 different accounts for testing purpose
     * and put them into the preparedAccounts map for keeping the account numbers
     */
    @Before @Test
    public void createSomeAccounts() {
        Map<String, Account> accounts = new HashMap<>();
        accounts.put("fromAccountRur", createAccount("some owner id", 1000, Currency.RUR));
        accounts.put("toAccountRur", createAccount("some owner id", 2000, Currency.RUR));
        accounts.put("fromAccountUsd", createAccount("some owner id", 1000, Currency.USD));
        accounts.put("toAccountUsd", createAccount("some owner id", 2000, Currency.USD));

        accounts.forEach((key, account) -> {
            // Save the account by Rest API
            Response response = target("/account").request()
                    .post(Entity.entity(account, MediaType.APPLICATION_JSON));
            assertEquals("Should return status CREATED", Response.Status.CREATED.toString(), response.getStatusInfo().toString());
            assertNotNull("Should return location", response.getLocation());
            URI accountUri = response.getLocation();

            // Extract created account number from URI
            String path = accountUri.getPath();
            String accountIdStr = path.substring(path.lastIndexOf('/') + 1);

            // Get the account by Rest API
            response = target("/account/" + accountIdStr).request().get();
            Account accountFromRest = response.readEntity(Account.class);
            assertNotNull("Account should not be null", accountFromRest);
            assertEquals("Should return status OK", Response.Status.OK.toString(), response.getStatusInfo().toString());
            compareAccount(account, accountFromRest);
            // Put into map for other tests
            preparedAccounts.put(key, accountFromRest);
        });
    }

    /**
     * Perform transfer in RUR between two valid accounts
     */
    @Test
    public void doTransferInRur() {
        Account from = preparedAccounts.get("fromAccountRur");
        Account to = preparedAccounts.get("toAccountRur");
        BigDecimal sum = BigDecimal.valueOf(350);
        doTransfer(from, to, sum, Currency.RUR);
    }

    /**
     * Perform transfer in USD between two valid accounts
     */
    @Test
    public void doTransferInUsd() {
        Account from = preparedAccounts.get("fromAccountUsd");
        Account to = preparedAccounts.get("toAccountUsd");
        BigDecimal sum = BigDecimal.valueOf(350);
        doTransfer(from, to, sum, Currency.USD);
    }

    /**
     * Perform transaction with incorrect account number
     * Should return status NOT_FOUND
     */
    @Test
    public void shouldReturnNotFoundStatus() {
        Transfer transfer = new Transfer(1L, 100L, BigDecimal.valueOf(100), Currency.RUR);
        Response response = target("/transfer").request().post(Entity.entity(transfer, MediaType.APPLICATION_JSON));
        assertEquals("Should return status NOT_FOUND", Response.Status.NOT_FOUND.getStatusCode(), response.getStatusInfo().getStatusCode());
        log.trace("when a request with wrong account number then return: '{}' error", response.getStatusInfo().getReasonPhrase());
    }

    /**
     * Perform transaction between two valid account,
     * but their have different currency.
     * Should return detailed custom error
     */
    @Test
    public void shouldReturnCustomCurrencyResponse() {
        Long from = preparedAccounts.get("fromAccountRur").getNumber();
        Long to = preparedAccounts.get("toAccountUsd").getNumber();
        BigDecimal sum = BigDecimal.valueOf(350);
        // do transfer
        Transfer transfer = new Transfer(from, to, sum, Currency.RUR);
        Response response = target("/transfer").request().post(Entity.entity(transfer, MediaType.APPLICATION_JSON));

        assertEquals("Should return custom status DIFFERENT_CURRENCY", TransferErrorType.Status.DIFFERENT_CURRENCY.getStatusCode(), response.getStatusInfo().getStatusCode());
        assertEquals("Should return custom error message", "Currency of the accounts does not matches: from RUR to USD", response.getStatusInfo().getReasonPhrase());
        log.trace("when a request with different currency then return: '{}' error", response.getStatusInfo().getReasonPhrase());
    }

    /**
     * Perform transaction between two valid account,
     * but there is not enough money for this operation
     * Should return detailed custom error
     */
    @Test
    public void shouldReturnLackOfMoneyResponse() {
        Long from = preparedAccounts.get("fromAccountRur").getNumber();
        Long to = preparedAccounts.get("toAccountRur").getNumber();
        BigDecimal sum = BigDecimal.valueOf(3500);
        // do transfer
        Transfer transfer = new Transfer(from, to, sum, Currency.RUR);
        Response response = target("/transfer").request().post(Entity.entity(transfer, MediaType.APPLICATION_JSON));

        assertEquals("Should return custom status NOT_ENOUGH_MONEY", TransferErrorType.Status.NOT_ENOUGH_MONEY.getStatusCode(), response.getStatusInfo().getStatusCode());
        assertEquals("Should return custom error message", "there is not enough money in the account to complete the transaction", response.getStatusInfo().getReasonPhrase());
        log.trace("when there is not enough money on account then return: '{}' error", response.getStatusInfo().getReasonPhrase());
    }

    private void doTransfer(Account from, Account to, BigDecimal sum, Currency currency){
        Transfer transfer = new Transfer(from.getNumber(), to.getNumber(), sum, currency);
        Response response = target("/transfer").request().post(Entity.entity(transfer, MediaType.APPLICATION_JSON));
        assertEquals("Should return status ACCEPTED", Response.Status.ACCEPTED.getStatusCode(), response.getStatusInfo().getStatusCode());
        log.trace("Transfer {} {} from account {} to account {} was done", sum, currency, from.getNumber(), to.getNumber());
        // check the balance of the accounts after transfer
        response = target("/account/" + from.getNumber()).request().get();
        Account accountFrom = response.readEntity(Account.class);
        assertTrue("The first account should contain 650 " + currency, BigDecimal.valueOf(650).compareTo(accountFrom.getBalance()) == 0);
        response = target("/account/" + to.getNumber()).request().get();
        Account accountTo = response.readEntity(Account.class);
        assertTrue("The first account should contain 650 " + currency, BigDecimal.valueOf(2350).compareTo(accountTo.getBalance()) == 0);
        log.trace("Account N{} was {} {}, became {} {} ", from.getNumber(), from.getBalance(), from.getCurrency(), accountFrom.getBalance(), accountFrom.getCurrency());
        log.trace("Account N{} was {} {}, became {} {} ", to.getNumber(), to.getBalance(), to.getCurrency(), accountTo.getBalance(), accountTo.getCurrency());
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
    }

}
