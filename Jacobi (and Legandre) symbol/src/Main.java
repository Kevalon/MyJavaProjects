import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int a, n;
        ArrayList<Integer> as = new ArrayList<>();

        System.out.print("a = ");
        a = in.nextInt();
        System.out.print("n = ");
        n = in.nextInt();
        int n1 = n;
        int a1 = a;

        if (a1 < 0) {
            a1 *= -1;
            int tmp = (n - 1) / 2;
            as.add((int) Math.pow(-1, tmp));
        }

        if (n % a1 == 0 && a1 != 1) {
            System.out.println("(" + a + "/" + n + ") = 0");
            return;
        }

        while (true) {
            a1 %= n1;
            if (a1 == 0) {
                System.out.println("(" + a + "/" + n + ") = 0");
                return;
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
                System.out.println("(" + a + "/" + n + ") = 0");
                return;
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
        System.out.println("(" + a + "/" + n + ") = " + res);
    }
}

