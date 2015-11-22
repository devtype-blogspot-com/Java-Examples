import java.util.*;

class Thing {
    private int с; // стоимость предмета
    private int w; // объём предмета

    public Thing(int с, int w) {
        this.с = с;
        this.w = w;
    }

    public double getС() { return с; }
    public void setС(int с) { this.с = с; }

    public double getW() { return w; }
    public void setW(int w) { this.w = w; }

    // удельная стоимость
    public double specCost() { return this.getС() / this.getW(); }
}

public class Main {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        int n; // количество предметов
        n = scanner.nextInt();

        int W; // вместимость рюкзака
        W = scanner.nextInt();

        List<Thing> things = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            int c = scanner.nextInt();
            int w = scanner.nextInt();
            things.add(new Thing(c, w));
        }

        // сортируем в порядке убывания удельной стоимости.
        Collections.sort(things, new Comparator<Thing>() {
            public int compare(Thing t1, Thing t2) {
                if (t1.specCost() < t2.specCost()) return 1;
                if (t1.specCost() > t2.specCost()) return -1;
                return 0;
            }
        });

        double sum = 0;
        for (Thing thing : things) { // идем по предметам
            if (W >= thing.getW()) { // если помещается - берем
                sum += thing.getС();
                W -= thing.getW();
            } else {
                sum += W / thing.getW() * thing.getС(); // иначе берем сколько можно и выходим
                break;
            }
        }

        System.out.println(sum);
    }
}

/*
https://stepic.org/lesson/%D0%92%D0%B2%D0%B5%D0%B4%D0%B5%D0%BD%D0%B8%D0%B5-13238/step/10?unit=3424

Первая строка содержит количество предметов 1≤n≤10^3 и вместимость рюкзака 0≤W≤2⋅10^6.
Каждая из следующих n строк задаёт стоимость 0≤ci≤2⋅10^6 и объём 0<wi≤2⋅10^6 предмета (n, W, ci, wi — целые числа).
Выведите максимальную стоимость частей предметов (от каждого предмета можно отделить любую часть,
стоимость и объём при этом пропорционально уменьшатся), помещающихся в данный рюкзак, с точностью не менее трёх знаков после запятой.

Sample Input:
3 50
60 20
100 50
120 30

Sample Output:
180.000

 */


/*
http://neerc.ifmo.ru/wiki/index.php?title=%D0%97%D0%B0%D0%B4%D0%B0%D1%87%D0%B0_%D0%BE_%D1%80%D1%8E%D0%BA%D0%B7%D0%B0%D0%BA%D0%B5#.D0.9D.D0.B5.D0.BF.D1.80.D0.B5.D1.80.D1.8B.D0.B2.D0.BD.D1.8B.D0.B9_.D1.80.D1.8E.D0.BA.D0.B7.D0.B0.D0.BA
 */