import java.util.Scanner;

public class Main {
    private static int cnt = 0;
    private static char[] s;

    static char at(int i) {
        return cnt % 2 == 0 ? s[i] : (s[i] == '0' ? '1' : '0');
    }

    public static void main(String[] args) {
        String str;
        Scanner scanner = new Scanner(System.in);
        str = scanner.nextLine();
        s = str.toCharArray();

        int i = s.length - 1;
        while (i >= 0) {
            if (at(i) == '0') {
                while (i >= 0 && at(i) == '0') i--;
            } else {
                while (i >= 0 && at(i) != '0') i--;
                cnt++;
            }
        }

        System.out.println(cnt);
    }
}

/*

 Дана строка s длины не более 10^5, состоящая только из символов 0 и 1.
 Со строкой разрешается делать следующую операцию: взять префикс строки и поменять в нём всё нули на единицы,
 а все единицы — на нули (другими словами, инвертировать все символы префикса).
 Выведите минимальное количество операций, требующееся, чтобы из строки s получить строку, состоящую из всех нулей.

 Sample Input:
 010

 Sample Output:
 2

*/