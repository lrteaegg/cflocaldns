package top.badguy.exception;

public class BizException extends RuntimeException{

    public BizException(Exception e) {
        super(e);
    }
}
