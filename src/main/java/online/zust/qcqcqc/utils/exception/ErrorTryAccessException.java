package online.zust.qcqcqc.utils.exception;

/**
 * @author qcqcqc
 * tryAccess()方法抛出的异常
 */
public class ErrorTryAccessException extends RuntimeException{
    public ErrorTryAccessException(String message) {
        super(message);
    }
}
