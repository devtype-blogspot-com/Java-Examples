import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * https://stepic.org/lesson/Потоки-символов-12784/step/13?course=Java-Базовый-курс&unit=3131
 */
public class Main {

    public static void main(String[] args) throws IOException {
        double sum = 0;
        String str = readString(System.in);

        StringTokenizer defaultTokenizer = new StringTokenizer(str);
        while (defaultTokenizer.hasMoreTokens())
        {
            try {
                double d = Double.parseDouble(defaultTokenizer.nextToken());
                sum += d;
            } catch (Exception e) {
            }
        }

        DecimalFormat df = new DecimalFormat("0.000000");
        System.out.print(df.format(sum));
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
