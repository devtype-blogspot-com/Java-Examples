import java.io.IOException;
import java.io.InputStream;

/**
 * https://stepic.org/lesson/Потоки-байт-12783/step/8?course=Java-Базовый-курс&unit=3130
 */
public class Main {

    public static int checkSumOfStream(InputStream inputStream) throws IOException {
        int checkSum = 0;

        int x = inputStream.read();
        while(x != -1) {
            checkSum = Integer.rotateLeft(checkSum, 1) ^ x;
            x = inputStream.read();
        }

        return checkSum;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}
