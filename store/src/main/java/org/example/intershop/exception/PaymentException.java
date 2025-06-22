package org.example.intershop.exception;

public class PaymentException extends RuntimeException{
    public PaymentException(String errorBody) {
        super(errorBody);
    }
}
