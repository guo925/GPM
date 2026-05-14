package com.gjx.gpms.common.exception;

/**
 * 业务异常
 *
 * @author gpms
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }

}