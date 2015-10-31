/**
 * @see https://stepic.org/lesson/Generics-12775/step/12?unit=3123
 */
public class Main {

    public static void main(String[] args) {
        Pair<Integer, String> pair = Pair.of(1, "hello");
        Integer i = pair.getFirst(); // 1
        String s = pair.getSecond(); // "hello"

        Pair<Integer, String> pair2 = Pair.of(1, "hello");
        boolean mustBeTrue = pair.equals(pair2); // true!
        assert (mustBeTrue);
        boolean mustAlsoBeTrue = pair.hashCode() == pair2.hashCode(); // true!
        assert (mustAlsoBeTrue);
    }
}

class Pair<A, B> {

    private A a;
    private B b;

    private Pair(A a, B b) {
        this.a = a;
        this.b = b;
    }

    public A getFirst() { return a; }
    public B getSecond() { return b; }
    public static <A, B> Pair<A, B> of(A a, B b) { return new Pair<>(a, b); }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (a != null) result = prime * result + a.hashCode();
        if (b != null) result = prime * result + b.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        Pair other = (Pair) obj;
        if ( (a != null && !a.equals(other.a)) || (other.a != null && !other.a.equals(a)) ) return false;
        if ( (b != null && !b.equals(other.b)) || (other.b != null && !other.b.equals(b)) ) return false;

        return true;
    }
}

/*
Реализуйте generic-класс Pair, похожий на Optional, но содержащий пару элементов разных типов и не запрещающий элементам принимать значение null.

Реализуйте методы getFirst(), getSecond(), equals() и hashCode(), а также статический фабричный метод of().
Конструктор должен быть закрытым (private).

С корректно реализованным классом Pair должен компилироваться и успешно работать следующий код:

Pair<Integer, String> pair = Pair.of(1, "hello");
Integer i = pair.getFirst(); // 1
String s = pair.getSecond(); // "hello"

Pair<Integer, String> pair2 = Pair.of(1, "hello");
boolean mustBeTrue = pair.equals(pair2); // true!
boolean mustAlsoBeTrue = pair.hashCode() == pair2.hashCode(); // true!


Пожалуйста, не меняйте модификатор доступа класса Pair. Для корректной проверки класс должен иметь пакетную видимость.

 */