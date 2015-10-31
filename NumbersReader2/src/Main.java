import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.*;

/**
 * @see https://stepic.org/lesson/Коллекции-12776/step/15?unit=3124
 */
public class Main {


    public static void main(String[] args) throws IOException {
        double sum = 0;
        String str = readString(System.in);

        ArrayList<String> collection = new ArrayList<>();

        List list = Arrays.asList(str.split("\\s"));
        Iterator<String> it = list.iterator();
        int idx = 0;
        while (it.hasNext()) {
            String element = it.next();
            if (idx++ % 2 == 0) continue;
            collection.add(element);
        }

        // http://stackoverflow.com/a/2102552/2289640
        ListIterator lit = collection.listIterator(collection.size());
        while(lit.hasPrevious()) {
            String element = (String)lit.previous();
            System.out.print(element + " ");
        }

    }

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
}

/*
Напишите программу, которая прочитает из System.in последовательность целых чисел, разделенных пробелами, затем удалит из них все числа,
стоящие на четных позициях, и затем выведет получившуюся последовательность в обратном порядке в System.out.

Все числа влезают в int. Позиции чисел в последовательности нумеруются с нуля.

В этом задании надо написать программу целиком, включая import'ы, декларацию класса Main и метода main.

Sample Input:
1 2 3 4 5 6 7
Sample Output:
6 4 2

 */