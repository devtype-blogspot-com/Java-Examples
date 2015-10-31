import java.util.HashSet;
import java.util.Set;

/**
 * @see https://stepic.org/lesson/Коллекции-12776/step/14?unit=3124
 * @see https://ru.wikipedia.org/wiki/Симметрическая_разность
 */
public class Main {

    /**
     * @see http://stackoverflow.com/a/8064726/2289640
     *
     * @param set1
     * @param set2
     * @param <T>
     * @return
     */
    public static <T> Set<T> symmetricDifference(Set<? extends T> set1, Set<? extends T> set2) {
        Set<T> symmetricDiff = new HashSet<T>(set1);
        symmetricDiff.addAll(set2);
        Set<T> tmp = new HashSet<T>(set1);
        tmp.retainAll(set2);
        symmetricDiff.removeAll(tmp);
        return symmetricDiff;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}

/*
Реализуйте метод, вычисляющий симметрическую разность двух множеств.

Метод должен возвращать результат в виде нового множества. Изменять переданные в него множества не допускается.

Пример

Симметрическая разность множеств {1, 2, 3} и {0, 1, 2} равна {0, 3}.

 */