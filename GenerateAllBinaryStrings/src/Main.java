/*
https://stepic.org/lesson/K-сочетания-из-n-элементов-9481/step/16?course=Ликбез-по-дискретной-математике&unit=1759
https://gist.github.com/duyetdev/55c91420c9fafbf54691
http://stackoverflow.com/questions/275944/how-do-i-count-the-number-of-occurrences-of-a-char-in-a-string
http://aofa.cs.princeton.edu/lectures/lectures12/09Strings.pdf
http://stackoverflow.com/questions/1851134/generate-all-binary-strings-of-length-n-with-k-bits-set
 */
public class Main {
    static int oneCnt = 7;
    static String oneTwo = "11";

    static int answer = 0;

    static void getStrings( String s, int digitsLeft, int o )
    {
        if (o > oneCnt || s.contains(oneTwo))
            return;

        if (digitsLeft == 0) { // the length of string is n
            if (o == oneCnt) {
                answer++;
            }
        } else {
            getStrings(s + "0", digitsLeft - 1, o);
            getStrings( s + "1", digitsLeft - 1, o + 1);
        }
    }

    public static void main(String[] args) {
        getStrings("", 42, 0); // initial call

        System.out.println(answer);
    }
}

/*
Сколько существует бинарных (т.е. состоящих из цифр 0 и 1 ) строк длины n, содержащих k единиц, в которых никакие две единицы не стоят рядом?
Решите задачу в общем случае, а в качестве ответа введите количество таких строк для n = 42 и k = 7.
 */