import java.util.function.IntSupplier;
import java.util.stream.IntStream;

/**
 * https://stepic.org/lesson/Stream-API-12781/step/11?unit=3128
 * http://www.java2s.com/Tutorials/Java/java.util.stream/IntStream/IntStream.generate_IntSupplier_s_.htm
 * http://stackoverflow.com/a/26277712/2289640
 */
public class Main {

    public static IntStream pseudoRandomStream(int seed) {
        IntSupplier generator = new IntSupplier() {
            int current = 0;

            int mid(int idx) {
                if (idx == 0) return seed;
                int m = mid(idx - 1); m *= m;

                int result = 0, cnt = 0, r;
                while (m > 0) {
                    r = m % 10; cnt++;
                    if (cnt >= 2 && cnt <= 4) result += r * Math.pow(10, cnt - 2);
                    m /= 10;
                }

                return result;
            }

            public int getAsInt() {
                return mid(current++);
            }
        };

        IntStream natural = IntStream.generate(generator);

        return natural;
    }

    public static void main(String[] args) {

        IntStream i = pseudoRandomStream(13);
        i.limit(10).forEach(System.out::println);
    }
}

/*
Напишите метод, возвращающий стрим псевдослучайных целых чисел. Алгоритм генерации чисел следующий:

Берется какое-то начальное неотрицательное число (оно будет передаваться в ваш метод проверяющей системой).
Первый элемент последовательности устанавливается равным этому числу.
Следующие элементы вычисляются по рекуррентной формуле R[n+1]=mid(R[n]^2), где mid — это функция,
выделяющая второй, третий и четвертый разряд переданного числа. Например, mid(123456)=345.
Алгоритм, конечно, дурацкий и не выдерживающий никакой критики, но для практики работы со стримами сойдет :)

Пример

pseudoRandomStream(13) должен вернуть стрим, состоящий из следующих чисел:

13, 16, 25, 62, 384, 745, 502, 200, 0, ... (дальше бесконечное количество нулей)
 */