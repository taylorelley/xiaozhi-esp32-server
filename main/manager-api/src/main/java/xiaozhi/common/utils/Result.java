package xiaozhi.common.utils;

import java.io.Serializable;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import xiaozhi.common.exception.ErrorCode;

/**
 * responsedata
 * Copyright (c) Renren Opensource All rights reserved.
 * Website: https://www.renren.io
 */
@Data
@Schema(description = "response")
public class Result<T> implements Serializable {

    /**
     * code：0representssuccess，othervaluerepresentsfailed
     */
    @Schema(description = "code：0representssuccess，othervaluerepresentsfailed")
    private int code = 0;
    /**
     * messagecontent
     */
    @Schema(description = "messagecontent")
    private String msg = "success";
    /**
     * responsedata
     */
    @Schema(description = "responsedata")
    private T data;

    public Result<T> ok(T data) {
        this.setData(data);
        return this;
    }

    public Result<T> error() {
        this.code = ErrorCode.INTERNAL_SERVER_ERROR;
        this.msg = MessageUtils.getMessage(this.code);
        return this;
    }

    public Result<T> error(int code) {
        this.code = code;
        this.msg = MessageUtils.getMessage(this.code);
        return this;
    }

    public Result<T> error(int code, String msg) {
        this.code = code;
        this.msg = msg;
        return this;
    }

    public Result<T> error(String msg) {
        this.code = ErrorCode.INTERNAL_SERVER_ERROR;
        this.msg = msg;
        return this;
    }

}