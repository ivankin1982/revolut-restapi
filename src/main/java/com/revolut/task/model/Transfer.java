package com.revolut.task.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * Created by Alexey on 21.10.2018.
 */

@Entity
@XmlRootElement
public class Transfer {
    @Id
    @GeneratedValue
    private Long id;
    private Long fromAccount;
    private Long toAccount;
    private BigDecimal sum;
    private Currency currency;

    public Transfer(){}

    public Transfer(Long fromAccount, Long toAccount, BigDecimal sum, Currency currency) {
        this.fromAccount = fromAccount;
        this.toAccount = toAccount;
        this.sum = sum;
        this.currency = currency;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getFromAccount() {
        return fromAccount;
    }

    public void setFromAccount(Long fromAccount) {
        this.fromAccount = fromAccount;
    }

    public Long getToAccount() {
        return toAccount;
    }

    public void setToAccount(Long toAccount) {
        this.toAccount = toAccount;
    }

    public BigDecimal getSum() {
        return sum;
    }

    public void setSum(BigDecimal sum) {
        this.sum = sum;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return "Transfer: [" +
                "fromAccount=" + fromAccount + ", " +
                "toAccount=" + toAccount + ", " +
                "sum=" + sum + ", " +
                "currency=" + currency + "]";
    }
}
