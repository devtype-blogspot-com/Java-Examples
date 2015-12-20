import java.util.Scanner;

public class Main {

    public static long foo(long n, long m) {
        long result = 0;
        if (n <= 1) return 1;
        if (m > n) return foo(n, n);
        for (long k = 1; k <= m; k++) result = result + foo(n - k, k);
        return result;
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        long n = scanner.nextLong();
        System.out.println(foo(n, n));
    }
}


/*

Число разбиений на слагаемые

По данному целому числу 1≤n≤1000 найдите число способов представить n в виде суммы положительных целых чисел.
Выведите данное число по модулю 10^9+7.
Два представления, отличающиеся друг от друга только порядком слагаемых, считаем одинаковыми.

Sample Input:
5

Sample Output:
7

*/