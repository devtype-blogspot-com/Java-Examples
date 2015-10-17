/*
https://stepic.org/lesson/$k$-перестановки-из-$n$-элементов-9482/step/12?course=Ликбез-по-дискретной-математике&unit=1760
http://www.cyberforum.ru/delphi-beginners/thread682954.html
https://www.sparksoft.eu/education/formula/4/#f4
 */

import java.util.Scanner;

public class Main {
    private static int m;

    private static byte[][] result;
    private static int cnt = 0;


    private static void genStr(byte[] s0, byte[] s1) {
        if (s0.length == m) {
            result[cnt++] = s0;
//            for (int j = 0; j < s0.length; j++)
//                System.out.print(s0[j] + " ");
//            System.out.println();
        } else {
            for (int i = 0; i < s1.length; i++) {
                byte[] s0_ = new byte[s0.length + 1];
//                for (int q = 0; q < s0.length; q++) s0_[q] = s0[q];
                System.arraycopy(s0,0,s0_,0,s0.length);

                s0_[s0_.length - 1] = s1[i];

                byte[] s1_ = new byte[i + (s1.length - (i + 1))];
//                for (int q = 0; q < i; q++) s1_[q] = s1[q];
                System.arraycopy(s1,0,s1_,0,i);

//                for (int q = i; q < s1_.length; q++) s1_[q] = s1[q + 1];
                System.arraycopy(s1,i+1,s1_,i,s1_.length-i);

                genStr(s0_, s1_);
            }
        }
    }

    private static void genStr(String s0, String s1) {
        if (s0.length() == m) {
            for (int j = 0; j < s0.length(); j++)
                System.out.print(s0.charAt(j) + " ");
            System.out.println();
        } else {
            for (int i = 0; i < s1.length(); i++) {
                genStr(s0 + s1.charAt(i), s1.substring(0, i) + s1.substring(i + 1));
            }
        }
    }

    public static void main(String[] args) {
//        m = 2;
//        genStr("", "0123");
//
        Scanner s = new Scanner(System.in);
        int n = s.nextInt();
        int k = s.nextInt();

        m = k;

        int l = factorial(n)/factorial(n-k);
        result = new byte[l][];

        StringBuilder sb = new StringBuilder();
        byte[] a = new byte[n];

        for (byte i = 0; i < n; i++) {
//            sb.append(i);
            a[i] = i;
        }

//        genStr("", sb.toString());
        genStr(new byte[] {}, a);

        for (int i = 0; i < l; i++) {
            for (int j = 0; j < k; j++) {
//                System.out.print(result[i][j] + " ");
                sb.append(result[i][j] + " ");
            }
//            System.out.println();
            sb.append("\n");
        }

        System.out.println(sb.toString());

    }

    private static int factorial(int n) {
        int fact = 1; // this  will be the result
        for (int i = 1; i <= n; i++) {
            fact *= i;
        }
        return fact;
    }

}

/*
Нужно сгенерировать все возможные k-перестановки n-элементов без повторений.

Формат входные данные:
Два числа n и k через пробел. Для них гарантированно выполняется условие: 0 < k ≤ n.

Формат выходных данных:
Необходимое число лексикографически упорядоченных строк, в каждой из которых содержится k чисел от 0 до n-1, разделенных пробелом.


Sample Input:
4 2

Sample Output:
0 1
0 2
0 3
1 0
1 2
1 3
2 0
2 1
2 3
3 0
3 1
3 2

 */