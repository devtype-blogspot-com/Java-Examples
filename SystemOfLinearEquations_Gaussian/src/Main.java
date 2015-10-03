import java.util.Scanner;

class Main {
    public static void main(String[] args) {

        /* Ввод данных */

        Scanner s = new Scanner(System.in);
        int n = s.nextInt();
        int m = s.nextInt();
        double[][] A = new double[100][100];
        double[] b = new double[100];
        for (int i = 0; i < n; i++) {
            A[i] = new double[100];
            for (int j = 0; j < m; j++) {
                A[i][j] = s.nextDouble();
            }
            b[i] = s.nextDouble();
        }

        /* Метод Гаусса */

        int N  = n;
        for (int p = 0; p < N; p++) {

            int max = p;
            for (int i = p + 1; i < N; i++) {
                if (Math.abs(A[i][p]) > Math.abs(A[max][p])) {
                    max = i;
                }
            }
            double[] temp = A[p]; A[p] = A[max]; A[max] = temp;
            double   t    = b[p]; b[p] = b[max]; b[max] = t;

            if (Math.abs(A[p][p]) <= 1e-10) {
                System.out.println("NO");
                return;
            }

            for (int i = p + 1; i < N; i++) {
                double alpha = A[i][p] / A[p][p];
                b[i] -= alpha * b[p];
                for (int j = p; j < N; j++) {
                    A[i][j] -= alpha * A[p][j];
                }
            }
        }

        // Обратный проход

        double[] x = new double[N];
        for (int i = N - 1; i >= 0; i--) {
            double sum = 0.0;
            for (int j = i + 1; j < N; j++) {
                sum += A[i][j] * x[j];
            }
            x[i] = (b[i] - sum) / A[i][i];
        }

        /* Вывод результатов */

        if (n < m) {
            System.out.print("INF");
        } else {
            System.out.println("YES");
            for (int i = 0; i < N; i++) {
                System.out.print(x[i] + " ");
            }
        }

    }
}

/*
Напишите программу, которая решает систему линейных алгебраических уравнений методом Гаусса.

Формат входных данных:

В первой строке задаются два числа: количество уравнений n (n≥1) и количество неизвестных m (m≥1).
Далее идут n строк, каждая из которых содержит m+1 число. Первые m чисел — это коэффициенты i-го уравнения системы,
а последнее, (m+1)-е число — коэффициент bi, стоящий в правой части i-го уравнения.

Формат выходных данных:
В первой строке следует вывести слово YES, если решение существует и единственно, слово NO в случае, если решение не существует,
и слово INF в случае, когда решений существует бесконечно много. Если решение существует и единственно,
то во второй строке следует вывести решение системы в виде m чисел, разделенных пробелом.

Sample Input 1:
3 3
4 2 1 1
7 8 9 1
9 1 3 2
Sample Output 1:
YES
0.2608695652173913 0.04347826086956526 -0.1304347826086957

Sample Input 2:
2 3
1 3 4 4
2 1 4 5
Sample Output 2:
INF

Sample Input 3:
3 3
1 3 2 7
2 6 4 8
1 4 3 1
Sample Output 3:
NO

*/