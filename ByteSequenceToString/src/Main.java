import java.io.UnsupportedEncodingException;

public class Main {

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public static void main(String[] args) {
        String hexString = "d0 a1 d0 a2 d0 95 d0 9f d0 98 d0 9a 3a 29".replace(" ", "");
        try {
            System.out.println(new String(hexStringToByteArray(hexString), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}
