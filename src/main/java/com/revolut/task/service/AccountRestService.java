package com.revolut.task.service;

import com.revolut.task.model.Account;
import com.revolut.task.utils.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

/**
 * Created by Alexey on 20.10.2018.
 *
 * REST API for creating and getting any accounts
 */

@Path("/account")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AccountRestService {

    @Context
    private UriInfo uriInfo;

    @GET
    @Path("{number}")
    public Response get(@PathParam("number") Long number) {
        EntityManager em = JpaUtil.getEm();
        Account account = em.find(Account.class, number);
        em.close();
        if (account == null)
            throw new NotFoundException();
        return Response.ok(account).build();
    }

    @POST
    public Response add(Account account){
        EntityManager em = JpaUtil.getEm();
        em.getTransaction().begin();
        em.persist(account);
        em.getTransaction().commit();
        em.close();
        URI accountUri = uriInfo.getAbsolutePathBuilder().path(account.getNumber().toString()).build();
        return Response.created(accountUri).build();
    }

    @DELETE
    @Path("{number}")
    public Response delete(@PathParam("number") Long number){
        EntityManager em = JpaUtil.getEm();
        em.getTransaction().begin();
        Account account = em.find(Account.class, number, LockModeType.PESSIMISTIC_WRITE);
        if (account == null) {
            em.close();
            throw new NotFoundException();
        }
        em.remove(account);
        em.getTransaction().commit();
        em.close();
        return Response.noContent().build();
    }

}
