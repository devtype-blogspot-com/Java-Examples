import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * https://stepic.org/lesson/Stream-API-12781/step/12?unit=3128
 * http://www.concretepage.com/java/jdk-8/java-8-stream-findAny-findFirst-limit-max-min-example
 * https://stevewall123.wordpress.com/2014/08/31/java-8-streams-max-and-streams-min-example/
 */
public class Main {

    public static <T> void findMinMax(
            Stream<? extends T> stream,
            Comparator<? super T> order,
            BiConsumer<? super T, ? super T> minMaxConsumer) {
        List<? extends T> minMaxList = stream.sorted(order).collect(Collectors.toList());
        if (minMaxList.size() > 1)
            minMaxConsumer.accept(minMaxList.get(0), minMaxList.get(minMaxList.size() - 1));
        else if (minMaxList.size() == 1)
            minMaxConsumer.accept(minMaxList.get(0), minMaxList.get(0));
        else
            minMaxConsumer.accept(null, null);
    }

    public static void main(String[] args) {
        List<Integer> numbers = Arrays.asList(3, 2, 2, 3, 7, 3, 5);

        findMinMax(numbers.stream(), new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return o1.compareTo(o2);
            }
        }, new BiConsumer<Integer, Integer>() {
            @Override
            public void accept(Integer integer, Integer integer2) {
                System.out.println("min = " + integer + ", max = " + integer2);
            }
        });

    }
}
