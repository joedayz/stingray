package no.cantara.stingray.sql;

public class StingraySqlException extends RuntimeException {

    public StingraySqlException() {
    }

    public StingraySqlException(String message) {
        super(message);
    }

    public StingraySqlException(String message, Throwable cause) {
        super(message, cause);
    }

    public StingraySqlException(Throwable cause) {
        super(cause);
    }

    public StingraySqlException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
