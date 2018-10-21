package com.revolut.task.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * Created by Alexey on 20.10.2018.
 */
@Entity
@XmlRootElement
public class Account {
    @Id
    @GeneratedValue
    private Long number;
    private BigDecimal balance;
    private String owner;
    private Currency currency;

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "Account: [" +
                "number=" + number + ", " +
                "balance=" + balance + ", " +
                "owner=" + owner + ", " +
                "currency=" + currency + "]";
    }
}
