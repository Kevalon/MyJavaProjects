import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;

public class NonLinearRSLOS {

    public static ArrayList<Integer> l = new ArrayList<>();

    public static void one_generator(int i, int w, ArrayList<Integer> p, ArrayList<ArrayList<Integer>> j,
                                     ArrayList<ArrayList<Integer>> X) {
        int lCur = l.get(i);
        int pCur = p.get(i);
        int k = 0;
        int[] a = new int[p.get(i)];
        for (var el : j.get(i)) {
            a[el - 1] = 1;
        }
        while(k < w) {
            int xCur = 0;
            int start = X.get(i).size() - pCur;

            for (int it = start; it < X.get(i).size(); it++)
                xCur = (xCur ^ (a[it - start] & X.get(i).get(it))) % 2;
            X.get(i).add(xCur);
            if (X.get(i).size() > lCur)
                X.get(i).remove(0);
            k++;
        }
    }

    public static void generate(int c, int w, ArrayList<Integer> p, ArrayList<ArrayList<Integer>> X,
                                ArrayList<ArrayList<Integer>> j, ArrayList<BigInteger> anotherJ, BigInteger mod) {

        for (int i = 0; i < X.size(); i++) {
            l.add(Math.max(p.get(i), w));
        }

        for (int i = 0; i < c; i++) {
            for (int it = 0; it < X.size(); it++)
                one_generator(it, w, p, j, X);

            BigInteger Y = BigInteger.ZERO;
            for (int it = w - 1; it >= 0; it--) {
                int xi = 0;
                for (int t = 0; t < anotherJ.size(); t++) {
                    int tmp = 1;
                    for (int ii = 0; ii < X.size(); ii++) {
                        if (anotherJ.get(t).testBit(ii))
                            tmp = (tmp & X.get(X.size() - 1 - ii).get(w - 1 - it)) % 2;
                    }
                    xi = (xi ^ tmp) % 2;
                }

                if (xi == 1)
                    Y = Y.setBit(it);
            }
            if (!mod.equals(BigInteger.ZERO))
                Y = Y.mod(mod);
            System.out.println(Y);
        }
    }
}
