package de.plimplom.addonreader.util;

public class ApplicationException extends Exception{
    private final ErrorCode errorCode;

    public ApplicationException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        CONFIGURATION_ERROR,
        NETWORK_ERROR,
        FILE_ACCESS_ERROR,
        PARSING_ERROR,
        SECURITY_ERROR,
        UNKNOWN_ERROR
    }
}
