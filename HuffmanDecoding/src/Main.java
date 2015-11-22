import java.util.HashMap;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        int k, l;
        Scanner scanner = new Scanner(System.in);
        k = scanner.nextInt();
        l = scanner.nextInt();
        scanner.nextLine();

        HashMap<String, String> table = new HashMap<>();
        for (int i = 0; i < k; i++) {
            String letterCode = scanner.nextLine();
            String[] arr = letterCode.split(": ");
            table.put(arr[1], arr[0]);
        }
        String encoded = scanner.nextLine();

        while (encoded.length() != 0) {
            for (String key : table.keySet()) {
                if (encoded.startsWith(key)) {
                    encoded = encoded.substring(key.length());
                    System.out.print(table.get(key));

                }
            }
        }

    }
}

/*
https://stepic.org/lesson/%D0%9A%D0%BE%D0%B4%D1%8B-%D0%A5%D0%B0%D1%84%D1%84%D0%BC%D0%B0%D0%BD%D0%B0-13239/step/6?unit=3425

Восстановите строку по её коду и беспрефиксному коду символов.

В первой строке входного файла заданы два целых числа k и l через пробел — количество различных букв,
встречающихся в строке, и размер получившейся закодированной строки, соответственно.
В следующих k строках записаны коды букв в формате "letter: code". Ни один код не является префиксом другого.
Буквы могут быть перечислены в любом порядке. В качестве букв могут встречаться лишь строчные буквы латинского алфавита;
каждая из этих букв встречается в строке хотя бы один раз. Наконец, в последней строке записана закодированная строка.
Исходная строка и коды всех букв непусты. Заданный код таков, что закодированная строка имеет минимальный возможный размер.


В первой строке выходного файла выведите строку s. Она должна состоять из строчных букв латинского алфавита.
Гарантируется, что длина правильного ответа не превосходит 10^4 символов.


Sample Input 1:
1 1
a: 0
0

Sample Output 1:
a



Sample Input 2:
4 14
a: 0
b: 10
c: 110
d: 111
01001100100111

Sample Output 2:
abacabad

 */
