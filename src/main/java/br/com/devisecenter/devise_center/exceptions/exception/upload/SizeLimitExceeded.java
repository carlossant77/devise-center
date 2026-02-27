package br.com.devisecenter.devise_center.exceptions.exception.upload;

public class SizeLimitExceeded extends RuntimeException {

    public SizeLimitExceeded(String message) {
        super(message);
    }

}
