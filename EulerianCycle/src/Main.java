import java.util.*;

/**
 * @see https://stepic.org/lesson/Эйлеровы-графы-10765/step/12?course=Ликбез-по-дискретной-математике&unit=2206
 *
 * @see https://sites.google.com/site/indy256/algo/euler_cycle
 * @see https://github.com/jgrapht/jgrapht/blob/master/jgrapht-core/src/main/java/org/jgrapht/alg/EulerianCircuit.java
 *
 * @see http://www.geeksforgeeks.org/fleurys-algorithm-for-printing-eulerian-path/
 * @see http://programmersforum.ru/showthread.php?t=8626
 *
 * @see http://code.activestate.com/recipes/498243-finding-eulerian-path-in-undirected-graph/
 * @see https://github.com/DiegoAscanio/python-graphs/blob/master/eulerian.py
 * @see http://gregorulm.com/finding-an-eulerian-path/
 */
public class Main {
    static void dfs(List<Integer>[] graph, int[] curEdge, ArrayList unusedEdges, List<Integer> res, int u) {
        while (curEdge[u] < graph[u].size()) {
            int v = graph[u].get(curEdge[u]++);

            boolean b = false;
            for (Object o : unusedEdges) {
                int[] a = (int[])o;
                if ( (a[0] == u && a[1] == v) || (a[0] == v && a[1] == u) ) {
                    b = true;
                    unusedEdges.remove(o);
                    break;
                }
            }

            if (b) dfs(graph, curEdge, unusedEdges, res, v);
        }
        res.add(u);
    }

    public static List<Integer> eulerCycleUndirected(List<Integer>[] graph, int u, ArrayList unusedEdges) {
        int n = graph.length;
        int[] curEdge = new int[n];
        List<Integer> res = new ArrayList<>();
        dfs(graph, curEdge, unusedEdges, res, u);
        Collections.reverse(res);
        return res;
    }

    public static void main(String[] args) {
        Scanner s = new Scanner(System.in);
        int v = s.nextInt(), e = s.nextInt();

        ArrayList unusedEdges = new ArrayList();
        boolean[][] adjacencyMatrix = new boolean[v][v];

        List<Integer>[] g = new List[v];
        for (int i = 0; i < v; i++) g[i] = new ArrayList<>();
        for (int i = 0; i < e; i++) {
            int a = s.nextInt() - 1, b = s.nextInt() - 1;
            unusedEdges.add(new int[]{a, b});
            g[a].add(b);
            g[b].add(a);

            adjacencyMatrix[a][b] = adjacencyMatrix[b][a] = true;
        }

        boolean[] mark = BFS(adjacencyMatrix, v, 0);
        for (int i = 0; i < mark.length; i++) {
            if (!mark[i]) {
                System.out.println("NONE");
                return;
            }
        }

        List<Integer> res = eulerCycleUndirected(g, 0, unusedEdges);
        if (res.get(0).equals(res.get(res.size() - 1))) {
            for (int j = 0; j < res.size() - 1; j++) System.out.print((res.get(j) + 1) + " ");
            return;
        }

        System.out.println("NONE");
    }

    /**
     * @see http://stackoverflow.com/a/8124880/2289640
     *
     * @param adjacencyMatrix
     * @param vertexCount
     * @param givenVertex
     * @return
     */
    public static boolean[] BFS(boolean[][] adjacencyMatrix, int vertexCount, int givenVertex){
        // Result array.
        boolean[] mark = new boolean[vertexCount];

        Queue<Integer> queue = new LinkedList<Integer>();
        queue.add(givenVertex);
        mark[givenVertex] = true;

        while (!queue.isEmpty())
        {
            Integer current = queue.remove();

            for (int i = 0; i < vertexCount; ++i)
                if (adjacencyMatrix[current][i] && !mark[i])
                {
                    mark[i] = true;
                    queue.add(i);
                }
        }

        return mark;
    }
}

/*
Найдите эйлеров цикл в графе.

Формат входных данных:
В первой строке указаны два числа разделенных пробелом: v (число вершин) и e (число ребер).
В следующих e строках указаны пары вершин, соединенных ребром. Выполняются ограничения: 2≤v≤1000,0≤e≤1000 .

Формат выходных данных:
Одно слово: NONE, если в графе нет эйлерова цикла, или список вершин в порядке обхода эйлерова цикла, если он есть.


Sample Input 1:
4 2
1 2
3 2
Sample Output 1:
NONE

Sample Input 2:
3 3
1 2
2 3
3 1
Sample Output 2:
1 2 3
 */