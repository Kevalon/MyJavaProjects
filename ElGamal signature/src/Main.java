import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static BigInteger[] gcdEX(BigInteger a, BigInteger b) {
        ArrayList<BigInteger> as = new ArrayList<>();
        ArrayList<BigInteger> xs = new ArrayList<>();
        ArrayList<BigInteger> ys = new ArrayList<>();
        ArrayList<BigInteger> qs = new ArrayList<>();

        if (a.compareTo(b) > 0) {
            as.add(a);
            as.add(b);
        }
        else {
            as.add(b);
            as.add(a);
        }

        if (b.compareTo(a) < 0) {
            xs.add(BigInteger.ONE);
            xs.add(BigInteger.ZERO);
            ys.add(BigInteger.ZERO);
            ys.add(BigInteger.ONE);
        }
        else {
            xs.add(BigInteger.ZERO);
            xs.add(BigInteger.ONE);
            ys.add(BigInteger.ONE);
            ys.add(BigInteger.ZERO);
        }
        qs.add(BigInteger.ONE.negate());
        int i = 1;

        while (!as.get(i).equals(BigInteger.ZERO)) {
            BigInteger qi = as.get(i - 1).divide(as.get(i));
            BigInteger aNext = as.get(i - 1).mod(as.get(i));
            qs.add(qi);
            as.add(aNext);
            i++;

            BigInteger xi = xs.get(i - 2).subtract(xs.get(i - 1).multiply(qs.get(i - 1)));
            BigInteger yi = ys.get(i - 2).subtract(ys.get(i - 1).multiply(qs.get(i - 1)));
            xs.add(xi);
            ys.add(yi);
        }
        return new BigInteger[] { as.get(i - 1), xs.get(i - 1), ys.get(i - 1) };
    }

    public static BigInteger findInverse(BigInteger a, BigInteger m) {
        BigInteger x, g;
        BigInteger[] tmp = gcdEX(a, m);
        g = tmp[0];
        x = tmp[1];
        if (!g.equals(BigInteger.ONE))
            return BigInteger.ONE.negate();
        else {
            while (x.compareTo(BigInteger.ZERO) < 0)
                x = x.add(m);
            return x.mod(m);
        }
    }

    public static BigInteger findPrimitive (BigInteger p, BigInteger start) {
        ArrayList<BigInteger> fact = new ArrayList<>();
        BigInteger phi = p.subtract(BigInteger.ONE);
        BigInteger n = phi;
        for (BigInteger i = BigInteger.TWO; n.compareTo(i.multiply(i)) > 0; i = i.add(BigInteger.ONE))
            if (n.mod(i).equals(BigInteger.ZERO)) {
                fact.add(i);
                while (n.mod(i).equals(BigInteger.ZERO))
                    n = n.divide(i);
            }
        if (n.compareTo(BigInteger.ONE) > 0)
            fact.add(n);

        for (BigInteger res = start; p.compareTo(res) > 0; res = res.add(BigInteger.ONE)) {
            boolean ok = true;
            for (int i = 0;  i < fact.size() && ok; ++i)
                ok = !res.modPow(phi.divide(fact.get(i)), p).equals(BigInteger.ONE);
            if (ok)  return res;
        }

        for (BigInteger res = BigInteger.TWO; p.compareTo(res) > 0; res = res.add(BigInteger.ONE)) {
            boolean ok = true;
            for (int i = 0;  i < fact.size() && ok; ++i)
                ok = !res.modPow(phi.divide(fact.get(i)), p).equals(BigInteger.ONE);
            if (ok)  return res;
        }
        return BigInteger.ONE.negate();
    }

    public static void main(String[] args) throws IOException {
        Scanner in = new Scanner(System.in);

        int response;
        boolean working = true;
        while (working) {
            System.out.println("?????? ?????");
            System.out.println("1. ??????????.");
            System.out.println("2. ??????.");
            response = in.nextInt();
            if (response == 1) {
                while (working) {
                    System.out.println("???????????????? ???????????????? ????????????????:");
                    System.out.println("1. ?????????????????????????? ??????????.");
                    System.out.println("2. ?????????????????? ??????????????????.");
                    response = in.nextInt();

                    if (response == 1) {
                        System.out.println("?????????????? ?????????????? ?????????? p (p > 5):");
                        BigInteger p = in.nextBigInteger();
                        if (p.compareTo(BigInteger.valueOf(5)) < 0) {
                            System.out.println("?????????????????? p ?????????????? ??????????????????.");
                            continue;
                        }
                        if (!MillerRabinTest.test(p, 14)) {
                            System.out.println("?????????????????? p - ???? ??????????????.");
                            continue;
                        }
                        BigInteger g = findPrimitive(p, MillerRabinTest.getRandomBigInteger(BigInteger.TWO,
                                p.subtract(BigInteger.TWO)));
                        BigInteger x = MillerRabinTest.getRandomBigInteger(BigInteger.TWO, p.subtract(BigInteger.ONE));
                        BigInteger y = g.modPow(x, p);

                        try (FileWriter out = new FileWriter("AlicePublicKey.txt")) {
                            out.write(y.toString());
                            out.append("\n");
                            out.append(g.toString());
                            out.append("\n");
                            out.append(p.toString());
                        }
                        System.out.println("???????????????? ???????? ?????????????? ?? ???????? AlicePublicKey.txt");

                        try (FileWriter out = new FileWriter("AlicePrivateKey.txt")) {
                            out.write(x.toString());
                        }
                        System.out.println("???????????????? ???????? ?????????????? ?? ???????? AlicePrivateKey.txt");
                    }

                    else if (response == 2) {
                        BigInteger y, g, p, x;

                        File publicInput = new File("AlicePublicKey.txt");
                        if (!publicInput.exists() || publicInput.isDirectory()) {
                            System.out.println("???????? ?? ???????????????? ???????????? ???? ????????????.");
                            return;
                        }
                        if (publicInput.length() == 0) {
                            System.out.println("???????? ?????????????????? ?????????? ????????.");
                            return;
                        }
                        
                        File privateInput = new File("AlicePrivateKey.txt");
                        if (!privateInput.exists() || privateInput.isDirectory()) {
                            System.out.println("???????? ?? ???????????????? ???????????? ???? ????????????.");
                            return;
                        }
                        if (privateInput.length() == 0) {
                            System.out.println("???????? ?????????????????? ?????????? ????????.");
                            return;
                        }

                        Scanner publicScan = new Scanner(publicInput);
                        Scanner privateScan = new Scanner(privateInput);

                        x = privateScan.nextBigInteger();
                        y = publicScan.nextBigInteger();
                        g = publicScan.nextBigInteger();
                        p = publicScan.nextBigInteger();
                        
                        privateScan.close();
                        publicScan.close();
                        
                        File Mfile = new File("AliceM.txt");
                        if (!Mfile.exists() || Mfile.isDirectory()) {
                            System.out.println("???????? ?? ???????????????????? ?? ???? ????????????.");
                            return;
                        }
                        if (Mfile.length() == 0) {
                            System.out.println("???????? ?????????????????? ?? ????????.");
                            return;
                        }
                        Scanner Mscan = new Scanner(Mfile);
                        BigInteger M = Mscan.nextBigInteger();
                        Mscan.close();

                        BigInteger tmp = p.subtract(BigInteger.ONE);
                        BigInteger k = MillerRabinTest.getRandomBigInteger(BigInteger.TWO, tmp);
                        while (!k.gcd(tmp).equals(BigInteger.ONE)) {
                            k = MillerRabinTest.getRandomBigInteger(BigInteger.TWO, tmp);
                        }
                        BigInteger r = g.modPow(k, p);

                        BigInteger mayBeNegative = M.subtract(x.multiply(r));
                        if (mayBeNegative.compareTo(BigInteger.ZERO) < 0)
                            mayBeNegative = (mayBeNegative.mod(tmp).add(tmp)).mod(tmp);
                        BigInteger s = (mayBeNegative.multiply(findInverse(k, tmp))).mod(tmp);

                        try (FileWriter out = new FileWriter("AliceSignature.txt")) {
                            out.write(r.toString());
                            out.write("\n");
                            out.write(s.toString());
                        }
                        System.out.println("?????????????? ???????????????? ?? ???????? AliceSignature.txt.");
                        working = false;
                    }
                    else {
                        System.out.println("?????????? ?????????????? ???? ????????????????????.");
                    }
                }
            }

            else if (response == 2) {
                BigInteger y, g, p;

                File publicInput = new File("BobAlicePublicKey.txt");
                if (!publicInput.exists() || publicInput.isDirectory()) {
                    System.out.println("???????? ?? ???????????????? ???????????? ???? ????????????.");
                    return;
                }
                if (publicInput.length() == 0) {
                    System.out.println("???????? ?????????????????? ?????????? ????????.");
                    return;
                }

                Scanner publicScan = new Scanner(publicInput);
                y = publicScan.nextBigInteger();
                g = publicScan.nextBigInteger();
                p = publicScan.nextBigInteger();
                publicScan.close();

                File Mfile = new File("BobM.txt");
                if (!Mfile.exists() || Mfile.isDirectory()) {
                    System.out.println("???????? ?? ???????????????????? ?? ???? ????????????.");
                    return;
                }
                if (Mfile.length() == 0) {
                    System.out.println("???????? ?????????????????? ?? ????????.");
                    return;
                }
                Scanner Mscan = new Scanner(Mfile);
                BigInteger M = Mscan.nextBigInteger();
                Mscan.close();

                File signFile = new File("BobSignature.txt");
                if (!signFile.exists() || signFile.isDirectory()) {
                    System.out.println("???????? ?? ???????????????? ???? ????????????.");
                    return;
                }
                if (signFile.length() == 0) {
                    System.out.println("???????? ?? ???????????????? ????????.");
                    return;
                }
                Scanner signScan = new Scanner(signFile);
                BigInteger r = signScan.nextBigInteger();
                BigInteger s = signScan.nextBigInteger();
                signScan.close();

                if (r.compareTo(BigInteger.ZERO) < 1 || r.compareTo(p) > -1 || s.compareTo(BigInteger.ZERO) < 1
                        || s.compareTo(p.subtract(BigInteger.ONE)) > -1) {
                    System.out.println("?????????????? ???? ????????????????????????.");
                    return;
                }

                if (y.modPow(r, p).multiply(r.modPow(s, p)).mod(p).equals(g.modPow(M, p))) {
                    System.out.println("?????????????? ????????????????????????.");
                }
                else {
                    System.out.println("?????????????? ???? ????????????????????????.");
                }
                working = false;
            }

            else {
                System.out.println("?????????? ?????????????? ???? ????????????????????.");
            }
        }
        in.close();
    }
}
