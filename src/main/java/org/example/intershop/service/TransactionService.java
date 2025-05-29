package org.example.intershop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Service
public class TransactionService {

    @Transactional
    public <T> T doInTransaction(Supplier<T> action) {
        return action.get();
    }

    @Transactional
    public void doInTransaction(Runnable action) {
        action.run();
    }

    @Transactional
    public <T> void doInTransaction(Consumer<T> action, T argument) {
        action.accept(argument);
    }

    @Transactional
    public <T, R> R doInTransaction(Function<T, R> action, T input) {
       return action.apply(input);
    }
}
