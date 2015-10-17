package org.stepic.java.example;

public class Test {

    public static void main(String[] args) {
//        test1();
        test2();
    }

    private static void test2() {
        System.out.println(getCallerClassAndMethodName());
        anotherMethod();
    }

    private static void anotherMethod() {
        System.out.println(getCallerClassAndMethodName());
    }

    public static String getCallerClassAndMethodName() {
        String result = null;
        try {
            throw new Exception();
        } catch (Exception e) {
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            if (stackTraceElements.length > 2) {
                StackTraceElement element = stackTraceElements[2];
                result = element.getClassName() + "#" + element.getMethodName();
            }
        }
        return result;
    }

    /**
     * java.lang.ClassCastException
     */
    private static void test1() {
        B b = new B();
        A a = (A)b;
    }
}

class A extends B {
}

class B {
}