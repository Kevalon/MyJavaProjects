package com.ssu.diploma;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.Scanner;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class Main {
    public static void main(String[] args)throws Exception {

        // todo: GOST

        Scanner in = new Scanner(System.in);
        System.out.println("Sender of Receiver?");
        if (in.next().toLowerCase(Locale.ROOT).equals("sender")) {
            Sender.send(in);
        } else {
            Receiver.receive();
        }
        in.close();
    }
}
