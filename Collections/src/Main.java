import java.util.Arrays;
import java.util.Collection;
import java.util.function.*;

public class Main {

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }

    private void test1() {
        Collection<?> collection = null;
        Object object = null;

        collection.remove(object);
//        collection.add(object);
        collection.size();
        collection.contains(object);
        collection.clear();
//        collection.addAll(Arrays.asList(object));
        collection.iterator();
        collection.toArray();
    }
}
