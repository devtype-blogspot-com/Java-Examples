import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Scanner;

/**
 * https://stepic.org/lesson/Потоки-символов-12784/step/12?course=Java-Базовый-курс&unit=3131
 */
public class Main {

    public static String readAsString(InputStream inputStream, Charset charset) throws IOException {
        byte[] bytes = inputStream2ByteArr(inputStream);
        return new String(bytes, charset);
    }

    private static byte[] inputStream2ByteArr(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

}
