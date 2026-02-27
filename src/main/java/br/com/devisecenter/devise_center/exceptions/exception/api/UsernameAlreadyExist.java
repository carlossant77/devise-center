package br.com.devisecenter.devise_center.exceptions.exception.api;

public class UsernameAlreadyExist extends RuntimeException {

    public UsernameAlreadyExist(String message) {
        super(message);
    }

}
