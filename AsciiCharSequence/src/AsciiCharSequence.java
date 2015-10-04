public class AsciiCharSequence implements java.lang.CharSequence {
    private byte[] s;

    public AsciiCharSequence (byte[] a) {
        s = a;
    }

    @Override
    public int length() {
        return s.length;
    }

    @Override
    public char charAt(int index) {
        return (char) s[index];
    }

    @Override
    public CharSequence subSequence(int start, int end) {
        byte[] ss = new byte[end - start];
        for (int i = start, j = 0; i < end; i++, j++) {
            ss[j] = s[i];
        }
        return new AsciiCharSequence(ss);
    }

//    @Override
//    public IntStream chars() {
//        return null;
//    }
//
//    @Override
//    public IntStream codePoints() {
//        return null;
//    }

    @Override
    public String toString() {
        return new String(s);
    }
}
