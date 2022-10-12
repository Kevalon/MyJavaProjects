package com.main.app.word;

import java.time.LocalDate;

public class GermanWord extends Word{
    public GermanWord(String nativeWord, String englishMeaning, LocalDate repeatDay) {
        super(nativeWord, englishMeaning, repeatDay);
    }

    @Override
    public String toString() {
        return "GermanWord{" +
                "nativeWord='" + nativeWord + '\'' +
                ", englishMeaning='" + englishMeaning + '\'' +
                ", repeatDay=" + repeatDay +
                '}';
    }
}
