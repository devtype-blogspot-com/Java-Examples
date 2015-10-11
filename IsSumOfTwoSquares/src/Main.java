public class Main {
    public static boolean isSumOfTwoSquares(int value) {
        for (int i = 0; i <= value; i++) {
            if (i*i > value) continue;
            if (i*i + i*i == value) { return true; }
            for (int j = i + 1; j <= value; j++) {
                if (j*j > value) break;
                if (i*i + j*j == value) { return true; }
            }
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(isSumOfTwoSquares(25));
    }
}

/*
Реализуйте метод, проверяющий, может ли заданное целое число быть представлено в виде суммы квадратов двух целых чисел.

Sample Input 1:
-1
Sample Output 1:
false

Sample Input 2:
5
Sample Output 2:
true

 */