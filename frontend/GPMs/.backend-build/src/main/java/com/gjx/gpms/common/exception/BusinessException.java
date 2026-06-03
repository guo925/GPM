package com.gjx.gpms.common.exception;

/**
 * 业务异常
 *
 * @author gpms
 */
public class BusinessException extends RuntimeException {

    /**
     * 处理BusinessException相关逻辑。
     */
    public BusinessException(String message) {
        super(message);
    }

}