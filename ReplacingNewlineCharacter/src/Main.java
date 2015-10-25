import java.io.IOException;

/**
 * https://stepic.org/lesson/Потоки-байт-12783/step/9?course=Java-Базовый-курс&unit=3130
 */
public class Main {

    public static void main(String[] args) throws IOException {
        boolean a = false;

        int c = System.in.read();
        while (c != -1) {

            if (c == 13) {
                if (a == true) {
                    System.out.write((char)c);
                }
                a = true;
            } else if (c == 10 && a) {
                System.out.write(10);
                a = false;
            } else {
                if (a == true) {
                    System.out.write((char)13);
                    a = false;
                }
                System.out.write((char)c);
            }

            c = System.in.read();
        }

        System.out.flush();
    }
}
