package com.main.app.word;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class Word {
    protected String nativeWord;
    protected String englishMeaning;
    protected LocalDate repeatDay;
}
