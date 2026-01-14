package com.example.chat.common.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

    private final CodeInterface codeInterface;

    public CustomException(CodeInterface v) {
        super(v.getMessage());
        this.codeInterface = v;
    }

    public CustomException(CodeInterface v, String message) {
        super(v.getMessage());
        this.codeInterface = v;
    }

    public ErrorCode getErrorCode() {
        return (ErrorCode) this.codeInterface;
    }

}
