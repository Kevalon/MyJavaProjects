import java.util.ArrayList;

public class JacobiLegendreSymbol {

    public static int getSymbol(int a, int n) {
        ArrayList<Integer> as = new ArrayList<>();
        int n1 = n;
        int a1 = a;

        if (a1 < 0) {
            a1 *= -1;
            int tmp = (n - 1) / 2;
            as.add((int) Math.pow(-1, tmp));
        }

        if (n % a1 == 0 && a1 != 1) {
            return 0;
        }

        while (true) {
            a1 %= n1;
            if (a1 == 0) {
                return 0;
            }


            if (a1 % 2 == 0) {
                int t = 0;

                do {
                    a1 /= 2;
                    t++;
                } while (a1 % 2 == 0);

                if (t % 2 == 1) {
                    int tmp = (n1 * n1 - 1) / 8;
                    as.add((int) Math.pow(-1, tmp));
                }
            }

            if (a1 == 1)
                break;

            if (n1 % a1 == 0) {
                return 0;
            }

            int tmp = ((a1 - 1) / 2) * ((n1 - 1) / 2);
            as.add((int) Math.pow(-1, tmp));
            tmp = a1;
            a1 = n1;
            n1 = tmp;
        }

        int res = 1;
        for (int el : as) {
            res *= el;
        }
        return res;
    }
}

