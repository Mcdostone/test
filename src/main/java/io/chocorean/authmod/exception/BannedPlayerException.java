package io.chocorean.authmod.exception;

public class BannedPlayerException extends LoginException {

    public BannedPlayerException(String message) {
        super(message);
    }

}
