package br.com.devisecenter.devise_center.exceptions.exception.validation;

public class UnauthorizedOperation extends RuntimeException {

    public UnauthorizedOperation(String message) {
        super(message);
    }

}
