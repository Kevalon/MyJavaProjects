package com.ssu.diploma.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@Builder
@ToString
public class EncryptionParametersDto {
    private byte[] key;
    private byte[] IV;
    private String cipherSystem;
}
