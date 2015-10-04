
public class Main {

    public static void main(String[] args) {
        A i = new B();
        i.a1();

        A j = new B();
        j.a2(1);

        B k = new B();
        k.a3();
    }
}

class A {
    public void a1() {
        System.out.println("a1");
    }

    public A a2(int a) {
        System.out.println("a2");
        return this;
    }

    protected void a3() {
        System.out.print("a3");
    }
}

class B extends A {
    public void a1() {
        System.out.println("a1 override");
    }

    public B a2(int a) {
        System.out.println("a2 override");
        return this;
    }

    /**
     * В Java при наследовании у метода модификатор доступа может быть более открытым, чем в базовом классе.
     * Ведь наследование - это, по смыслу, "расширение" одного класса другим.
     * Если бы в наследнике доступ стал менее открытым, то наследник бы "сузил" контракт родителя (в плане методов), что нарушает принципы.
     * А расширить доступ - это можно.
     */
    public void a3() {
        super.a3();
        System.out.println(" override");
    }
}