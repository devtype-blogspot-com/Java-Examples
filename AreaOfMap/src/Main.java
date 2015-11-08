import java.util.function.*;
import java.util.Comparator.*;

/**
 *
 * @see http://stackoverflow.com/questions/15705490/find-largest-area-in-2d-array-in-c
 */
public class Main {

    static int isLand(double[][] mainBoard, int rowcount, int colcount, boolean[][] visited, int row, int col)
    {
        if (row < 0 || col < 0 || row >= rowcount || col >= colcount) return 0;
        if (mainBoard[row][col] < 0) return 0;
        if (visited[row][col]) return 0;
        visited[row][col] = true;

        return 1 + isLand(mainBoard, rowcount, colcount, visited, row, col + 1) +
                isLand(mainBoard, rowcount, colcount, visited, row + 1, col) +
                isLand(mainBoard, rowcount, colcount, visited, row, col-1) +
                isLand(mainBoard, rowcount, colcount, visited, row-1, col);
    }


    public static int maxLandSize(double[][] map) {
        int rowSize = map.length;
        int colSize = map[0].length;
        boolean[][] visisedBlocks = new boolean[rowSize][colSize];
        int maxLandSize = 0;

        for (int i = 0; i < rowSize; ++i) {
            for (int j = 0; j < colSize; ++j) {
                if (visisedBlocks[i][j]) continue;
                int basinSize = isLand(map, rowSize, colSize, visisedBlocks, i, j);
                if (basinSize > maxLandSize)
                    maxLandSize = basinSize;
            }
        }
        return maxLandSize;
    }

    public static void main(String[] args) {
        double[][] map = {
                {-1, 0, 1},
                {0, -1, 1},
                {1, 0, -1}};
        int sz = maxLandSize(map);
        System.out.print("maxLandSize = " + sz);
        assert (sz == 3);
    }
}

/*

Дан двумерный массив map вещественных чисел, описывающий рельеф некоторой местности.
Местность поделена на квадратные клетки, как шахматная доска.
Значение в ячейке массива map[i][j] задает высоту клетки (i, j) над уровнем моря.
Если высота больше либо равна нулю, то клетка находится над водой и является сушей.
Если высота меньше нуля, то клетка находится под водой.

Требуется определить площадь максимального участка суши на карте.
Площадь одной клетки равна единице.
Две клетки относятся к одному участку суши, если они соприкасаются сторонами.
Если клетки касаются только вершинами, то это разные участки суши.

Пример

Для следующего массива

double[][] map = {
        {-1, 0, 1},
        {0, -1, 1},
        {1, 0, -1}};

площадь максимального участка суши будет равна 3.

Подсказка: проще всего реализовать рекурсивный алгоритм.

 */