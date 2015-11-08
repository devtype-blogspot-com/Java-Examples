import java.util.List;
import java.util.Map;

public class Main {

    public static <T> boolean isSymmetric(Map<T, List<T>> graph) {
        boolean result = true;

        for (Map.Entry<T, List<T>> entry : graph.entrySet()) {
            T vertex = entry.getKey();

            List<T> adjacency = entry.getValue();
            for (T adjacencyVertex : adjacency) {
                if (!graph.get(adjacencyVertex).contains(vertex)) {
                    return false;
                }
            }
        }

        return result;
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
    }
}

/*
Один из способов представления ориентированного графа — списки смежности вершин: для каждой вершины хранится список вершин,
в которые идут дуги из данной вершины.

В Java это можно реализовать как Map<T, List<T>>, т.е. отображение, в котором ключом является вершина,
а значением — список смежных вершин, в которые идут дуги.

Реализуйте метод, который проверяет, является ли переданный ориентированный граф симметричным,
т.е. для каждой дуги из вершины X в вершину Y существует противоположно направленная дуга из вершины Y в вершину X.

 */