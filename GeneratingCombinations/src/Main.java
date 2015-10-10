/*
http://e-maxx.ru/algo/generating_combinations
http://rsdn.ru/article/alg/Combine.xml
http://study-and-dev.com/blog/sda_theory_combinatoric/
 */

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int M = s.nextInt();
        int N = s.nextInt();

        int[] arr = new int [M];
        int i , j;
        for (i = 0; i < M; i++)
            arr [i] = i;
        do {

            for (i = 0; i < M; i++)
                System.out.print(arr[i] + " ");
            System.out.print("\n");

            for ( j = M -1 ; j >=0 && arr [j] >= N - M + j; j--) ;
            if ( j >= 0){
                arr [j] ++;
                for (i = j + 1; i < M; i++)
                    arr [i] = arr [i-1]  + 1;
            }
            else break;

        }while (true);

    }
}

/*
Нужно сгенерировать все возможные k-сочетания из n элементов.

Формат входных данных:
Два числа k и n через пробел. Для них гарантированно выполняется условие: 0≤k≤n.

Формат выходных данных:
Необходимое число строк, в каждой из которых содержится k чисел из диапазона от 0 до n-1 включительно, разделенных пробелом.



Sample Input 1:
2 3
Sample Output 1:
0 1
0 2
1 2

Sample Input 2:
1 1
Sample Output 2:
0
*/