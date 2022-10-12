package com.main.app;

import com.main.app.word.GermanWord;
import com.main.app.word.Word;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {
    /* TODO:
        1: Create class for one word - it contains german word, english word, date to repeat - done
        2: Create a file that contains all words - read words from this file - done
        3: Create a language selector (for now just set German as default and turn off the method) - done
        4: On start the app loads all the files from db that need to be repeated (scan the file - skip the ones that
        do not need to be repeated: we don't want to keep them in memory)
        5: The app has several modes for repeat:
            1. Choose correct English translation out of 4
            2. Choose correct German word by translation out of 4
            3. Spell German word by seeing translation
            4. Spell English word by seeing German word
        6: 4 modes are randomly assigned to each word from collected from db and for the first two there might be
        additional action done (each English or German word in 1 and 2 has to have at least 3 another options!)
        7: The progress of words: tomorrow, in 2 days, in 3 days, in a week, in two weeks, in a month, in 3 months, complete
        8: When the word is complete it is kept in a separate file and is erased from "learning" db.
    */
    private static String language = "german";
    private static List<Word> words = new ArrayList<>();

    private static void readAllWords() throws IOException {
        for (String line : Files.readAllLines(Paths.get("./src/main/resources/" + language + "Database.txt"))) {
            String orig = line.substring(0, line.indexOf('-') - 1);
            String translation = line.substring(line.indexOf('-') + 2, line.lastIndexOf('-') - 1);
            String date = line.substring(line.lastIndexOf('-') + 2);
            LocalDate resultDate = date.equals("Never") ? null : LocalDate.parse(date);

            if (language.equals("german")) {
                words.add(new GermanWord(orig, translation, resultDate));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);
//        System.out.println("Please input language");
//        language = in.next();
        readAllWords();
        System.out.println(words);
    }
}
