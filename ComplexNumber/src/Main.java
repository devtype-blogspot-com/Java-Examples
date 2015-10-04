/*
https://stepic.org/lesson/Наследование-Класс-Object-12769/step/9?course=Java-Базовый-курс&unit=3117
 */

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
        ComplexNumber a = new ComplexNumber(1, 1);
        ComplexNumber b = new ComplexNumber(1, 1);
// a.equals(b) must return true
// a.hashCode() must be equal to b.hashCode()
    }

    static final class ComplexNumber {
        private final double re;
        private final double im;

        public ComplexNumber(double re, double im) {
            this.re = re;
            this.im = im;
        }

        public double getRe() {
            return re;
        }

        public double getIm() {
            return im;
        }

        // http://stackoverflow.com/questions/9650798/hash-a-double-in-java
        // http://habrahabr.ru/post/168195/

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Double.valueOf(re).hashCode();
            result = prime * result + Double.valueOf(im).hashCode();
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ComplexNumber other = (ComplexNumber) obj;
            if (re != other.re)
                return false;
            if (im != other.im)
                return false;
            return true;
        }
    }

}
