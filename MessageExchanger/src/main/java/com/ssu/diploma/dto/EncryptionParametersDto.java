package com.ssu.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class EncryptionParametersDto {
    private byte[] key;
    private byte[] IV;
    private String cipherSystem;
    private int mode;
}
