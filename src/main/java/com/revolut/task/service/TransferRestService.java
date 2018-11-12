package com.revolut.task.service;

import com.revolut.task.exception.TransferException;
import com.revolut.task.exception.TransferErrorType;
import com.revolut.task.model.Account;
import com.revolut.task.model.Transfer;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.revolut.task.utils.JpaUtil;

/**
 * Created by Alexey on 21.10.2018.
 *
 * Main REST API for performing transfer between existing accounts
 */

@Path("/transfer")
public class TransferRestService {

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response transfer(Transfer transfer) {
        EntityManager em = JpaUtil.getEm();
        em.getTransaction().begin();
        Account fromAccount = em.find(Account.class, transfer.getFromAccount(), LockModeType.PESSIMISTIC_WRITE);
        Account toAccount = em.find(Account.class, transfer.getToAccount(), LockModeType.PESSIMISTIC_WRITE);

        if (fromAccount == null || toAccount == null)
            throw new NotFoundException();

        if (fromAccount.getCurrency() != toAccount.getCurrency())
            throw new TransferException(TransferErrorType.Status.DIFFERENT_CURRENCY,"Currency of the accounts does not matches:" +
                    " from " + fromAccount.getCurrency() +
                    " to " + toAccount.getCurrency());

        if (fromAccount.getBalance().compareTo(transfer.getSum()) < 0)
            throw new TransferException(TransferErrorType.Status.NOT_ENOUGH_MONEY,"there is not enough money in the account to complete the transaction");

        try {

            // decrease balance of the first account
            fromAccount.setBalance(fromAccount.getBalance().subtract(transfer.getSum()));
            // increase balance of the second account
            toAccount.setBalance(toAccount.getBalance().add(transfer.getSum()));
            // save the transfer for history
            em.persist(transfer);
            em.getTransaction().commit();
            return Response.accepted().build();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw new WebApplicationException("an error occurred while processing this transaction");
        } finally {
            em.close();
        }
    }
}
