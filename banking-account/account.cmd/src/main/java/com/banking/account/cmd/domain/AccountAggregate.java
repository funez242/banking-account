package com.banking.account.cmd.domain;

import com.banking.account.cmd.api.command.OpenAccountCommand;
import com.banking.account.common.events.AccountClosedEvent;
import com.banking.account.common.events.AccountOpenedEvent;
import com.banking.account.common.events.FundsDepositedEvent;
import com.banking.account.common.events.FundsWithdrawnEvent;
import com.banking.cqrs.core.domain.AggregateRoot;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
public class AccountAggregate extends AggregateRoot {
    private Boolean active;
    private double balance;
    
    public AccountAggregate(OpenAccountCommand command){
        raiseEvent(AccountOpenedEvent.builder()
                .id(command.getId())
                .accountHolder(command.getAccountHolder())
                .createdDate(new Date())
                .accountType(command.getAccountType())
                .openingBalance(command.getOpeningBalance())
                .build());
    }

    public void apply(AccountOpenedEvent event){
        this.id = event.getId();
        this.active = true;
        this.balance = event.getOpeningBalance();
    }

    public void depositFunds(double amount){
        if(this.active)
            throw new IllegalStateException("Los fondos no pueden ser depositados en esta cuenta");

        if(amount <= 0)
            throw new IllegalStateException("Eñ deposito de dineor no puede ser cero o menos que cero");

        raiseEvent(FundsDepositedEvent.builder()
                .id(this.id)
                .amount(amount).build());
    }

    public void apply(FundsDepositedEvent event){
        this.id = event.getId();
        this.balance += event.getAmount();
    }

    public void withdrawFunds(double amount){
        if (!this.active)
            throw new IllegalStateException("La cuenta bancaria está cerrada");
        raiseEvent(FundsWithdrawnEvent.builder()
                .id(this.id)
                .amount(amount)
                .build());
    }

    public void apply(FundsWithdrawnEvent event){
        this.id = event.getId();
        this.balance  -= event.getAmount();
    }

    public void closeAccount(){
        if(!active)
            throw new IllegalStateException("La cuenta de banco ya se encuentra cerrada");
        raiseEvent(AccountClosedEvent.builder()
                .id(this.id)
                .build());
    }
    public void apply(AccountClosedEvent event){
        this.id = event.getId();
        this.active = false;
    }
}
