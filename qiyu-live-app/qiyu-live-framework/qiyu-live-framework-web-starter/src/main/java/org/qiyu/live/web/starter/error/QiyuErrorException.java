package org.qiyu.live.web.starter.error;

import java.io.Serial;

public class QiyuErrorException extends RuntimeException{

    private int errorCode;
    private String errorMsg;

    public QiyuErrorException(int errorCode,String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public QiyuErrorException(QiyuBaseError qiyuBaseError) {
        this.errorCode = qiyuBaseError.getErrorCode();
        this.errorMsg = qiyuBaseError.getErrorMsg();
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}