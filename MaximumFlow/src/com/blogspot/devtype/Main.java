package com.blogspot.devtype;

import java.util.*;
import java.lang.*;
import java.io.*;
import java.util.LinkedList;

/**
 * https://stepic.org/lesson/%D0%9F%D0%BE%D1%82%D0%BE%D0%BA%D0%B8-%D0%B8-%D1%81%D0%B5%D1%82%D0%B8-12344/step/10?unit=6528
 * http://www.geeksforgeeks.org/ford-fulkerson-algorithm-for-maximum-flow-problem/
 * https://sites.google.com/site/indy256/algo/ford_fulkerson
 * https://ru.wikipedia.org/wiki/%D0%90%D0%BB%D0%B3%D0%BE%D1%80%D0%B8%D1%82%D0%BC_%D0%A4%D0%BE%D1%80%D0%B4%D0%B0_%E2%80%94_%D0%A4%D0%B0%D0%BB%D0%BA%D0%B5%D1%80%D1%81%D0%BE%D0%BD%D0%B0

 Найдите максимальный поток в сети.

 Первая строка содержит два числа 2≤v≤50 и 0≤e≤1000 — число вершин и число рёбер сети.
 Следующие ee строк описывают рёбра: каждая из них содержит три целых числа через пробел:
 0≤u[i]<v, 0≤v[i]<v, 0<c[i]<50 — исходящую и входящую вершины для этого ребра,
 а так же его пропускную способность.

 Выведите единственное число — величину максимального потока из вершины 0 в вершину v−1.

 Sample Input:
 4 5
 0 1 3
 1 2 1
 0 2 1
 1 3 1
 2 3 3

 Sample Output:
 3

 */
public class Main {
    static int V; //Number of vertices in graph

    /* Returns true if there is a path from source 's' to sink
      't' in residual graph. Also fills parent[] to store the
      path */
    boolean bfs(int rGraph[][], int s, int t, int parent[])
    {
        // Create a visited array and mark all vertices as not
        // visited
        boolean visited[] = new boolean[V];
        for(int i=0; i<V; ++i)
            visited[i]=false;

        // Create a queue, enqueue source vertex and mark
        // source vertex as visited
        LinkedList<Integer> queue = new LinkedList<Integer>();
        queue.add(s);
        visited[s] = true;
        parent[s]=-1;

        // Standard BFS Loop
        while (queue.size()!=0)
        {
            int u = queue.poll();

            for (int v=0; v<V; v++)
            {
                if (visited[v]==false && rGraph[u][v] > 0)
                {
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }

        // If we reached sink in BFS starting from source, then
        // return true, else false
        return (visited[t] == true);
    }

    // Returns tne maximum flow from s to t in the given graph
    int fordFulkerson(int graph[][], int s, int t)
    {
        int u, v;

        // Create a residual graph and fill the residual graph
        // with given capacities in the original graph as
        // residual capacities in residual graph

        // Residual graph where rGraph[i][j] indicates
        // residual capacity of edge from i to j (if there
        // is an edge. If rGraph[i][j] is 0, then there is
        // not)
        int rGraph[][] = new int[V][V];

        for (u = 0; u < V; u++)
            for (v = 0; v < V; v++)
                rGraph[u][v] = graph[u][v];

        // This array is filled by BFS and to store path
        int parent[] = new int[V];

        int max_flow = 0;  // There is no flow initially

        // Augment the flow while tere is path from source
        // to sink
        while (bfs(rGraph, s, t, parent))
        {
            // Find minimum residual capacity of the edhes
            // along the path filled by BFS. Or we can say
            // find the maximum flow through the path found.
            int path_flow = Integer.MAX_VALUE;
            for (v=t; v!=s; v=parent[v])
            {
                u = parent[v];
                path_flow = Math.min(path_flow, rGraph[u][v]);
            }

            // update residual capacities of the edges and
            // reverse edges along the path
            for (v=t; v != s; v=parent[v])
            {
                u = parent[v];
                rGraph[u][v] -= path_flow;
                rGraph[v][u] += path_flow;
            }

            // Add path flow to overall flow
            max_flow += path_flow;
        }

        // Return the overall flow
        return max_flow;
    }

    public static void main (String[] args) throws java.lang.Exception
    {
//        V = 4;
//        int graph[][] = new int[][] {
//                {0, 3, 1, 0},
//                {0, 0, 1, 1},
//                {0, 0, 0, 3},
//                {0, 0, 0, 0}
//        };
//        Main m = new Main();
//        System.out.println(3 == m.fordFulkerson(graph, 0, 3));

        Scanner s = new Scanner(System.in);
        V = s.nextInt();
        int E = s.nextInt();
        int graph[][] = new int[V][V];
        for (int e = 0; e < E; e++) {
            int u = s.nextInt();
            int v = s.nextInt();
            int c = s.nextInt();
            graph[u][v] = c;
        }
        Main m = new Main();
        System.out.println(m.fordFulkerson(graph, 0, V-1));
    }
}





