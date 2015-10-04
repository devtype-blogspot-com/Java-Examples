import java.nio.charset.StandardCharsets;

public class Main {

    public static void main(String[] args) {
        CharSequence cs1 = new AsciiCharSequence(new byte[] {32, 32, 32});
        CharSequence cs2 = new AsciiCharSequence(new byte[0]);
//        CharSequence cs3 = new AsciiCharSequence(s.getBytes(StandardCharsets.US_ASCII));
//        CharSequence cs4 = new AsciiCharSequence(s.getBytes(StandardCharsets.US_ASCII));

    }
}

/*
Напишите класс AsciiCharSequence, реализующий компактное хранение последовательности ASCII-символов (их коды влезают в один байт) в массиве байт.
По сравнению с классом String, хранящим каждый символ как char, AsciiCharSequence будет занимать в два раза меньше памяти.

Класс AsciiCharSequence должен:

реализовывать интерфейс java.lang.CharSequence;
иметь конструктор, принимающий массив байт;
определять методы length(), charAt(), subSequence() и toString()
Сигнатуры методов и ожидания по их поведению смотрите в описании интерфейса java.lang.CharSequence (JavaDoc или исходники).

P.S. В Java 9 ожидается подобная оптимизация в самом классе String: http://openjdk.java.net/jeps/254
*/
