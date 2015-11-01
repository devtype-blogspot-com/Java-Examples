import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @see http://stackoverflow.com/a/23846961/2289640
 * @see http://stackoverflow.com/a/29127257/2289640
 * @see https://stepic.org/lesson/Stream-API-12781/step/13?unit=3128
 * @see http://www.leveluplunch.com/java/examples/java-util-stream-filter-slice-example/
 */
public class Main {
    static String readString(InputStream is) throws IOException {
        char[] buf = new char[2048];
        Reader r = new InputStreamReader(is, "UTF-8");
        StringBuilder s = new StringBuilder();
        while (true) {
            int n = r.read(buf);
            if (n < 0)
                break;
            s.append(buf, 0, n);
        }
        return s.toString();
    }

    public static void main(String[] args) {
        String inputStr = null;
//        try { inputStr = readString(System.in); } catch (IOException e) { e.printStackTrace(); }
//        inputStr = "Мама мыла-мыла-мыла раму!";
        inputStr = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sodales consectetur purus at faucibus. Donec mi quam, tempor vel ipsum non, faucibus suscipit massa. Morbi lacinia velit blandit tincidunt efficitur. Vestibulum eget metus imperdiet sapien laoreet faucibus. Nunc eget vehicula mauris, ac auctor lorem. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer vel odio nec mi tempor dignissim.";

        List<String> wordsList = Arrays.asList(inputStr.split("[^\\p{L}\\p{Digit}_]+"));
        Map<String, Integer> counts = wordsList.parallelStream()
                .map(String::toLowerCase)
                .collect(Collectors.toConcurrentMap(w -> w, w -> 1, Integer::sum));

        counts.entrySet().stream()
                //.sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .sorted(new Comparator<Map.Entry<String, Integer>>() {
                    @Override
                    public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                        if (o1.getValue() == o2.getValue()) {
                            return o1.getKey().compareTo(o2.getKey());
                        } else if (o1.getValue() > o2.getValue()) {
                            return -1;
                        } else if (o1.getValue() < o2.getValue()) {
                            return 1;
                        }
                        return 0;
                    }
                })
                .limit(10)
                .forEach(new Consumer<Map.Entry<String, Integer>>() {
                    @Override
                    public void accept(Map.Entry<String, Integer> stringIntegerEntry) {
//                        System.out.println(stringIntegerEntry.getKey() + " : " + stringIntegerEntry.getValue());
                        System.out.println(stringIntegerEntry.getKey());

                    }
                });
    }
}

/*
Напишите программу, читающую из System.in текст в кодировке UTF-8, подсчитывающую в нем частоту появления слов,
и в конце выводящую 10 наиболее часто встречающихся слов.

Словом будем считать любую непрерывную последовательность символов, состоящую только из букв и цифр. Например,
в строке "Мама мыла раму 33 раза!" ровно пять слов: "Мама", "мыла", "раму", "33" и "раза".

Подсчет слов должен выполняться без учета регистра, т.е. "МАМА", "мама" и "Мама" — это одно и то же слово.
Выводите слова в нижнем регистре.

Если в тексте меньше 10 уникальных слов, то выводите сколько есть.

Если в тексте некоторые слова имеют одинаковую частоту, т.е. их нельзя однозначно упорядочить только по частоте,
то дополнительно упорядочите слова с одинаковой частотой в лексикографическом порядке.

Задача имеет красивое решение через стримы без циклов и условных операторов. Попробуйте придумать его.


Sample Input 1:
Мама мыла-мыла-мыла раму!

Sample Output 1:
мыла
мама
раму



Sample Input 2:
Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed sodales consectetur purus at faucibus. Donec mi quam, tempor vel ipsum non, faucibus suscipit massa. Morbi lacinia velit blandit tincidunt efficitur. Vestibulum eget metus imperdiet sapien laoreet faucibus. Nunc eget vehicula mauris, ac auctor lorem. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer vel odio nec mi tempor dignissim.

Sample Output 2:
consectetur
faucibus
ipsum
lorem
adipiscing
amet
dolor
eget
elit
mi

 */