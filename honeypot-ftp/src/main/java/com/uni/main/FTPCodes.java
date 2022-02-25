package com.uni.main;

public enum FTPCodes {
    START_TRANSFER("125"),
    OPEN_DATA_CONNECTION("150"),
    WRONG_COMMAND("202"),
    SERVER_READY("220"),
    CLOSING_CONNECTION("221"),
    FINISH_TRANSFER("226"),
    PASSWORD_OK("230"),
    OK("250"),
    LOGIN_OK("331"),
    CANT_OPEN_DATA_CONNECTION("425"),
    BAD_LOGIN("430"),
    ARGS_SYNTAX_ERROR("501"),
    NOT_IMPLEMENTED("502"),
    NOT_LOGGED_IN("530"),
    FILE_UNAVAILABLE("550"),
    NAME_NOT_ALLOWED("553");

    public String getCode() {
        return code;
    }

    private String code;

    FTPCodes(String code) {
        this.code = code;
    }
}
