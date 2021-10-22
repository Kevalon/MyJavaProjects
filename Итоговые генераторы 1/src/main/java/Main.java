import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.apache.commons.cli.*;

public class Main {

    public static void main(String[] args) {
        BigInteger module = BigInteger.ZERO;
        String configPath = "";
        int number = 0;

        if (args.length == 0) {
            System.err.println("Отсутствуют аргументы программы");
            System.exit(1);
        }


        //pasring arguments from cmd

        Options options = new Options();
        Option help = new Option("h", "help", false,
                "подробное информационное сообщение о параметрах программы.");
        help.setRequired(false);
        options.addOption(help);

        Option xml = new Option("c", "xml file", true,
                "<path> - путь к файлу конфигурации.");
        xml.setRequired(false);
        options.addOption(xml);

        Option numbers = new Option("n", "amount of numbers", true,
                "<number> - количество генерируемых значений. Неотрицательное целое число.");
        numbers.setRequired(false);
        options.addOption(numbers);

        Option mod = new Option("m", "module", true,
                "<module> - на выход подаются числа, взятые по указанному модулю. Например, если указано, " +
                        "что -m 10, а алгоритм сгенерировал число 123, то на выход должно быть подано 6" +
                        "(= 123 mod 10). Неотрицательное число. Если не указан, то сгенерированные числа" +
                        "выводятся в изначальном виде.");
        mod.setRequired(false);
        options.addOption(mod);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.err.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        if (cmd.hasOption("h")) {
            System.out.println("-n " + numbers.getDescription());
            System.out.println("-c " + xml.getDescription());
            System.out.println("-m " + mod.getDescription());
            System.out.println("-h " + help.getDescription());
            return;
        }
        else {
            if (cmd.hasOption("n") && cmd.hasOption("c")) {
                if (cmd.hasOption("m"))
                    module = new BigInteger(cmd.getOptionValue("m"));
                configPath = cmd.getOptionValue("c");
                number = Integer.parseInt(cmd.getOptionValue("n"));
            }
            else {
                System.err.println("Неверный ввод параметров консоли");
                System.exit(1);
            }

        }

        File f = new File(configPath);
        if (!f.exists() || f.isDirectory()) {
            System.err.println("Указанный файл кофигурации не найден");
            System.exit(1);
        }

        // Parsing XML

        if (!XSDValidator.validate(configPath)) {
            System.err.println("Неверные данные в xml файле");
            System.exit(1);
        }


        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(configPath);
            Node root = document.getDocumentElement();
            NodeList params = root.getChildNodes();
            switch (params.item(1).getNodeName()) {
                case ("add"):
                    NodeList addParams = params.item(1).getChildNodes();
                    ArrayList<BigInteger> a = new ArrayList<>();
                    String factors = addParams.item(1).getChildNodes().item(0).getTextContent();
                    String[] factorsAr = factors.split(" ");
                    for (String el : factorsAr) {
                        a.add(new BigInteger(el));
                    }
                    ArrayList<BigInteger> x = new ArrayList<>();
                    String initialValues = addParams.item(3).getChildNodes().item(0).getTextContent();
                    String[] tmp = initialValues.split(" ");
                    for (String el : tmp) {
                        x.add(new BigInteger(el));
                    }
                    BigInteger c, m;
                    c = new BigInteger(addParams.item(5).getChildNodes().item(0).getTextContent());
                    m = new BigInteger(addParams.item(7).getChildNodes().item(0).getTextContent());
                    Additive.generate(number, a, x, c, m, module);

                    break;
                case ("lc"):
                    NodeList lcParams = params.item(1).getChildNodes();
                    BigInteger aLc = new BigInteger(lcParams.item(1).getChildNodes().item(0)
                            .getTextContent());
                    BigInteger cLc = new BigInteger(lcParams.item(3).getChildNodes().item(0)
                            .getTextContent());
                    BigInteger mLc = new BigInteger(lcParams.item(5).getChildNodes().item(0)
                            .getTextContent());
                    BigInteger x0Lc = new BigInteger(lcParams.item(7).getChildNodes().item(0)
                            .getTextContent());
                    LinearCongruent.generate(number, aLc, cLc, mLc, x0Lc, module);
                    break;
                case ("lfsr"):
                    NodeList lfsrParams = params.item(1).getChildNodes();
                    BigInteger YLfsr = new BigInteger(lfsrParams.item(1).getChildNodes().item(0)
                            .getTextContent());
                    int pLfsr = Integer.parseInt(lfsrParams.item(3).getChildNodes().item(0)
                            .getTextContent());
                    String values = lfsrParams.item(5).getChildNodes().item(0).getTextContent();
                    tmp = values.split(" ");
                    int[] jLfsr = new int[tmp.length];
                    for (int i = 0; i < jLfsr.length; i++) {
                        jLfsr[i] = Integer.parseInt(tmp[i]);
                    }
                    int sLfsr = Integer.parseInt(lfsrParams.item(7).getChildNodes().item(0)
                            .getTextContent());
                    RSLOS.generate(number, YLfsr, pLfsr, jLfsr, sLfsr, module);
                    break;
                case ("fp"):
                    NodeList fpParams = params.item(1).getChildNodes();
                    BigInteger YFp = new BigInteger(fpParams.item(1).getChildNodes().item(0)
                            .getTextContent());
                    int pFp = Integer.parseInt(fpParams.item(3).getChildNodes().item(0)
                            .getTextContent());
                    int q1Fp = Integer.parseInt(fpParams.item(5).getChildNodes().item(0)
                            .getTextContent());
                    int q2Fp = Integer.parseInt(fpParams.item(7).getChildNodes().item(0)
                            .getTextContent());
                    int q3Fp = Integer.parseInt(fpParams.item(9).getChildNodes().item(0)
                            .getTextContent());
                    int wFp = Integer.parseInt(fpParams.item(11).getChildNodes().item(0)
                            .getTextContent());
                    FiveParams.generate(number, YFp, pFp, q1Fp, q2Fp, q3Fp, wFp, module);
                    break;
                case ("nfsr"):
                    NodeList nfsrParams = params.item(1).getChildNodes();

                    ArrayList<Integer> pNfsr = new ArrayList<>();
                    ArrayList<ArrayList<Integer>> XNfsr = new ArrayList<>();
                    ArrayList<ArrayList<Integer>> jNfsr = new ArrayList<>();
                    ArrayList<BigInteger> anotherJ = new ArrayList<>();

                    NodeList generators = nfsrParams.item(1).getChildNodes();
                    int p = 0;
                    for (int i = 1; i < generators.getLength(); i+=2) {
                        pNfsr.add(Integer.parseInt(generators.item(i).getChildNodes().item(3)
                                .getTextContent()));
                        BigInteger X0 = new BigInteger(generators.item(i).getChildNodes().item(1)
                                .getTextContent());
                        String X0Str = X0.toString(2);
                        while (X0Str.length() < pNfsr.get(p))
                            X0Str = "0" + X0Str;
                        ArrayList<Integer> tmpX = new ArrayList<>();
                        for (int it = pNfsr.get(p) - 1; it >= 0; it--) {
                            tmpX.add(X0Str.charAt(it) - '0');
                        }
                        Collections.reverse(tmpX);
                        XNfsr.add(tmpX);
                        String function = generators.item(i).getChildNodes().item(5).getTextContent();
                        tmp = function.split(" ");
                        ArrayList<Integer> tmp2 = new ArrayList<>();
                        for (String el : tmp) {
                            tmp2.add(Integer.parseInt(el));
                        }
                        jNfsr.add(tmp2);
                        p++;
                    }

                    String binaryPolinomial = nfsrParams.item(3).getChildNodes().item(0)
                            .getTextContent();
                    tmp = binaryPolinomial.split(" ");
                    for (String el : tmp) {
                        anotherJ.add(new BigInteger(el, 2));
                    }
                    int wNfsr = Integer.parseInt(nfsrParams.item(5).getChildNodes().item(0)
                            .getTextContent());
                    NonLinearRSLOS.generate(number, wNfsr, pNfsr, XNfsr, jNfsr, anotherJ, module);
                    break;
                case ("mt"):
                    NodeList mtParams = params.item(1).getChildNodes();
                    ArrayList<BigInteger> Xmt = new ArrayList<>();
                    values = mtParams.item(1).getChildNodes().item(0).getTextContent();
                    tmp = values.split("\\W+");
                    for (String el : tmp) {
                        if (!el.equals(" ") && !el.equals(""))
                            Xmt.add(new BigInteger(el));
                    }
                    MersenneTwister.generate(number, Xmt, module);
                    break;
                case ("rc4"):
                    NodeList rc4Params = params.item(1).getChildNodes();
                    int wRc4 = Integer.parseInt(rc4Params.item(1).getChildNodes().item(0)
                            .getTextContent());
                    int[] K = new int[256];
                    values = rc4Params.item(3).getChildNodes().item(0).getTextContent();
                    tmp = values.split("\\W+");
                    int i = 0;
                    for (String el : tmp) {
                        if (!el.equals(" ") && !el.equals("")) {
                            K[i] = Integer.parseInt(el);
                            i++;
                        }
                    }
                    RC4.generate(number, wRc4, K, module);
                    break;
                case ("rsa"):
                    NodeList RSAParams = params.item(1).getChildNodes();
                    BigInteger xoRSA = new BigInteger(RSAParams.item(1).getChildNodes().item(0)
                            .getTextContent());
                    int lRSA = Integer.parseInt(RSAParams.item(3).getChildNodes().item(0)
                            .getTextContent());
                    int wRSA = Integer.parseInt(RSAParams.item(5).getChildNodes().item(0)
                            .getTextContent());
                    BigInteger eRSA = new BigInteger(RSAParams.item(7).getChildNodes().item(0)
                            .getTextContent());
                    BigInteger nRSA = new BigInteger(RSAParams.item(9).getChildNodes().item(0)
                            .getTextContent());
                    RSAGen.generate(number, xoRSA, lRSA, wRSA, eRSA, nRSA, module);
                    break;
                case ("bbs"):
                    NodeList bbsParams = params.item(1).getChildNodes();
                    BigInteger xoBbs = new BigInteger(bbsParams.item(1).getChildNodes().item(0)
                            .getTextContent());
                    int lBbs = Integer.parseInt(bbsParams.item(3).getChildNodes().item(0)
                            .getTextContent());
                    BigInteger nBbs = new BigInteger(bbsParams.item(5).getChildNodes().item(0)
                            .getTextContent());
                    BBS.generate(number, xoBbs, lBbs, nBbs, module);
                    break;
                default:
                    System.err.println("Неизвестный тип генератора");
                    break;
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
