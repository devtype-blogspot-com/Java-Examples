import java.util.Scanner;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Main {

    static final private Map<String, BigInteger> cache = new HashMap<>();

    public static BigInteger p(final long n, final long m) {
        if (n <= 1) return BigInteger.ONE;
        if (m > n) return p(n, n);

        String cacheKey = n + "," + m;
        BigInteger sum = cache.get(cacheKey);
        if (sum != null) return sum;

        sum = BigInteger.ZERO;
        for (long k = 1; k <= m; k++) sum = sum.add(p(n-k, k));

        cache.put(cacheKey, sum);
        return sum;
    }

    public static BigInteger p(final long n) {
        return p(n, n);
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        long n = scanner.nextLong();
        BigInteger result = p(n, n);
        System.out.println(result.mod(new BigInteger("1000000007")));
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