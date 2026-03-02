package br.com.devisecenter.devise_center.exceptions.exception.api;

public class MissingToken extends RuntimeException {

    public MissingToken(String message) {
        super(message);
    }

}
