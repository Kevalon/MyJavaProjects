import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.net.QuotedPrintableCodec;


public class SpamFilter {
    public static int spam_counter = 0;
    public static boolean spam = false;

    public static String Subject;
    public static String EncodedText;
    public static String Text;

    public static void SubjectLengthTest(String email) throws DecoderException {
        int start = email.indexOf("Subject: ");
        if (start == -1) return;
        int offset = 9;
        char s = email.charAt(start + offset);
        String Title = "";
        while (s != '\n' && s != '\r') {
            Title += s;
            offset++;
            s = email.charAt(start + offset);
        }
        char encoding = Title.charAt(8);
        String clean = Title.substring(10, Title.length() - 2);
        if (encoding == 'B') {
            byte[] decodedBytes = Base64.getDecoder().decode(clean);
            Subject = new String(decodedBytes);
        }
        if (encoding == 'Q') {
            QuotedPrintableCodec codec = new QuotedPrintableCodec();
            Subject = codec.decode(clean);
        }
        if (encoding != ' ') {
            Subject = Title;
        }
        if (Subject == null) return;
        if (Subject.length() > 120) {
            System.out.println("Признак спама: больше 120 символов в Title");
            spam_counter++;
            return;
        }
        int count = 0;
        for (int i = 0; i < Subject.length(); i++) {
            if (Subject.charAt(i) == ' ')
                count++;
        }
        if (count > 12) {
            System.out.println("Признак спама: слишком много слов в Title");
            spam_counter++;
        }
    }

    public static void decodeText(String email) {
        int pos = email.indexOf("Content-Type: text/plain");
        if (pos == -1) {
            pos = email.indexOf("Content-Type: text/PLAIN");
            if (pos == -1)
                return;
        }
        int encodingPos = email.indexOf("Content-Transfer-Encoding: ", pos);
        if (encodingPos == -1) {
            encodingPos = email.indexOf("Content-Transfer-Encoding: ");
        }
        int offset = "Content-Transfer-Encoding: ".length();
        char s = email.charAt(encodingPos + offset);
        String encoding = "";
        while (s != '\n' && s != '\r' && s != ' ') {
            encoding += s;
            offset++;
            s = email.charAt(encodingPos + offset);
        }
        if (encoding.equals("quoted-printable")) {
            int textBegin = 0;
            if (pos < encodingPos)
                textBegin = email.indexOf('=', encodingPos + offset);
            if (pos > encodingPos)
                textBegin = email.indexOf('=', pos + offset);
            int textEnd = email.indexOf("--", textBegin);
            if (textEnd == -1) {
                textEnd = email.indexOf("\r\n\r\n", textBegin);
            }
            String codedText = "";
            offset = 0;
            s = email.charAt(textBegin);
            if (textEnd != -1)
                while (textBegin + offset < textEnd) {
                    codedText += s;
                    offset++;
                    if (textBegin + offset < email.length())
                        s = email.charAt(textBegin + offset);
                }
            else
                while (textBegin + offset < email.length()) {
                    codedText += s;
                    offset++;
                    if (textBegin + offset < email.length())
                        s = email.charAt(textBegin + offset);
                }

            EncodedText = codedText;

            codedText = codedText.replaceAll("=\n", "");
            codedText = codedText.replaceAll("=\r", "");
            codedText = codedText.replaceAll("\n", "");
            codedText = codedText.replaceAll("\r", "");
            codedText = codedText.replaceAll("= =", "=");
            codedText = codedText.replaceAll("==", "=");
            codedText = codedText.replaceAll("<[^<]*>", "");
            codedText = codedText.replaceAll("&[^&]*;", " ");
            if (codedText.charAt(codedText.length() - 1) == '=')
                codedText = codedText.substring(0, codedText.length() - 1);
            QuotedPrintableCodec codec = new QuotedPrintableCodec();
            try {
                Text = codec.decode(codedText);
            }
            catch (DecoderException e) {
                System.out.println("Ошибка декодирования текста");
            }
        }
        if (encoding.equals("base64")) {
            int textBegin = email.indexOf("\r\n\r\n", pos + offset) + 4;
            int textEnd = email.indexOf("\r\n", textBegin);
            EncodedText = email.substring(textBegin, textEnd);
            Text = new String(Base64.getDecoder().decode(EncodedText.getBytes()));
        }
        if (encoding.equals("7bit") && email.contains("X-Mailru-Intl-Transport")) {
            int textBegin = email.indexOf("\r\n\r\n", pos + offset) + 4;
            EncodedText = email.substring(textBegin);
            Text = EncodedText;
        }
    }

    public static void compareSubjectAndText() {
        if (Subject == null || Text == null)
            return;
        if (Subject.equals("") || Text.equals(""))
            return;
        String[] subjectWords = Subject.split(" ");
        String[] textWords = Text.split(" ");
        for (int i = 0; i < subjectWords.length; i++) {
            subjectWords[i] = subjectWords[i].replaceAll("[^а-яА-Я]", "").toLowerCase();
        }
        for (int i = 0; i < textWords.length; i++) {
            textWords[i] = textWords[i].replaceAll("[^а-яА-Я]", "").toLowerCase();
        }
        for (String subjectWord : subjectWords) {
            for (String textWord : textWords)
                if (subjectWord.equals(textWord) && !subjectWord.equals("") && textWord.length() > 2)
                    return;
        }
        spam_counter++;
        System.out.println("Признак спама: не найдено совпадений слов между темой письма и текстом");
    }

    public static void checkDescription(String email) {
        int pos = email.indexOf("\"description\"");
        if (pos == -1) return;
        pos = email.indexOf("content=\"") + "content=\"".length();
        int endpos = email.indexOf("\"", pos + 1);
        String description = email.substring(pos, endpos);
        if (description.length() > 250) {
            spam_counter++;
            System.out.println("Признак спама: description длиннее 250 символов");
            return;
        }
        int space_count = 0;
        for (int i = 0; i < description.length(); i++) {
            if (description.charAt(i) == ' ')
                space_count++;
        }
        if (space_count > 39) {
            spam_counter++;
            System.out.println("Признак спама: description длиннее 40 слов");
        }
    }

    public static void checkKeywords(String email) {
        int pos = email.indexOf("\"keywords\"");
        if (pos == -1) return;
        pos = email.indexOf("content=\"") + "content=\"".length();
        int endpos = email.indexOf("\"", pos + 1);
        String keywords = email.substring(pos, endpos);
        if (keywords.length() > 250) {
            spam_counter++;
            System.out.println("Признак спама: keywords длиннее 250 символов");
            return;
        }
        int space_count = 0;
        for (int i = 0; i < keywords.length(); i++) {
            if (keywords.charAt(i) == ' ')
                space_count++;
        }
        if (space_count > 39) {
            spam_counter++;
            System.out.println("Признак спама: keywords длиннее 40 слов");
        }
    }

    public static void checkReceiver(String email) {
        int ToPos = email.indexOf("\nTo: ") + 5;
        int CcPos = email.indexOf("\nCc: ") + 5;
        int AppPos = email.indexOf("Apparently-To: ");
        if (AppPos != -1) {
            spam_counter++;
            spam = true;
            System.out.println("Признак спама: найден заголовок Apparently-to:");
            return;
        }
        int Messageidpos = email.indexOf("Message-ID: ");
        if (Messageidpos == -1) {
            Messageidpos = email.indexOf("Message-id: ");
            if (Messageidpos == -1)
                Messageidpos = email.indexOf("Message-Id: ");
        }
        if (Messageidpos == -1) return;
        int resPos = CcPos - 5 == -1 ? Messageidpos : CcPos;
        String To = email.substring(ToPos, resPos);
        int mail_amount = 0;
        for (int i = 0; i < To.length(); i++) {
            if (To.charAt(i) == '@')
                mail_amount++;
        }
        if (CcPos - 5 != -1) {
            mail_amount++;
            String notFullCc = email.substring(CcPos);
            char cur = ' ', pred = notFullCc.charAt(0);
            int p = 2;
            while (cur != '\r' || pred != '>') {
                if (cur == '@')
                    mail_amount++;
                pred = cur;
                cur = notFullCc.charAt(p);
                p++;
            }
        }
        if (mail_amount > 20) {
            spam_counter++;
            System.out.println("Признак спама: число получателей > 20");
        }

    }

    public static void checkBackAddress(String email) {
        int RPpos = email.indexOf("Return-Path: ") + "Return-path: ".length();
        if (RPpos == 12)
            RPpos = email.indexOf("Return-path: ") + "Return-path: ".length();
        int RPposEnd = email.indexOf('\n', RPpos);
        if (RPpos == 12) {
            spam_counter++;
            spam = true;
            System.out.println("Признак спама: нет обратного адреса");
            return;
        }
        String ReturnPath = email.substring(RPpos, RPposEnd);
        if (ReturnPath.indexOf('@') == -1) {
            spam_counter++;
            spam = true;
            System.out.println("Признак спама: нет обратного адреса");
            return;
        }
        int firstDot = ReturnPath.indexOf('.');
        if (ReturnPath.indexOf('.', firstDot + 1) != -1) {
            spam_counter++;
            spam = true;
            System.out.println("Признак спама: некорректный обратный адрес");
        }
    }

    public static void checkIfImageOnly(String email) {
        if (email.contains("Content-Type: image/")) {
            if (Text == null) {
                spam_counter++;
                System.out.println("Признак спама: в теле письма только картинка");
                return;
            }
            if (Text.equals("")) {
                spam_counter++;
                System.out.println("Признак спама: в теле письма только картинка");
            }
        }
    }

    public static void checkBCC(String email) {
        if (email.contains("Bcc: ")) {
            spam_counter++;
            spam = true;
            System.out.println("Признак спама: найден заголовок Bcc:");
        }
    }

    public static void checkWordDensity(String text) {
        if (text == null) return;
        if (text.equals("")) return;
        String[] textWords = text.split(" ");
        int total_amount = 0;
        for (int i = 0; i < textWords.length; i++) {
            if (!textWords[i].equals(""))
                total_amount++;
            textWords[i] = textWords[i].replaceAll("[^а-яА-Я]", "").toLowerCase();
        }
        HashSet<String> uniqueWords = new HashSet<>();
        HashMap<String, Integer> extraWords = new HashMap<>();
        for (String word : textWords) {
            if (!word.equals(" ")) {
                if (uniqueWords.contains(word)) {
                    if (extraWords.containsKey(word))
                        extraWords.replace(word, extraWords.get(word) + 1);
                    else
                        extraWords.put(word, 2);
                }
                else uniqueWords.add(word);
            }
        }
        for (HashMap.Entry<String, Integer> entry : extraWords.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            if (((double)value / (double)total_amount) * 100.0 > 4.0 && key.length() > 2) {
                System.out.println("Признак спама: частота встречания слова \"" + key + "\" больше 4%");
                spam_counter++;
                return;
            }
        }
    }

    public static void checkTagsPercentage(String text) {
        if (text == null) return;
        if (text.equals("")) return;
        ArrayList tags = new ArrayList();
        tags.add("b");
        tags.add("strong");
        tags.add("em");
        tags.add("i");
        tags.add("u");
        tags.add("h1");
        tags.add("h2");
        tags.add("h3");
        tags.add("h4");
        tags.add("h5");
        tags.add("h6");
        boolean tag = false;
        String tagStr = "";
        int totalTagged = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '<') {
                tag = true;
                continue;
            }
            if (tag) {
                if (text.charAt(i) == '>') {
                    tag = false;
                    if (tags.contains(tagStr)) {
                        i++;
                        while (text.charAt(i) != '<' || text.charAt(i + 2) != tagStr.charAt(0)) {
                            totalTagged++;
                            i++;
                        }
                        i = text.indexOf('>', i);
                    }
                    tagStr = "";
                    if (i != text.length() - 1) i++;
                }
                else {
                    tagStr += text.charAt(i);
                }
            }
        }
        if (((double)totalTagged / (double)text.length()) * 100.0 > 30.0) {
            spam_counter++;
            System.out.println("Признак спама: злоупотребление тегами логического/физического выделения");
        }
    }

    public static void checkMessageID(String email) {
        int Messageidpos = email.indexOf("Message-ID: ");
        if (Messageidpos == -1) {
            Messageidpos = email.indexOf("Message-id: ");
            if (Messageidpos == -1) {
                Messageidpos = email.indexOf("Message-Id: ");
                if (Messageidpos == -1) {
                    spam = true;
                    spam_counter++;
                    System.out.println("Признак спама: не найден заголовок Message-Id");
                    return;
                }
            }
        }
        int endPos = email.indexOf("\n", Messageidpos);
        String Message = email.substring(Messageidpos, endPos);
        if (!Message.contains("@")) {
            spam = true;
            spam_counter++;
            System.out.println("Признак спама: неправильный вид Message-Id");
        }
    }

    public static void checkxmailer(String email) {
        if (email.contains("X-Mailer: ") || email.contains("X-mailer: ")) {
            spam_counter++;
            System.out.println("Признак спама: при отправлении письма использовалась сторонняя программа");
        }
    }

    public static void main(String []args) throws DecoderException {
        Scanner in = new Scanner(System.in);
        System.out.println("Введите полное название файла:");
        String pathStr = in.nextLine();
        String email = "";

        try {
            FileReader inFile = new FileReader(pathStr);
            int s;
            while ((s = inFile.read()) != -1) {
                email += (char)s;
            }
        }
        catch (IOException ex) {
            System.out.println("Ошибка чтения пути");
            return;
        }
        if (email.indexOf("Received: ") == -1) {
            try
            {
                email = new String(Base64.getMimeDecoder().decode(email.getBytes()));
            }
            catch (Exception ex) {
                QuotedPrintableCodec codec = new QuotedPrintableCodec();
                try {
                    email = codec.decode(email);
                }
                catch (DecoderException e) {
                    System.out.println("Ошибка декодирования текста");
                    return;
                }
            }
        }
        SubjectLengthTest(email);
        decodeText(email);
        compareSubjectAndText();
        checkDescription(email);
        checkKeywords(email);
        checkReceiver(email);
        checkBackAddress(email);
        checkIfImageOnly(email);
        checkBCC(email);
        checkWordDensity(Text);
        checkTagsPercentage(EncodedText);
        checkMessageID(email);
        checkxmailer(email);

        if (spam) {
            System.out.println("Данное письмо - спам");
        }
        else if (spam_counter == 0) {
            System.out.println("Данное письмо, вероятно, не спам");
        }
        else if (spam_counter == 1) {
            System.out.println("Данное письмо с малой вероятностью может быть спамом");
        }
        else {
            System.out.println("Данное письмо, вероятнее всего, спам");
        }
        //System.out.println(spam_counter);
    }
}
