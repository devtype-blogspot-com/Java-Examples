package com.blogspot.devtype;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.*;
import java.util.Map.*;
import java.lang.ref.SoftReference;
import java.io.*;

public class Main {
    public static void test(String[] args) {

        PrimaryGraph pg = new PrimaryGraph();
        ArrayList<Node> vs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            vs.add(pg.newNode(i));
        }
        pg.newEdge(vs.get(0), vs.get(1));
        pg.newEdge(vs.get(1), vs.get(2));
        pg.newEdge(vs.get(2), vs.get(0));
        pg.newEdge(vs.get(3), vs.get(2));
        pg.newEdge(vs.get(4), vs.get(3));
        pg.newEdge(vs.get(4), vs.get(2));
        pg.newEdge(vs.get(5), vs.get(4));

        InspectableGraph ig = pg;

        // https://github.com/DimitrisAndreou/flexigraph/blob/master/src/gr/forth/ics/graph/algo/BlockCutPointTree.java
        BlockCutPointTree bcpt = BlockCutPointTree.execute(ig);
        Biconnectivity b = bcpt.getBiconnectivity();
        System.out.println("Количество компонент: " + b.componentsCount());
        ig = bcpt.get();
        System.out.println("Узлы: ");
        for (Node n : ig.nodes()) {
            Object val = n.getValue();
            System.out.println(val
                    + " :: isBlock: "
                    + bcpt.isBlock(n)
                    + ", isCutNode: "
                    + bcpt.isCutNode(n));
        }

        int[] solution = new int[6];
        for (int i = 0; i < 6; i++) {
            solution[i] = i;
        }

        for (Node n : ig.nodes()) {
            if (bcpt.isBlock(n)) {
                Set<EdgeImpl> s = (Set<EdgeImpl>)n.getValue();
                for (EdgeImpl e : s) {
                    Integer n1 = (Integer)e.n1().getValue();
                    Integer n2 = (Integer)e.n2().getValue();
                    if (solution[n1] > n1) solution[n1] = n1;
                    if (solution[n2] > n1) solution[n2] = n1;
                    if (solution[n1] > n2) solution[n1] = n2;
                    if (solution[n2] > n2) solution[n2] = n2;
                }
            }
        }

        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < 6; i++) {
            sb.append(solution[i] + "\n");
        }
        System.out.println("Вывод:\n" + sb.toString());

    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt(), m = scanner.nextInt();
        PrimaryGraph pg = new PrimaryGraph();
        ArrayList<Node> vs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vs.add(pg.newNode(i));
        }
        for (int i = 0; i < m; i++) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            pg.newEdge(vs.get(x), vs.get(y));
        }
        BlockCutPointTree bcpt = BlockCutPointTree.execute(pg);
        InspectableGraph ig = bcpt.get();
        int[] solution = new int[n];
        for (int i = 0; i < n; i++) {
            solution[i] = i;
        }

        for (Node node : ig.nodes()) {
            if (bcpt.isBlock(node)) {
                Set<EdgeImpl> s = (Set<EdgeImpl>)node.getValue();
                for (EdgeImpl e : s) {
                    Integer n1 = (Integer)e.n1().getValue();
                    Integer n2 = (Integer)e.n2().getValue();
                    if (solution[n1] > n1) solution[n1] = n1;
                    if (solution[n2] > n1) solution[n2] = n1;
                    if (solution[n1] > n2) solution[n1] = n2;
                    if (solution[n2] > n2) solution[n2] = n2;
                }
            }
        }

        StringBuilder sb = new StringBuilder("");
        for (int i = 0; i < n; i++) {
            sb.append(solution[i] + "\n");
        }
        System.out.println(sb.toString());

    }
}

class BlockCutPointTree {
    private final InspectableGraph graph;
    private final SecondaryGraph blockCutPointTree;
    private final Object componentEdgesKey = new Object();
    private final Biconnectivity bicon;

    private BlockCutPointTree(InspectableGraph graph) {
        Args.notNull(graph);
        this.graph = graph;
        this.bicon = Biconnectivity.execute(graph);
        this.blockCutPointTree = new SecondaryGraph();
        Map<Object, Node> blocks = new HashMap<Object, Node>();
        for (Edge e : graph.edges()) {
            Object component = bicon.componentOf(e);
            Node block = blocks.get(component);
            Set<Edge> blockEdges;
            if (block == null) {
                blocks.put(component, block = blockCutPointTree.newNode());
                block.putWeakly(componentEdgesKey, blockEdges = new HashSet<Edge>());
                block.setValue(Collections.unmodifiableSet(blockEdges));
            } else {
                @SuppressWarnings("unchecked")
                Set<Edge> set = (Set<Edge>) block.get(componentEdgesKey);
                blockEdges = set;
            }


            blockEdges.add(e);
        }

        for (Node n : graph.nodes()) {
            if (bicon.isCutNode(n)) {
                blockCutPointTree.adoptNode(n);
                Set<Object> neighborBlocks = new HashSet<Object>();
                for (Edge e : graph.edges(n)) {
                    neighborBlocks.add(bicon.componentOf(e));
                }
                for (Object neighborBlock : neighborBlocks) {
                    Node block = blocks.get(neighborBlock);
                    blockCutPointTree.newEdge(n, block);
                }
            }
        }
    }

    public static BlockCutPointTree execute(InspectableGraph graph) {
        return new BlockCutPointTree(graph);
    }

    public InspectableGraph get() {
        return blockCutPointTree;
    }

    public InspectableGraph getOriginalGraph() {
        return graph;
    }

    public boolean isBlock(Node n) {
        checkOwned(n);
        return n.has(componentEdgesKey);
    }

    public Set<Edge> getBlockEdges(Node blockNode) {
        @SuppressWarnings("unchecked")
        Set<Edge> edges = (Set<Edge>) blockNode.get(componentEdgesKey);
        if (edges == null) {
            throw new IllegalArgumentException("Not a block node");
        }
        return edges;
    }

    public boolean isCutNode(Node n) {
        return !isBlock(n);
    }

    private void checkOwned(Node n) {
        if (!blockCutPointTree.containsNode(n)) {
            throw new IllegalArgumentException("Not a node of the block-cutpoint tree: " + n);
        }
    }

    public Biconnectivity getBiconnectivity() {
        return bicon;
    }
}

enum Direction {
    
    OUT(1),
    
    IN(2),
    
    EITHER(1 | 2);

    private final int direction;
    private static final int out = 1;
    private static final int in = 2;

    private Direction(int i) {
        direction = i;
    }

    
    public boolean isOut() {
        return (direction & out) != 0;
    }

    
    public boolean isIn() {
        return (direction & in) != 0;
    }

    
    public Direction flip() {
        switch (this) {
            case OUT:
                return IN;
            case IN:
                return OUT;
            default:
                return this;
        }
    }
}


interface InspectableGraph {

    boolean isEmpty();


    int nodeCount();


    int edgeCount();


    ExtendedListIterable<Edge> edges();


    ExtendedListIterable<Node> nodes();


    ExtendedListIterable<Edge> edges(Node node);


    ExtendedListIterable<Edge> edges(Node node, Direction direction);


    ExtendedListIterable<Edge> edges(Node n1, Node n2);


    ExtendedListIterable<Edge> edges(Node n1, Node n2, Direction direction);


    ExtendedListIterable<Node> adjacentNodes(Node node);


    ExtendedListIterable<Node> adjacentNodes(Node node, Direction direction);


    boolean areAdjacent(Node n1, Node n2);


    boolean areAdjacent(Node n1, Node n2, Direction direction);


    Edge anEdge();


    Edge anEdge(Node node);


    Edge anEdge(Node node, Direction direction);


    Edge anEdge(Node n1, Node n2);


    Edge anEdge(Node n1, Node n2, Direction direction);


    Node aNode();


    Node aNode(Node neighbor);


    Node aNode(Node neighbor, Direction direction);


    boolean containsEdge(Edge edge);


    boolean containsNode(Node node);


    int inDegree(Node node);


    int outDegree(Node node);


    int degree(Node node);


    int degree(Node node, Direction direction);


    void addGraphListener(GraphListener listener);


    void removeGraphListener(GraphListener listener);


    void addNodeListener(NodeListener listener);


    void removeNodeListener(NodeListener listener);


    void addEdgeListener(EdgeListener listener);


    void removeEdgeListener(EdgeListener listener);


    List<NodeListener> getNodeListeners();


    List<EdgeListener> getEdgeListeners();


    Tuple tuple();


    void hint(Hint hint);
}

interface Node extends Tuple {
    Path asPath();
}

interface Edge extends Tuple {
    
    Node n1();

    
    Node n2();

    
    boolean isIncident(Node node);

    
    Node opposite(Node node);

    
    boolean isSelfLoop();

    
    boolean isIncident(Edge other);

    
    Node getIntersection(Edge other);

    
    Node getIntersection(boolean startFromN1, Edge other);

    
    boolean areParallel(Edge other);

    
    Orientation testOrientation(Edge other);

    
    Path asPath();

    
    Path asPath(Node headNode);

    

}

enum Orientation {
    SAME, OPPOSITE, UNDEFINED
}

interface Path {
    
    Node headNode();

    
    Edge headEdge();

    
    int size();

    
    int edgeCount();

    
    int nodeCount();

    
    Node tailNode();

    
    Edge tailEdge();

    
    boolean isHamilton();

    
    boolean isEuler();

    
    ExtendedIterable<Path> steps();

    
    ExtendedIterable<Node> nodes();

    
    ExtendedIterable<Edge> edges();

    
    Node getNode(int index);

    
    Edge getEdge(int index);

    
    Path slice(int start, int end);

    
    Path headPath(int steps);

    
    Path tailPath(int steps);

    
    Path[] split(int position);

    
    Path append(Path other);

    
    boolean isCycle();

    
    Path replaceFirst(Path subpath, Path replacement);

    
    Path replace(int start, int end, Path replacement);

    
    Path replaceAll(Path subpath, Path replacement);

    
    boolean contains(Path subpath);

    
    int find(Path subpath);

    
    int find(Path path, int from);

    
    Path reverse();
}

interface Tuple extends Serializable {

    
    Object put(Object key, Object value);

    
    Object get(Object key);


    
    Object remove(Object key);

    
    boolean has(Object key);


    
    Object putWeakly(Object key, Object value);

    
    Object getValue();

    
    Object setValue(Object value);

    
    Boolean getBoolean(Object key);

    
    int getInt(Object key);

    
    long getLong(Object key);

    
    double getDouble(Object key);

    
    float getFloat(Object key);

    
    Character getChar(Object key);

    
    short getShort(Object key);

    
    Number getNumber(Object key);

    
    String getString(Object key);

    
    InspectableGraph getInspectableGraph(Object key);

    
    Node getNode(Object key);

    
    Edge getEdge(Object key);

    
    Graph getGraph(Object key);

    
    Tuple getTuple(Object key);


    
    void copyInto(Tuple tuple);

    
    boolean equalValues(Tuple other);

    
    Set<Object> keySet();

    
    Tuple getParentTuple();


    
    void setParentTuple(Tuple parent);

    
    Map<Object, Object> asMap();
}

interface Graph extends InspectableGraph {
    
    Edge newEdge(Node node1, Node node2);

    
    Edge newEdge(Node node1, Node node2, Object value);

    
    boolean removeEdge(Edge edge);

    
    int removeEdges(Iterable<Edge> edges);

    
    int removeAllEdges();

    
    Node newNode();

    
    Node newNode(Object value);

    
    Node[] newNodes(int count);

    
    Node[] newNodes(Object ... values);

    
    boolean removeNode(Node node);

    
    int removeNodes(Iterable<Node> nodes);

    
    int removeAllNodes();

    
    OrderManager getOrderManager();

    
    void importGraph(Graph graph);


    
    Collection<Edge> importGraph(Graph graph, Iterable<Node> nodes);

    
    boolean isPrimary();

    
    boolean reinsertNode(Node n);

    
    boolean reinsertEdge(Edge e);

    
    interface OrderManager {
        
        void moveNodeToFront(Node node);

        
        void moveNodeToBack(Node node);

        
        void moveNodeBefore(Node node, Node beforeWhat);

        
        void moveNodeAfter(Node node, Node afterWhat);

        
        void moveEdgeToFront(Edge edge, boolean onSourceNode);

        
        void moveEdgeToBack(Edge edge, boolean onSourceNode);

        
        void moveEdgeBefore(Edge edge, boolean onSourceNode, Edge beforeWhat);

        
        void moveEdgeAfter(Edge edge, boolean onSourceNode, Edge afterWhat);
    }
}

class ExtendedIterable<E> implements Iterable<E> {
    private final Iterable<E> iter;
    private final int expectedSize;
    protected List<Filter<? super E>> filters;

    protected ExtendedIterable() {
        this(-1);
    }


    protected ExtendedIterable(int expectedSize) {
        iter = new Iterable<E>() {
            public Iterator<E> iterator() {
                return ExtendedIterable.this.iterator();
            }
        };
        this.expectedSize = expectedSize;
    }

    public ExtendedIterable(Iterable<E> iter) {
        this(iter, -1);
    }

    public ExtendedIterable(Iterable<E> iter, int expectedSize) {
        Args.notNull(iter);
        this.iter = iter;
        this.expectedSize = expectedSize;
    }

    
    public int size() {
        int size = 0;
        for (E e : this) { size++; }
        return size;
    }

    public Iterator<E> iterator() {
        if (filters == null || filters.isEmpty()) {
            return iter.iterator();
        }
        return new FilteringIterator<E>(iter.iterator()) {
            protected boolean accept(E element) {
                for (Filter<? super E> filter : filters) {
                    if (!filter.accept(element)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }

    public Collection<E> drainTo(Collection<E> col) {
        Args.notNull(col);
        for (E e : this) {
            col.add(e);
        }
        return col;
    }

    public Set<E> drainTo(Set<E> set) {
        drainTo((Collection<E>)set);
        return set;
    }

    public List<E> drainTo(List<E> list) {
        drainTo((Collection<E>)list);
        return list;
    }

    public Set<E> drainToSet() {
        return drainTo(new LinkedHashSet<E>());
    }

    public List<E> drainToList() {
        ArrayList<E> list = expectedSize > 0 ? new ArrayList<E>(expectedSize) : new ArrayList<E>();
        return drainTo(list);
    }

    public ExtendedIterable<E> filter(Filter<? super E> filter) {
        Args.notNull(filter);
        if (filters == null) {
            filters = new ArrayList<Filter<? super E>>();
        }
        filters.add(filter);
        return this;
    }

    public ExtendedIterable<E> filter(List<Filter<? super E>> filters) {
        Args.notNull(filters);
        for (Filter<? super E> f : filters) {
            filter(f);
        }
        return this;
    }

    @Override
    public String toString() {
        return drainToList().toString();
    }

    public static <K> ExtendedIterable<K> wrap(Iterable<K> iter) {
        if (iter instanceof ExtendedIterable) {
            return (ExtendedIterable<K>)iter;
        }
        Args.notNull(iter);
        return new ExtendedIterable<K>(iter);
    }

    public static <E> Collection<E> drainTo(Iterator<? extends E> iter, Collection<E> col) {
        Args.notNull(col);
        if (iter != null) {
            while (iter.hasNext()) {
                col.add(iter.next());
            }
        }
        return col;
    }

    public static <E> Set<E> drainTo(Iterator<? extends E> iter, Set<E> set) {
        drainTo(iter, (Collection<E>)set);
        return set;
    }

    public static <E> List<E> drainTo(Iterator<? extends E> iter, List<E> list) {
        drainTo(iter, (Collection<E>)list);
        return list;
    }

    public static <E> Set<E> drainToSet(Iterator<? extends E> iter) {
        return drainTo(iter, new LinkedHashSet<E>());
    }

    public static <E> List<E> drainToList(Iterator<? extends E> iter) {
        return drainTo(iter, new ArrayList<E>());
    }

    @SuppressWarnings("unchecked")
    private static final ExtendedIterable EMPTY = new ExtendedIterable(Collections.EMPTY_LIST);

    @SuppressWarnings("unchecked")
    public static <E> ExtendedIterable<E> empty() {
        return EMPTY;
    }
}

abstract class FilteringIterator<E> implements Iterator<E> {
    private final PushBackIterator<E> iterator;

    public FilteringIterator(Iterator<E> iterator) {
        Args.notNull(iterator);
        this.iterator = new PushBackIterator<E>(iterator);
    }

    protected abstract boolean accept(E element);

    private void checkNext() {
        while (iterator.hasNext()) {
            E next = iterator.next();
            if (accept(next)) {
                iterator.pushBack();
                break;
            }
        }
    }

    public boolean hasNext() {
        checkNext();
        return iterator.hasNext();
    }

    public E next() {
        checkNext();
        return iterator.next();
    }

    public void remove() {
        iterator.remove();
    }
}

class PushBackIterator<E> implements Iterator<E> {
    private final Iterator<E> iterator;

    public PushBackIterator(Iterator<E> iterator) {
        Args.notNull(iterator);
        this.iterator = iterator;
    }

    private E saved = null;
    private E last;

    public boolean hasNext() {
        return saved != null || iterator.hasNext();
    }

    public E next() {
        if (saved != null) {
            try {
                return last = saved;
            } finally {
                saved = null;
            }
        }
        return last = iterator.next();
    }

    public void pushBack() {
        saved = last;
    }


    public void remove() {
        if (saved != null) {
            throw new IllegalStateException("Cannot remove when next element has been pushed back");
        }
        iterator.remove();
    }
}

final class SecondaryGraph extends AbstractListGraph {
    private static final long serialVersionUID = 2348144112946233116L;

    private final Object nodeDataKey = new SerializableObject();
    private final Object edgeOutRefKey = new SerializableObject();
    private final Object edgeInRefKey = new SerializableObject();

    public SecondaryGraph() { }

    public SecondaryGraph(InspectableGraph graph) {
        adoptGraph(graph);
    }

    public SecondaryGraph(InspectableGraph graph, Iterable<Node> nodes) {
        Args.notNull(graph, nodes);
        for (Node n : nodes) {
            adoptNode(n);
        }
        for (Node n : nodes) {
            for (Edge e : graph.edges(n)) {
                if (this.containsNode(e.opposite(n))) {
                    adoptEdge(e);
                }
            }
        }
    }

    public SecondaryGraph(Iterable<Node> nodes, Iterable<Edge> edges) {
        if (nodes != null) {
            for (Node n : nodes) {
                adoptNode(n);
            }
        }
        if (edges != null) {
            for (Edge e : edges) {
                adoptEdge(e);
            }
        }
    }

    @Override FastLinkedList<EdgeImpl> getOutEdges(NodeImpl node) {
        return ((NodeData)node.get(nodeDataKey)).outEdges;
    }

    @Override FastLinkedList<EdgeImpl> getInEdges(NodeImpl node) {
        return ((NodeData)node.get(nodeDataKey)).inEdges;
    }

    @Override final Accessor<NodeImpl> getNodeRef(NodeImpl node) {
        return ((NodeData)node.get(nodeDataKey)).ref;
    }

    @SuppressWarnings("unchecked")
    @Override final Accessor<EdgeImpl> getEdgeOutRef(EdgeImpl edge) {
        return (Accessor<EdgeImpl>)edge.get(edgeOutRefKey);
    }

    @SuppressWarnings("unchecked")
    @Override final Accessor<EdgeImpl> getEdgeInRef(EdgeImpl edge) {
        return (Accessor<EdgeImpl>)edge.get(edgeInRefKey);
    }

    @Override final void removeEdgeRefs(EdgeImpl edge) {
        edge.remove(edgeInRefKey);
        edge.remove(edgeOutRefKey);
    }

    @Override final void setNodeRef(NodeImpl node, Accessor<NodeImpl> ref) {
        ((NodeData)node.get(nodeDataKey)).ref = ref;
    }

    @Override final void setEdgeOutRef(EdgeImpl edge, Accessor<EdgeImpl> ref) {
        edge.putWeakly(edgeOutRefKey, ref);
    }

    @Override final void setEdgeInRef(EdgeImpl edge, Accessor<EdgeImpl> ref) {
        edge.putWeakly(edgeInRefKey, ref);
    }

    @Override final void removeNodeRef(NodeImpl node) {
        ((NodeData)node.get(nodeDataKey)).ref = null;
    }

    @Override final void initNode(NodeImpl node) {
        node.putWeakly(nodeDataKey, new NodeData());
    }

    @Override public final boolean isPrimary() {
        return false;
    }



    public boolean adoptGraph(InspectableGraph graph) {
        Args.notNull(graph);
        boolean changed = adoptNodes(graph.nodes());
        changed |= adoptEdges(graph.edges());
        return changed;
    }

    public boolean adoptNode(Node node) {
        Args.notNull(node);
        if (containsNode(node)) {
            return false;
        }
        final NodeImpl nodeImpl = (NodeImpl)node;
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.NODE_ADDED, node), new Runnable() {
            public void run() {
                NodeData nodeData = new NodeData();
                nodeData.ref = nodes.addLast(nodeImpl);
                nodeImpl.putWeakly(nodeDataKey, nodeData);
            }
        });
        return true;
    }

    public boolean adoptEdge(final Edge edge) {
        Args.notNull(edge);
        if (containsEdge(edge)) {
            return false;
        }
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.EDGE_ADDED, edge), new Runnable() {
            public void run() {
                EdgeImpl edgeImpl = (EdgeImpl)edge;
                adoptNode(edgeImpl.n1);
                adoptNode(edgeImpl.n2);
                edgeCount++;
                setEdgeOutRef(edgeImpl, getOutEdges(edgeImpl.n1).addLast(edgeImpl));
                setEdgeInRef(edgeImpl, getInEdges(edgeImpl.n2).addLast(edgeImpl));
            }
        });
        return true;
    }

    public boolean adoptPath(Path path) {
        return adoptEdges(path.edges());
    }

    public boolean adoptNodes(Iterable<Node> nodes) {
        Args.notNull(nodes);
        boolean changed = false;
        for (Node n : nodes) {
            changed |= adoptNode(n);
        }
        return changed;
    }

    public boolean adoptEdges(Iterable<Edge> edges) {
        Args.notNull(edges);
        boolean changed = false;
        for (Edge e : edges) {
            changed |= adoptEdge(e);
        }
        return changed;
    }

    public boolean removeGraph(InspectableGraph graph) {
        Args.notNull(graph);
        return removeNodes(graph.nodes()) != 0;
    }

    public boolean retainGraph(InspectableGraph graph) {
        Args.notNull(graph);
        boolean changed = false;
        for (Node n : nodes()) {
            if (!graph.containsNode(n)) {
                changed = true;
                removeNode(n);
            }
        }
        InspectableGraph one, other;
        if (this.edgeCount() > graph.edgeCount()) {
            one = graph;
            other = this;
        } else {
            one = this;
            other = graph;
        }
        for (Edge e : one.edges()) {
            if (!other.containsEdge(e)) {
                changed = true;
                removeEdge(e);
            }
        }
        return changed;
    }

    public boolean reinsertNode(Node n) {
        return adoptNode(n);
    }

    public boolean reinsertEdge(Edge e) {
        return adoptEdge(e);
    }

    private static class NodeData implements Serializable {
        private static final long serialVersionUID = 1L;
        Accessor<NodeImpl> ref;
        final FastLinkedList<EdgeImpl> inEdges = new FastLinkedList<EdgeImpl>();
        final FastLinkedList<EdgeImpl> outEdges = new FastLinkedList<EdgeImpl>();
    }
}

final class NodeImpl extends TupleImpl implements Node {
    FastLinkedList<EdgeImpl> outEdges = new FastLinkedList<EdgeImpl>();
    FastLinkedList<EdgeImpl> inEdges = new FastLinkedList<EdgeImpl>();
    Accessor<NodeImpl> reference;

    NodeImpl(Object value) {
        super(value);
    }

    public String toString() {
        return getValue() != null ? getValue().toString() : "null";
    }

    public Path asPath() {
        return new AbstractPath() {
            public Node headNode() {
                return NodeImpl.this;
            }

            public Edge headEdge() {
                throw new NoSuchElementException();
            }

            public int size() {
                return 0;
            }

            public Node tailNode() {
                return NodeImpl.this;
            }

            public Edge tailEdge() {
                throw new NoSuchElementException();
            }

            public ExtendedIterable<Path> steps() {
                return ExtendedIterable.<Path>empty();
            }

            public Node getNode(int index) {
                if (index == 0) {
                    return NodeImpl.this;
                }
                throw new IllegalArgumentException("Illegal index specified");
            }

            public Edge getEdge(int index) {
                throw new NoSuchElementException();
            }

            public Path slice(int start, int end) {
                if (start == 0 && end == 0) {
                    return this;
                }
                throw new IllegalArgumentException("Illegal indexes specified");
            }

            public Path headPath(int steps) {
                if (steps == 0) {
                    return this;
                }
                throw new IllegalArgumentException("Illegal index specified");
            }

            public Path tailPath(int steps) {
                if (steps == 0) {
                    return this;
                }
                throw new IllegalArgumentException("Illegal index specified");
            }

            public Path reverse() {
                return this;
            }
        };
    }
}

final class EdgeImpl extends TupleImpl implements Edge {
    final NodeImpl n1, n2;
    Accessor<EdgeImpl> outReference, inReference;

    EdgeImpl(NodeImpl n1, NodeImpl n2, Object value) {
        super(value);
        this.n1 = n1;
        this.n2 = n2;
    }

    public NodeImpl n1() {
        return n1;
    }

    public NodeImpl n2() {
        return n2;
    }

    public boolean isIncident(Node node) {
        return node == n1 || node == n2;
    }

    public NodeImpl opposite(Node node) {
        if (isIncident(node)) {
            if (node == n1) {
                return n2;
            } else {
                return n1;
            }
        } else {
            throw new RuntimeException("Edge: " + this + " does not contain node: " + node);
        }
    }

    public boolean isSelfLoop() {
        return n1 == n2;
    }

    public boolean isIncident(Edge other) {
        Args.notNull(other);
        return n1 == other.n1() || n1 == other.n2() || n2 == other.n1() || n2 == other.n2();
    }


    public NodeImpl getIntersection(Edge other) {
        Args.notNull(other);
        if (n1 == other.n1() || n1 == other.n2()) {
            return n1;
        }
        if (n2 == other.n1() || n2 == other.n2()) {
            return n2;
        }
        return null;
    }

    public NodeImpl getIntersection(boolean startFromN1, Edge other) {
        if (startFromN1) {
            return getIntersection(other);
        }
        Args.notNull(other);
        if (n2 == other.n1() || n2 == other.n2()) {
            return n2;
        }
        if (n1 == other.n1() || n1 == other.n2()) {
            return n1;
        }
        return null;
    }

    public Path asPath() {
        return asPath(n1());
    }

    public Path asPath(final Node head) {
        Args.isTrue("Node not contained in edge", isIncident(head));
        return new AbstractPath() {
            final Node tail = EdgeImpl.this.opposite(head);

            public Node headNode() {
                return head;
            }

            public Edge headEdge() {
                return EdgeImpl.this;
            }

            public int size() {
                return 1;
            }

            public Node tailNode() {
                return tail;
            }

            public Edge tailEdge() {
                return EdgeImpl.this;
            }

            public ExtendedIterable<Path> steps() {
                return ExtendedIterable.wrap(Collections.<Path>singleton(this));
            }

            public Node getNode(int index) {
                switch (index) {
                    case -2: return head;
                    case -1: return tail;
                    case 0: return head;
                    case 1: return tail;
                }
                throw new IllegalArgumentException("Illegal index specified: " + index);
            }

            public Edge getEdge(int index) {
                if (index != 0) {
                    throw new IllegalArgumentException("Illegal index specified" + index);
                }
                return EdgeImpl.this;
            }

            public Path slice(int start, int end) {
                switch (start) {
                    case 0:
                        if (end == 0) {
                            return head.asPath();
                        } else if (end == 1) {
                            return this;
                        }
                        break;

                    case 1:
                        if (end == 1) {
                            return tail.asPath();
                        }
                }
                throw new IllegalArgumentException("Illegal indexes specified");
            }

            public Path headPath(int steps) {
                if (steps == 1) {
                    return this;
                } else if (steps == 0) {
                    return head.asPath();
                } else {
                    throw new IllegalArgumentException("Illegal index specified");
                }
            }

            public Path tailPath(int steps) {
                if (steps == 1) {
                    return this;
                } else if (steps == 0) {
                    return tail.asPath();
                } else {
                    throw new IllegalArgumentException("Illegal index specified");
                }
            }

            public Path reverse() {
                return EdgeImpl.this.asPath(tail);
            }
        };
    }

    @Override
    public String toString() {
        return "{" + n1 + "->" + n2 + (getValue() == null ? "" : ", (" + getValue().toString() + ")") + "}";
    }

    public Orientation testOrientation(Edge other) {
        Args.notNull(other);
        Node n = getIntersection(other);
        if (n == null || isSelfLoop() || other.isSelfLoop()) {
            return Orientation.UNDEFINED;
        }
        int x = 0;
        if (n == n2) {
            x++;
        }
        if (n == other.n2()) {
            x++;
        }
        switch (x % 2) {
            case 1: return Orientation.SAME;
            default: return Orientation.OPPOSITE;
        }
    }

    public boolean areParallel(Edge other) {
        Args.notNull(other);
        if (n1 == other.n1()) {
            return n2 == other.n2();
        } else if (n1 == other.n2()) {
            return n2 == other.n1();
        }
        return false;
    }
}

class TupleImpl extends AbstractTuple {
    private transient Map<Object, Object> values;

    public TupleImpl() { }

    public TupleImpl(Object value) {
        super(value);
    }

    public TupleImpl(Map<?, ?> values) {
        Args.notNull(values);
        if (values.isEmpty()) {
            return;
        }
        lazyInit();
        for (Entry<?, ?> entry : values.entrySet()) {
            this.values.put(entry.getKey(), entry.getValue());
        }
    }

    public TupleImpl(Tuple copy) {
        this(copy.asMap());
        setValue(copy.getValue());
    }

    private void lazyInit() {
        if (values == null) {
            values = new WeakHashMap<Object, Object>(1);
        }
    }

    protected Object getLocally(Object key) {
        if (values == null) {
            return null;
        }
        return unmask(values.get(key));
    }

    public Object remove(Object key) {
        if (values == null) {
            return null;
        }
        Object old = values.remove(key);
        if (values.size() == 0) {
            values = null;
        }
        return unmask(old);
    }

    public Object put(Object key, Object value) {
        lazyInit();
        return unmask(values.put(key, mask(key, value)));
    }

    public Object putWeakly(Object key, Object value) {
        lazyInit();
        Args.notNull("Null cannot be put weakly - use normal put instead", key);
        return values.put(key, value);
    }

    private StrongValue mask(Object key, Object value) {
        return new StrongValue(key, value);
    }

    private Object unmask(Object value) {
        if (value instanceof StrongValue) {
            return ((StrongValue)value).value;
        }
        return value;
    }

    protected boolean hasLocally(Object key) {
        if (values == null) {
            return false;
        }
        return values.containsKey(key);
    }

    public Set<Object> keySet() {
        if (values == null) {
            return Collections.emptySet();
        }
        Set<Object> keys = new HashSet<Object>();
        for (Entry<Object, Object> entry : values.entrySet()) {
            if (entry.getValue() instanceof StrongValue) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public Map<Object, Object> asMap() {
        return new AbstractMap<Object, Object>() {
            public Object remove(Object key) {
                return TupleImpl.this.remove(key);
            }

            public Object get(Object key) {
                return TupleImpl.this.get(key);
            }

            public boolean containsKey(Object key) {
                return TupleImpl.this.has(key);
            }

            public Object put(Object key, Object value) {
                return TupleImpl.this.put(key, value);
            }

            public Set<Map.Entry<Object, Object>> entrySet() {
                if (values == null) {
                    return Collections.emptySet();
                }
                final Set<Map.Entry<Object, Object>> delegate = values.entrySet();
                return new Set<Map.Entry<Object, Object>>() {
                    public boolean add(Map.Entry<Object, Object> o) {
                        Object oldValue = TupleImpl.this.get(o.getKey());
                        boolean existed;
                        if (oldValue != null) {
                            existed = true;
                        } else {
                            existed = TupleImpl.this.has(o.getKey());
                        }
                        TupleImpl.this.put(o.getKey(), o.getValue());
                        if (oldValue != o.getValue()) {
                            return true;
                        }
                        return !existed;
                    }

                    public boolean addAll(Collection<? extends Entry<Object, Object>> c) {
                        boolean changed = false;
                        for (Entry<Object, Object> entry : c) {
                            changed |= add(entry);
                        }
                        return changed;
                    }

                    public boolean contains(Object o) {
                        return delegate.contains(o);
                    }

                    public boolean remove(Object o) {
                        return delegate.remove(o);
                    }

                    public <T> T[] toArray(T[] a) {
                        return delegate.toArray(a);
                    }

                    public boolean containsAll(Collection<?> c) {
                        return delegate.containsAll(c);
                    }

                    public boolean removeAll(Collection<?> c) {
                        return delegate.removeAll(c);
                    }

                    public boolean retainAll(Collection<?> c) {
                        return delegate.retainAll(c);
                    }

                    public void clear() {
                        delegate.clear();
                    }

                    public boolean isEmpty() {
                        return delegate.isEmpty();
                    }

                    public Iterator<Entry<Object, Object>> iterator() {
                        final Iterator<Entry<Object, Object>> iDelegate = delegate.iterator();
                        return new Iterator<Entry<Object, Object>>() {
                            public boolean hasNext() {
                                return iDelegate.hasNext();
                            }

                            public Entry<Object, Object> next() {
                                Entry<Object, Object> e = iDelegate.next();
                                e.setValue(unmask(e.getValue()));
                                return e;
                            }

                            public void remove() {
                                iDelegate.remove();
                            }
                        };
                    }

                    public int size() {
                        return delegate.size();
                    }

                    public Object[] toArray() {
                        return delegate.toArray();
                    }
                };
            }

            public int size() {
                if (values == null) {
                    return 0;
                }
                return values.size();
            }

            public String toString() {
                if (values == null || values.isEmpty()) {
                    return "{}";
                }
                StringBuilder sb = new StringBuilder();
                sb.append("{");
                for (Object key : values.keySet()) {
                    sb.append(key);
                    sb.append("=");
                    sb.append(get(key));
                    sb.append(", ");
                }
                sb.delete(sb.length() - 2, sb.length());
                sb.append("}");
                return sb.toString();
            }
        };
    }

    private final class StrongValue implements Serializable {
        final Object key;
        final Object value;

        StrongValue(Object key, Object value) {
            this.key = key;
            this.value = value;
        }

        public boolean equals(Object o) {
            if (!(o instanceof StrongValue)) {
                return false;
            }

            StrongValue sv = (StrongValue)o;
            return value.equals(sv.value) && key.equals(sv.key);
        }

        public int hashCode() {
            int hash = 0;
            if (key != null) { hash += key.hashCode(); }
            if (value != null) { hash += value.hashCode(); }
            return hash;
        }

        public String toString() {

            return "[StrongValue: " + value + "]";
        }
    }

    private void writeObject(ObjectOutputStream out) throws Exception {
        out.defaultWriteObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        if (values != null) {
            map.putAll(values);
        }
        out.writeObject(map);
    }

    @SuppressWarnings("unchecked")
    private void readObject(ObjectInputStream in) throws Exception {
        in.defaultReadObject();
        Map<Object, Object> map = (Map<Object, Object>)in.readObject();
        if (!map.isEmpty()) {
            values = new WeakHashMap<Object, Object>(map);
        }
    }

    public String toString() {
        if (values == null || values.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Object key : values.keySet()) {
            sb.append(key);
            sb.append("->");
            sb.append(get(key));
            sb.append(", ");
        }
        sb.append("value=");
        sb.append(getValue());
        sb.append("]");
        return sb.toString();
    }

}

class Biconnectivity {
    private final Object DISCOVERER = new Object();
    private final Object MARKING_NODE = new Object();
    private final Object IS_CUT_NODE = new Object();

    private final InspectableGraph graph;
    private final Dfs dfs;
    private final Graph cycleGraph;
    private final Clusterer connectedComponents;

    private final int rootsCount;

    private Biconnectivity(InspectableGraph graph) {
        Args.notNull(graph);
        final List<Node> roots = new LinkedList<Node>();
        this.graph = graph;
        dfs = new Dfs(graph, Direction.EITHER) {
            @Override protected boolean visitNewTree(Node v) {
                roots.add(v);
                return false;
            }

            @Override protected boolean visitTreeEdge(Path path) {
                Node to = path.tailNode();
                Edge e = path.tailEdge();
                to.putWeakly(DISCOVERER, e);
                return false;
            }
        };
        cycleGraph = new PrimaryGraph();

        dfs.execute();
        this.rootsCount = roots.size();

        for (Node n : roots) {
            preorderTraverse(n);
        }

        connectedComponents = Clusterers.connectedComponents(cycleGraph);

        for(Node n : graph.nodes()) {
            computeCutVertex(n);
        }
    }

    
    public static Biconnectivity execute(InspectableGraph graph) {
        return new Biconnectivity(graph);
    }

    
    public Object componentOf(Edge e) throws IllegalArgumentException {
        if(!graph.containsEdge(e)) {
            throw new IllegalArgumentException("Edge " + e + " does not belong to the graph upon which this algorithm was most recently executed");
        }
        Node nodeMarking = nodeMarking(e);
        if (nodeMarking == null) {
            return null;
        }
        return connectedComponents.findClusterOf(nodeMarking);
    }

    
    public boolean isCutNode(Node v) throws IllegalArgumentException {
        if(!graph.containsNode(v)) {
            throw new IllegalArgumentException("Node " + v + " does not belong to the graph upon which this algorithm was most recently executed");
        }

        return v.getBoolean(IS_CUT_NODE);
    }

    private void computeCutVertex(Node v) {
        Iterator<Edge> edges = graph.edges(v).iterator();
        if (edges.hasNext()) {
            Object component = componentOf(edges.next());
            while(edges.hasNext()) {
                if (!component.equals( componentOf(edges.next()))) {
                    v.putWeakly(IS_CUT_NODE, true);
                    return;
                }
            }
        }
        v.putWeakly(IS_CUT_NODE, false);
    }

    private void preorderTraverse(Node v) {
        LinkedList<Edge> discoveryEdges = new LinkedList<Edge>();

        for (Edge e : graph.edges(v)) {


            if (dfs.isTreeEdge(e)
                    && dfs.getParent(e.opposite(v)) == v) {
                discoveryEdges.addFirst(e);
            } else if (dfs.isBackEdge(e) && !isMarked(e)) {
                Node vE = cycleGraph.newNode(e);
                mark(e, vE);
                Node u = e.opposite(v);
                while (u != v) {
                    Edge discoversU = discoverer(u);
                    if (isMarked(discoversU)) {
                        cycleGraph.newEdge(nodeMarking(discoversU), vE, null);
                        break;
                    } else {
                        Node vDU = cycleGraph.newNode(discoversU);
                        mark(discoversU, vDU);
                        cycleGraph.newEdge(vE, vDU, null);
                    }
                    u = discoversU.opposite(u);
                }
            }
        }
        for (Edge e : discoveryEdges) {
            preorderTraverse( e.opposite(v) );



            if (!isMarked(e)) {
                mark(e, cycleGraph.newNode(e));
            }
        }
    }

    private void mark(Edge e, Node vE) {
        e.putWeakly(MARKING_NODE, vE);
    }

    private boolean isMarked(Edge e) {
        return e.has(MARKING_NODE);
    }

    private Node nodeMarking(Edge e) {
        return e.getNode(MARKING_NODE);
    }

    private Edge discoverer(Node v) {
        return v.getEdge(DISCOVERER);
    }

    public InspectableGraph getGraph() {
        return graph;
    }

    
    public int componentsCount() {
        return rootsCount;
    }
}

abstract class ExtendedListIterable<E> extends ExtendedIterable<E> {
    public ExtendedListIterable() {
        super();
    }

    public ExtendedListIterable(int expectedSize) {
        super(expectedSize);
    }

    public final ListIterator<E> listIterator() {
        if (filters == null || filters.isEmpty()) {
            return listIteratorImpl();
        }
        return new FilteringListIterator<E>(listIteratorImpl(), new Filter<E>() {
            public boolean accept(E element) {
                for (Filter<? super E> filter : filters) {
                    if (!filter.accept(element)) {
                        return false;
                    }
                }
                return true;
            }
        });
    }

    @Override
    public ExtendedListIterable<E> filter(List<Filter<? super E>> filters) {
        super.filter(filters);
        return this;
    }

    protected abstract ListIterator<E> listIteratorImpl();

    @Override public Iterator<E> iterator() {
        return listIterator();
    }
}

interface Filter<E> {
    boolean accept(E element);
}

class FilteringListIterator<E> implements ListIterator<E> {
    private final Filter<? super E> filter;
    private final ListIterator<E> listIterator;

    private int index;
    private int offset;
    private E elementToReturn;
    private Accessed accessed = Accessed.NONE;

    private enum Accessed {
        NONE(false) {
            void moveForward(ListIterator<?> listIterator) {
                throw new IllegalStateException();
            }
            void moveBackward(ListIterator<?> listIterator) {
                throw new IllegalStateException();
            }
        }, NEXT(true) {
            void moveForward(ListIterator<?> listIterator) {
                listIterator.next();
            }
            void moveBackward(ListIterator<?> listIterator) {
                listIterator.previous();
            }
        }, PREVIOUS(true) {
            void moveForward(ListIterator<?> listIterator) {
                listIterator.previous();
            }
            void moveBackward(ListIterator<?> listIterator) {
                listIterator.next();
            }
        };

        final boolean exists;

        Accessed(boolean accessed) {
            this.exists = accessed;
        }

        abstract void moveForward(ListIterator<?> listIterator);
        abstract void moveBackward(ListIterator<?> listIterator);

        void moveBackAndForth(ListIterator<?> listIterator) {
            moveForward(listIterator);
            moveBackward(listIterator);
        }
    }

    public FilteringListIterator(ListIterator<E> listIterator, Filter<? super E> filter) {
        this(listIterator, filter, 0);
    }

    public FilteringListIterator(ListIterator<E> listIterator, Filter<? super E> filter, int index) {
        Args.notNull(listIterator);
        Args.notNull(filter);
        this.listIterator = listIterator;
        this.filter = filter;
        this.index = index;
    }

    public void add(E e) {
        moveBack();
        listIterator.add(e);
        index++;
        accessed = Accessed.NONE;
    }

    public boolean hasNext() {
        if (elementToReturn != null && offset > 0) {
            return true;
        }
        elementToReturn = null;
        while (offset < 0) {
            listIterator.next();
            offset++;
        }
        do {
            if (!listIterator.hasNext()) {
                return false;
            }
            E element = listIterator.next();
            offset++;
            if (filter.accept(element)) {
                elementToReturn = element;
                return true;
            }
        } while (true);
    }

    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        try {
            return elementToReturn;
        } finally {
            offset = 0;
            accessed = Accessed.PREVIOUS;
            elementToReturn = null;
            index++;
        }
    }

    public boolean hasPrevious() {
        if (elementToReturn != null && offset < 0) {
            return true;
        }
        elementToReturn = null;
        while (offset > 0) {
            listIterator.previous();
            offset--;
        }
        do {
            if (!listIterator.hasPrevious()) {
                return false;
            }
            E element = listIterator.previous();
            offset--;
            if (filter.accept(element)) {
                elementToReturn = element;
                return true;
            }
        } while (true);
    }

    public E previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        try {
            return elementToReturn;
        } finally {
            offset = 0;
            accessed = Accessed.NEXT;
            elementToReturn = null;
            index--;
        }
    }

    public int nextIndex() {
        return index;
    }

    public int previousIndex() {
        return index - 1;
    }

    public void remove() {
        moveBack();
        accessed.moveForward(listIterator);
        if (accessed == Accessed.PREVIOUS) {
            index--;
        }
        listIterator.remove();
        accessed = Accessed.NONE;
        elementToReturn = null;
    }

    public void set(E e) {
        moveBack();
        accessed.moveBackAndForth(listIterator);
        listIterator.set(e);
    }

    private void moveBack() {
        while (offset > 0) {
            listIterator.previous();
            offset--;
        }
        while (offset < 0) {
            listIterator.next();
            offset++;
        }
    }
}

class FastLinkedList<E> extends AbstractSequentialList<E> implements Serializable {
    private static final long serialVersionUID = -5460960741591122089L;

    
    private int size;

    
    private ListCell<E> head;

    
    private ListCell<E> tail;

    
    private boolean consumed;

    
    public FastLinkedList() { }

    
    public FastLinkedList(Iterable<? extends E> elements) {
        Args.notNull(elements);
        for (E e : elements) {
            addLast(e);
        }
    }

    
    public boolean isConsumed() {
        return consumed;
    }

    
    @Override
    public boolean add(E element) {
        addLast(element);
        return true;
    }

    private void markConsumed() {
        consumed = true;
        size = 0;
        head = tail = null;
    }

    
    public int size() {
        return size;
    }

    
    public E getFirst() {
        checkExisting(head);
        return head.getValue();
    }

    
    public E getLast() {
        checkExisting(tail);
        return tail.getValue();
    }

    private void checkExisting(ListCell<E> cell) {
        if (cell == null) {
            throw new NoSuchElementException();
        }
    }

    private AccessorImpl<E> checkOwnedAccessor(Accessor<E> accessor) {
        if (accessor == null) {
            throw new IllegalArgumentException("Null accessor");
        }
        AccessorImpl<E> accessorImpl = (AccessorImpl<E>)accessor;
        if (!accessorImpl.belongsTo(this)) {
            throw new IllegalArgumentException("This accessor does not belong to this list");
        }
        return accessorImpl;
    }

    
    public Accessor<E> addFirst(E value) {
        checkNotConsumed();
        size++;
        ListCell<E> cell = new ListCell<E>(value);
        cell.next = head;
        if (head != null) {
            head.prev = cell;
        }
        head = cell;
        if (tail == null) {
            tail = cell;
        }
        return new AccessorImpl<E>(cell, this);
    }

    
    public Accessor<E> addLast(E value) {
        checkNotConsumed();
        size++;
        ListCell<E> newCell = new ListCell<E>(value);
        newCell.prev = tail;
        if (tail != null) {
            tail.next = newCell;
        }
        tail = newCell;
        if (head == null) {
            head = newCell;
        }
        return new AccessorImpl<E>(newCell, this);
    }

    private Accessor<E> addAfter(AccessorImpl<E> previous, E value) {
        checkNotConsumed();
        size++;
        ListCell<E> previousCell = previous.cell;
        ListCell<E> nextCell = previousCell.next;
        ListCell<E> newCell = new ListCell<E>(value);

        previousCell.next = newCell;
        if (nextCell != null) {
            nextCell.prev = newCell;
        }
        newCell.next = nextCell;
        newCell.prev = previousCell;
        if (tail == previousCell) {
            tail = newCell;
        }
        return new AccessorImpl<E>(newCell, this);
    }

    private Accessor<E> addBefore(AccessorImpl<E> next, E value) {
        checkNotConsumed();
        size++;
        ListCell<E> nextCell = next.cell;
        ListCell<E> previousCell = nextCell.prev;
        ListCell<E> newCell = new ListCell<E>(value);

        nextCell.prev = newCell;
        if (previousCell != null) {
            previousCell.next = newCell;
        }
        newCell.prev = previousCell;
        newCell.next = nextCell;
        if (head == nextCell) {
            head = newCell;
        }
        return new AccessorImpl<E>(newCell, this);
    }

    private void checkNotConsumed() {
        if (consumed) {
            throwIllegalStateException();
        }
    }

    private static void throwIllegalStateException() {
        throw new IllegalStateException("This list has been appended to " +
                "another one, and thus has been marked as empty and unmodifiable, and should be thrown away");
    }

    
    public E removeFirst() {
        checkExisting(head);
        size--;
        if (head == tail) {
            tail = null;
            try {
                return head.getValue();
            } finally {
                head = null;
            }
        }
        try {
            return head.getValue();
        } finally {
            head.next.prev = null;
            head = head.next;
        }
    }

    
    public E removeLast() {
        checkExisting(tail);
        size--;
        if (head == tail) {
            head = null;
            try {
                return tail.getValue();
            } finally {
                tail = null;
            }
        }
        try {
            return tail.getValue();
        } finally {
            tail.prev.next = null;
            tail = tail.prev;
        }
    }

    
    public void append(FastLinkedList<E> list) {
        checkNotConsumed();
        if (list.isEmpty()) {
            return;
        }
        if (isEmpty()) {
            head = list.head;
            tail = list.tail;
        } else {
            tail.next = list.head;
            list.head.prev = tail;
            tail = list.tail;
        }
        size += list.size();
        list.markConsumed();
    }

    
    public boolean ownsAccessor(Accessor<E> accessor) {
        if ((accessor == null) || (!(accessor instanceof AccessorImpl))) {
            return false;
        }
        AccessorImpl<E> ref = (AccessorImpl<E>)accessor;
        return ref.belongsTo(this);
    }

    private void removeCell(ListCell<E> cell) {
        if (cell.prev != null) {
            cell.prev.next = cell.next;
        }
        if (cell.next != null) {
            cell.next.prev = cell.prev;
        }
        if (head == cell) {
            head = cell.next;
        }
        if (tail == cell) {
            tail = cell.prev;
        }
        cell.markDeleted();
        size--;
    }

    
    public Accessor<E> accessorFor(E element) {
        ListCell<E> curr = head;
        while (curr != null) {
            if (element == null) {
                if (curr.getValue() == null) {
                    return new AccessorImpl<E>(curr, this);
                }
            } else {
                if (element.equals(curr.getValue())) {
                    return new AccessorImpl<E>(curr, this);
                }
            }
            curr = curr.next;
        }
        return null;
    }

    
    public ListIterator<E> listIterator(int index) {
        if (isConsumed()) {
            return new ConsumedIterator<E>();
        }
        return new ListIteratorImpl(index);
    }

    private static class ConsumedIterator<E> implements ListIterator<E> {
        public void add(E e) {
            throwIllegalStateException();
        }

        public boolean hasNext() {
            return false;
        }

        public boolean hasPrevious() {
            return false;
        }

        public E next() {
            throw new NoSuchElementException();
        }

        public int nextIndex() {
            return 0;
        }

        public E previous() {
            throw new NoSuchElementException();
        }

        public int previousIndex() {
            return -1;
        }

        public void remove() {
            throw new NoSuchElementException();
        }

        public void set(E e) {
            throw new NoSuchElementException();
        }
    }

    private class ListIteratorImpl implements ListIterator<E> {
        private ListCell<E> next;
        private ListCell<E> prev;
        private ListCell<E> last;
        private int index;

        ListIteratorImpl(int offset) {
            if (offset < 0 || offset > size) {
                throw new NoSuchElementException();
            }
            index = offset;
            if (offset <= size / 2) {
                next = head;
                while (offset > 0) {
                    prev = next;
                    next = next.next;
                    offset--;
                }
            } else {
                prev = tail;
                offset = size - offset;
                while (offset > 0) {
                    next = prev;
                    prev = prev.prev;
                    offset--;
                }
            }
        }

        ListIteratorImpl(Accessor<E> ref) {
            AccessorImpl<E> reference = checkOwnedAccessor(ref);
            this.last = null;
            this.prev = reference.cell;
            this.next = prev.next;
        }

        public void add(E obj) {

            last = null;
            index++;
            size++;
            ListCell<E> newCell = new ListCell<E>(obj);
            newCell.next = next;
            newCell.prev = prev;
            if (prev == null) {
                head = newCell;
            } else {
                prev.next = newCell;
            }
            prev = newCell;
            if (next == null) {
                tail = newCell;
            } else {
                next.prev = newCell;
            }
        }

        private void proceedForward() {
            while (next != null && next.isDeleted()) {
                prev = next;
                next = next.next;
            }
        }

        private void proceedBackward() {
            while (prev != null && prev.isDeleted()) {
                next = prev;
                prev = prev.prev;
            }
        }

        public boolean hasNext() {
            proceedForward();
            return next != null;
        }

        public boolean hasPrevious() {
            proceedBackward();
            return prev != null;
        }

        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            try {
                return next.getValue();
            } finally {
                index++;
                last = next;
                prev = next;
                next = next.next;
            }
        }

        public int nextIndex() {
            return index;
        }

        public E previous() {
            if (!hasPrevious()) {
                throw new NoSuchElementException();
            }
            try {
                return prev.getValue();
            } finally {
                index--;
                last = prev;
                next = prev;
                prev = prev.prev;
            }
        }

        public int previousIndex() {
            return index - 1;
        }

        public void remove() {
            if (last == null || last.isDeleted()) {
                throw new IllegalStateException();
            }
            index--;
            size--;
            last.markDeleted();
            prev = last.prev;
            next = last.next;
            if (prev != null) {
                prev.next = last.next;
            } else {
                head = next;
            }
            if (next != null) {
                next.prev = last.prev;
            } else {
                tail = prev;
            }
            last = null;
        }

        public void set(E element) {
            if (last == null) {
                throw new IllegalStateException();
            }
            last.setValue(element);
        }
    }

    private static class AccessorImpl<E> implements Accessor<E>, Serializable {
        ListCell<E> cell;
        FastLinkedList<E> owner;

        AccessorImpl(ListCell<E> cell, FastLinkedList<E> owner) {
            this.cell = cell;
            this.owner = owner;
        }

        boolean belongsTo(FastLinkedList seq) {
            return owner == seq;
        }

        private void checkOwner() {
            if (owner == null) {
                throw new NoSuchElementException("this element has been removed");
            }
        }

        void invalidate() {
            owner = null;
        }

        @Override
        public String toString() {
            return cell.toString() + "," + (owner != null ? "valid" : "invalid");
        }

        public boolean remove() {
            if (cell.isDeleted()) {
                return false;
            }
            owner.removeCell(cell);
            invalidate();
            return true;
        }

        public E get() {
            checkOwner();
            return cell.getValue();
        }

        public FastLinkedList<E> owner() {
            return owner;
        }

        public boolean isRemoved() {
            return cell.isDeleted();
        }

        public E set(E newValue) {
            return cell.setValue(newValue);
        }

        public Accessor<E> addAfter(E value) {
            checkOwner();
            return owner.addAfter(this, value);
        }

        public Accessor<E> addBefore(E value) {
            checkOwner();
            return owner.addBefore(this, value);
        }

        public void moveAfter(Accessor<E> afterWhat) {
            checkOwner();
            AccessorImpl<E> r = owner.checkOwnedAccessor(afterWhat);
            if (r.cell == cell) {
                return;
            }
            E value = cell.getValue();
            FastLinkedList<E> owner = this.owner;
            this.remove();
            update(r.addAfter(value), owner);
        }

        public void moveBefore(Accessor<E> beforeWhat) {
            checkOwner();
            AccessorImpl<E> r = owner.checkOwnedAccessor(beforeWhat);
            if (r.cell == cell) {
                return;
            }
            E value = cell.getValue();
            FastLinkedList<E> owner = this.owner;
            this.remove();
            update(r.addBefore(value), owner);
        }

        public void moveToBack() {
            checkOwner();
            E value = cell.getValue();
            FastLinkedList<E> owner = this.owner;
            this.remove();
            update(owner.addLast(value), owner);
        }

        public void moveToFront() {
            checkOwner();
            E value = cell.getValue();
            FastLinkedList<E> owner = this.owner;
            this.remove();
            update(owner.addFirst(value), owner);
        }

        public Accessor<E> next() {
            checkOwner();
            if (cell.next == null) {
                throw new NoSuchElementException();
            }
            return new AccessorImpl<E>(cell.next, owner);
        }

        public Accessor<E> previous() {
            checkOwner();
            if (cell.prev == null) {
                throw new NoSuchElementException();
            }
            return new AccessorImpl<E>(cell.prev, owner);
        }

        public ListIterator<E> listIterator() {
            checkOwner();
            return owner.new ListIteratorImpl(this);
        }

        private void update(Accessor<E> valid, FastLinkedList<E> owner) {
            update((AccessorImpl<E>)valid, owner);
        }

        private void update(AccessorImpl<E> copyFrom, FastLinkedList<E> owner) {
            this.owner = owner;
            this.cell = copyFrom.cell;
        }
    }

    private static class ListCell<E> implements Serializable {
        enum Sentinel {
            TOMBSTONE;
        }

        ListCell<E> prev;
        ListCell<E> next;
        private E value;

        ListCell(E value) {
            this.value = value;
        }

        boolean isDeleted() {
            return value == Sentinel.TOMBSTONE;
        }

        @SuppressWarnings("unchecked")

        void markDeleted() {
            value = (E)(Object)Sentinel.TOMBSTONE;
        }

        E getValue() {
            return value;
        }

        E setValue(E value) {
            try {
                return this.value;
            } finally {
                this.value = value;
            }
        }

        public String toString() {
            return (prev == null ? "null" : prev.value.toString())
                    + "<--"
                    + (value == null ? "null" : value.toString())
                    + "-->"
                    + (next == null ? "null" : next.value.toString());
        }
    }
}

interface Accessor<E> {
    
    boolean remove();

    
    boolean isRemoved();

    
    E set(E newElement);

    
    E get();

    
    Accessor<E> addAfter(E element);

    
    Accessor<E> addBefore(E element);

    
    Accessor<E> next();

    
    Accessor<E> previous();

    
    ListIterator<E> listIterator();

    
    void moveToFront();

    
    void moveToBack();

    
    void moveAfter(Accessor<E> accessor);

    
    void moveBefore(Accessor<E> accessor);

    
    FastLinkedList<E> owner();
}

abstract class AbstractListGraph extends AbstractGraph {
    final FastLinkedList<NodeImpl> nodes = new FastLinkedList<NodeImpl>();

    private SoftReference<List<Node>> nodesCache;
    private SoftReference<List<Edge>> edgesCache;

    abstract void setNodeRef(NodeImpl node, Accessor<NodeImpl> ref);
    abstract void setEdgeOutRef(EdgeImpl edge, Accessor<EdgeImpl> ref);
    abstract void setEdgeInRef(EdgeImpl edge, Accessor<EdgeImpl> ref);
    abstract Accessor<NodeImpl> getNodeRef(NodeImpl node);
    abstract Accessor<EdgeImpl> getEdgeOutRef(EdgeImpl edge);
    abstract Accessor<EdgeImpl> getEdgeInRef(EdgeImpl edge);
    abstract void removeNodeRef(NodeImpl node);
    abstract void removeEdgeRefs(EdgeImpl edge);

    abstract FastLinkedList<EdgeImpl> getOutEdges(NodeImpl node);
    abstract FastLinkedList<EdgeImpl> getInEdges(NodeImpl node);

    abstract void initNode(NodeImpl node);



    public Node newNode(Object value) {
        final NodeImpl node = new NodeImpl(value);
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.NODE_ADDED, node) , new Runnable() {
            public void run() {
                initNode(node);
                setNodeRef(node, nodes.addLast(node));
                nodesCache = null;
            }
        });
        return node;
    }

    public Edge newEdge(Node n1, Node n2, final Object value) {
        Args.notNull(n1, n2);
        final NodeImpl node1 = checkContainedAndCast(n1);
        final NodeImpl node2 = checkContainedAndCast(n2);
        final EdgeImpl edge = new EdgeImpl(node1, node2, value);
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.EDGE_ADDED, edge), new Runnable() {
            public void run() {
                edgeCount++;
                Accessor<EdgeImpl> outRef = getOutEdges(node1).addLast(edge);
                Accessor<EdgeImpl> inRef = getInEdges(node2).addLast(edge);
                setEdgeOutRef(edge, outRef);
                setEdgeInRef(edge, inRef);
                edgesCache = null;
            }
        });
        return edge;
    }

    public int nodeCount() {
        return nodes.size();
    }

    public int edgeCount() {
        return edgeCount;
    }

    private NodeImpl checkContainedAndCast(Node node) {
        if (!containsNode(node)) {
            throw new IllegalArgumentException("Node " + node + " not contained in graph");
        }
        return (NodeImpl)node;
    }

    private EdgeImpl checkContainedAndCast(Edge edge) {
        if (!containsEdge(edge)) {
            throw new IllegalArgumentException("Edge " + edge + " not contained in graph");
        }
        return (EdgeImpl)edge;
    }

    final ExtendedListIterable<Node> iterableNodes(final FastLinkedList<NodeImpl> seq, int expectedSize) {
        return new ExtendedListIterable<Node>(expectedSize) {
            protected ListIterator<Node> listIteratorImpl() {
                return new ListIterator<Node>() {
                    final ListIterator<Node> iter = castNodes(seq).listIterator(0);
                    Node last;
                    int index = 0;

                    public boolean hasNext() {
                        return iter.hasNext();
                    }

                    public Node next() {
                        last = iter.next();
                        index++;
                        return last;
                    }

                    public void remove() {
                        if (last == null) {
                            throw new IllegalStateException();
                        }
                        removeNode(last);
                        last = null;
                    }

                    public void set(Node node) {
                        throw new UnsupportedOperationException();
                    }

                    public void add(Node node) {
                        throw new UnsupportedOperationException();
                    }

                    public Node previous() {
                        Node n = iter.previous();
                        index--;
                        return n;
                    }

                    public boolean hasPrevious() {
                        return iter.hasPrevious();
                    }

                    public int nextIndex() {
                        return index;
                    }

                    public int previousIndex() {
                        return index - 1;
                    }
                };
            }
        };
    }



    @SuppressWarnings("unchecked")
    private static List<Node> castNodes(List<NodeImpl> nodes) {
        List list = nodes;
        return (List<Node>)list;
    }



    @SuppressWarnings("unchecked")
    private static List<Edge> castEdges(List<EdgeImpl> edges) {
        List list = edges;
        return (List<Edge>)list;
    }

    public ExtendedListIterable<Node> nodes() {
        if (containsHint(Hint.FAST_NODE_ITERATION)) {
            List<Node> nodesList = null;
            if (nodesCache != null) {
                nodesList = nodesCache.get();
            }
            if (nodesList == null) {
                nodesList = new LinkedList<Node>();
                try {
                    for (Node n : nodes) {
                        nodesList.add(n);
                    }
                } catch (OutOfMemoryError e) {
                    return iterableNodes(nodes, nodeCount());
                }
                nodesCache = new SoftReference<List<Node>>(nodesList);
            }
            final List<Node> finalNodesList = nodesCache.get();
            if (finalNodesList != null) {
                return new ExtendedListIterable<Node>(nodes.size()) {
                    protected ListIterator<Node> listIteratorImpl() {
                        return new DelegateListIterator<Node>(finalNodesList.listIterator()) {
                            public void remove() {
                                removeNode(last);
                                last = null;
                            }
                        };
                    }
                };
            }
        }

        return iterableNodes(nodes, nodeCount());
    }

    public ExtendedListIterable<Node> adjacentNodes(final Node n, final Direction direction) {
        Args.notNull(n, direction);
        return new ExtendedListIterable<Node>(degree(n, direction)) {
            protected ListIterator<Node> listIteratorImpl() {
                return new ListIterator<Node>() {
                    final ListIterator<Edge> incidentEdges = edges(n, direction).listIterator();
                    Node last;
                    public boolean hasNext() {
                        return incidentEdges.hasNext();
                    }

                    public Node next() {
                        return last = incidentEdges.next().opposite(n);
                    }

                    public boolean hasPrevious() {
                        return incidentEdges.hasPrevious();
                    }

                    public Node previous() {
                        return last = incidentEdges.previous().opposite(n);
                    }

                    public void remove() {
                        if (last == null) {
                            throw new IllegalStateException();
                        }
                        removeNode(last);
                        last = null;
                    }

                    public void set(Node n) {
                        throw new UnsupportedOperationException();
                    }

                    public void add(Node n) {
                        throw new UnsupportedOperationException();
                    }

                    public int previousIndex() {
                        return incidentEdges.previousIndex();
                    }

                    public int nextIndex() {
                        return incidentEdges.nextIndex();
                    }
                };
            }
        };
    }

    public boolean removeEdge(Edge edge) {
        if (!containsEdge(edge)) {
            return false;
        }
        final EdgeImpl e = (EdgeImpl)edge;
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.EDGE_REMOVED, edge), new Runnable() {
            public void run() {
                NodeImpl n1 = (NodeImpl)e.n1();
                NodeImpl n2 = (NodeImpl)e.n2();
                getEdgeOutRef(e).remove();
                getEdgeInRef(e).remove();
                removeEdgeRefs(e);
                edgeCount--;
                edgesCache = null;
            }
        });
        return true;
    }

    public boolean removeNode(final Node node) {
        if (!containsNode(node)) {
            return false;
        }
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.NODE_REMOVED, node), new Runnable() {
            public void run() {
                NodeImpl n = (NodeImpl)node;
                for (Edge e : edges(node)) {
                    removeEdge(e);
                }
                getNodeRef(n).remove();
                removeNodeRef(n);
                edgesCache = null;
                nodesCache = null;
            }
        });
        return true;
    }

    public ExtendedListIterable<Edge> edges(Node node, final Direction direction) {
        Args.notNull(direction);
        final NodeImpl n = checkContainedAndCast(node);
        return new ExtendedListIterable<Edge>(degree(node, direction)) {
            protected ListIterator<Edge> listIteratorImpl() {
                return new CompoundListIterator<Edge>(
                        direction.isOut() ? castEdges(getOutEdges(n)).listIterator() : null,
                        direction.isIn() ? castEdges(getInEdges(n)).listIterator() : null
                );
            }
        };
    }


    public ExtendedListIterable<Edge> edges(final Node n1, final Node n2, final Direction direction) {
        Args.notNull(direction);
        final NodeImpl node1 = checkContainedAndCast(n1);
        final NodeImpl node2 = checkContainedAndCast(n2);
        Direction flip = direction.flip();
        if (degree(n1, direction) > (degree(n2, flip)) && n1 != n2) {
            return edges(n2, n1, flip);
        }
        final ListIterator<Edge> outIterator = (!direction.isOut() ? null :
                (outDegree(n1) < inDegree(n2) ?
                        castEdges(getOutEdges(node1)).listIterator() :
                        castEdges(getInEdges(node2)).listIterator()));
        final ListIterator<Edge> inIterator = (!direction.isIn() ? null :
                (inDegree(n1) < outDegree(n2) ?
                        castEdges(getInEdges(node1)).listIterator() :
                        castEdges(getOutEdges(node2)).listIterator()));
        return new ExtendedListIterable<Edge>() {
            protected ListIterator<Edge> listIteratorImpl() {
                return new FilteringListIterator<Edge>(new CompoundListIterator<Edge>(outIterator, inIterator), new Filter<Edge>() {
                    public boolean accept(Edge e) {
                        return e.isIncident(n1) && e.opposite(n1) == n2;
                    }
                });
            }
        };
    }

    public boolean containsEdge(Edge edge) {
        if (edge == null) {
            return false;
        }
        try {
            EdgeImpl e = (EdgeImpl)edge;
            NodeImpl n1 = e.n1;
            return containsNode(n1) && getOutEdges(n1).ownsAccessor(getEdgeOutRef(e));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public boolean containsNode(Node node) {
        if (node == null) {
            return false;
        }
        try {
            NodeImpl n = (NodeImpl)node;
            return nodes.ownsAccessor(getNodeRef(n));
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public int inDegree(Node node) {
        NodeImpl n = checkContainedAndCast(node);
        return getInEdges(n).size();
    }

    public int outDegree(Node node) {
        NodeImpl n = checkContainedAndCast(node);
        return getOutEdges(n).size();
    }

    public int degree(Node node) {
        NodeImpl n = checkContainedAndCast(node);
        return getOutEdges(n).size() + getInEdges(n).size();
    }

    public ExtendedListIterable<Edge> edges() {
        if (containsHint(Hint.FAST_EDGE_ITERATION)) {
            List<Edge> edgesList = null;
            if (edgesCache != null) {
                edgesList = edgesCache.get();
            }
            if (edgesList == null) {
                edgesList = new LinkedList<Edge>();
                try {
                    int pos = 0;
                    for (Edge e : edgesImpl()) {
                        edgesList.add(e);
                    }
                } catch (OutOfMemoryError e) {
                    return edgesImpl();
                }
                edgesCache = new SoftReference<List<Edge>>(edgesList);
            }

            final List<Edge> edgeList = edgesCache.get();
            if (edgeList != null) {
                return new ExtendedListIterable<Edge>(edgeList.size()) {
                    protected ListIterator<Edge> listIteratorImpl() {
                        return new DelegateListIterator<Edge>(edgeList.listIterator()) {
                            public void remove() {
                                super.remove();
                                removeEdge(last);
                                last = null;
                            }
                        };
                    }
                };
            }
        }

        return edgesImpl();
    }

    private ExtendedListIterable<Edge> edgesImpl() {
        return new ExtendedListIterable<Edge>(edgeCount()) {
            protected ListIterator<Edge> listIteratorImpl() {
                return new AbstractCompoundListIterator<Edge>() {
                    private Direction direction = null;

                    final ListIterator<Node> nodeCursor = nodes().listIterator();

                    protected boolean hasNextIterator() {
                        if (direction == Direction.IN) {
                            nodeCursor.next();
                            direction = Direction.OUT;
                        }
                        return nodeCursor.hasNext();
                    }

                    protected boolean hasPreviousIterator() {
                        if (direction == Direction.OUT) {
                            nodeCursor.previous();
                            direction = Direction.IN;
                        }
                        return nodeCursor.hasPrevious();
                    }

                    protected ListIterator<Edge> nextIterator() {
                        direction = Direction.OUT;
                        return getEdgesOfNode(nodeCursor.next(), true);
                    }

                    protected ListIterator<Edge> previousIterator() {
                        direction = Direction.IN;
                        return getEdgesOfNode(nodeCursor.previous(), false);
                    }

                    private ListIterator<Edge> getEdgesOfNode(Node node, boolean start) {
                        NodeImpl n = (NodeImpl)node;
                        int index = start ? 0 : outDegree(n);
                        return castEdges(getOutEdges(n)).listIterator(index);
                    }

                    @Override public void remove() {
                        removeEdge(last);
                        last = null;
                    }

                    @Override public void add(Edge e) {
                        throw new UnsupportedOperationException();
                    }

                    @Override public void set(Edge e) {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    public OrderManager getOrderManager() {
        return new OrderManagerImpl();
    }

    private static abstract class DelegateListIterator<E> implements ListIterator<E> {
        final ListIterator<E> delegate;

        DelegateListIterator(ListIterator<E> delegate) {
            this.delegate = delegate;
        }

        protected E last = null;

        public void set(E o) {
            throw new UnsupportedOperationException();
        }

        public void add(E o) {
            throw new UnsupportedOperationException();
        }

        public void remove() {
            if (last == null) {
                throw new IllegalStateException();
            }
        }

        public int previousIndex() {
            return delegate.previousIndex();
        }

        public E previous() {
            return last = delegate.previous();
        }

        public int nextIndex() {
            return delegate.nextIndex();
        }

        public E next() {
            return last = delegate.next();
        }

        public boolean hasPrevious() {
            return delegate.hasPrevious();
        }

        public boolean hasNext() {
            return delegate.hasNext();
        }
    }

    private class OrderManagerImpl implements OrderManager {
        public void moveNodeToFront(Node node) {
            NodeImpl n = checkContainedAndCast(node);
            getNodeRef(n).moveToFront();
            nodesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.NODE_REORDERED, node));
        }

        public void moveNodeToBack(Node node) {
            NodeImpl n = checkContainedAndCast(node);
            getNodeRef(n).moveToBack();
            nodesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.NODE_REORDERED, node));
        }

        public void moveNodeBefore(Node node, Node beforeWhat) {
            NodeImpl n = checkContainedAndCast(node);
            NodeImpl before = checkContainedAndCast(beforeWhat);
            getNodeRef(n).moveBefore(getNodeRef(before));
            nodesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.NODE_REORDERED, node));
        }

        public void moveNodeAfter(Node node, Node afterWhat) {
            NodeImpl n = checkContainedAndCast(node);
            NodeImpl after = checkContainedAndCast(afterWhat);
            getNodeRef(n).moveAfter(getNodeRef(after));
            nodesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.NODE_REORDERED, node));
        }

        public void moveEdgeToFront(Edge edge, boolean onSourceNode) {
            EdgeImpl e = checkContainedAndCast(edge);
            NodeImpl n;
            Accessor<EdgeImpl> ref;
            FastLinkedList<EdgeImpl> seq;
            if (onSourceNode) {
                n = e.n1();
                seq = getOutEdges(n);
                ref = getEdgeOutRef(e);
            } else {
                n = e.n2();
                seq = getInEdges(n);
                ref = getEdgeInRef(e);
            }
            ref.moveToFront();
            edgesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.EDGE_REORDERED, edge));
        }

        public void moveEdgeToBack(Edge edge, boolean onSourceNode) {
            EdgeImpl e = checkContainedAndCast(edge);
            NodeImpl n;
            Accessor<EdgeImpl> ref;
            FastLinkedList<EdgeImpl> seq;
            if (onSourceNode) {
                n = e.n1();
                seq = getOutEdges(n);
                ref = getEdgeOutRef(e);
            } else {
                n = e.n2();
                seq = getInEdges(n);
                ref = getEdgeInRef(e);
            }
            ref.moveToBack();
            edgesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.EDGE_REORDERED, edge));
        }

        public void moveEdgeBefore(Edge edge, boolean onSourceNode, Edge beforeWhat) {
            EdgeImpl e = checkContainedAndCast(edge);
            EdgeImpl before = checkContainedAndCast(beforeWhat);
            NodeImpl n;
            Accessor<EdgeImpl> ref;
            Accessor<EdgeImpl> ref2;
            FastLinkedList<EdgeImpl> seq;
            if (onSourceNode) {
                n = e.n1();
                seq = getOutEdges(n);
                ref = getEdgeOutRef(e);
                ref2 = getEdgeOutRef(before);
            } else {
                n = e.n2();
                seq = getInEdges(n);
                ref = getEdgeInRef(e);
                ref2 = getEdgeInRef(before);
            }
            ref.moveBefore(ref2);
            edgesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.EDGE_REORDERED, edge));
        }

        public void moveEdgeAfter(Edge edge, boolean onSourceNode, Edge afterWhat) {
            EdgeImpl e = checkContainedAndCast(edge);
            EdgeImpl after = checkContainedAndCast(afterWhat);
            NodeImpl n;
            Accessor<EdgeImpl> ref;
            Accessor<EdgeImpl> ref2;
            FastLinkedList<EdgeImpl> seq;
            if (onSourceNode) {
                n = e.n1();
                seq = getOutEdges(n);
                ref = getEdgeOutRef(e);
                ref2 = getEdgeOutRef(after);
            } else {
                n = e.n2();
                seq = getInEdges(n);
                ref = getEdgeInRef(e);
                ref2 = getEdgeInRef(after);
            }
            ref.moveAfter(ref2);
            edgesCache = null;
            graphEventSupport.fire(new GraphEvent(AbstractListGraph.this, GraphEvent.Type.EDGE_REORDERED, edge));
        }
    }
}

class GraphEvent extends EventObject {
    public static enum Type {
        NODE_ADDED(Node.class),
        NODE_REMOVED(Node.class),
        EDGE_ADDED(Edge.class),
        EDGE_REMOVED(Edge.class),

        NODE_REINSERTED(Node.class),
        EDGE_REINSERTED(Edge.class),

        NODE_REORDERED(Node.class),
        EDGE_REORDERED(Edge.class)
        ;

        private final Class clazz;

        private Type(Class expectedDataClass) {
            this.clazz = expectedDataClass;
        }

        private void checkAssignable(Object data) {
            clazz.cast(data);
        }
    };

    private final InspectableGraph source;
    private final Type eventType;
    private final Object data;

    public GraphEvent(InspectableGraph source, Type type, Object data) {
        super(source);
        this.source = source;
        this.eventType = type;
        this.data = data;
        type.checkAssignable(data);
    }

    @Override public InspectableGraph getSource() {
        return source;
    }

    public Type getEventType() {
        return eventType;
    }

    public Object getData() {
        return data;
    }

    public Node getNode() {
        return (Node)data;
    }

    public Edge getEdge() {
        return (Edge)data;
    }

    public int hashCode() {
        return 17 + source.hashCode() + eventType.hashCode() * 11 + data.hashCode() * 37;
    }

    public boolean equals(Object o) {
        if (!(o instanceof GraphEvent)) {
            return false;
        }
        GraphEvent other = (GraphEvent)o;
        return source == other.source &&
                eventType == other.eventType &&
                data == other.data;
    }

    public String toString() {
        return "[Event: source=" + source + ", type=" + eventType + ", data=" + data + "]";
    }
}

class GraphEventSupport {
    private static final Runnable NULL_RUNNABLE = new Runnable() { public void run() { } };
    private final EventSupport<NodeListener> nodeSupport = new EventSupport<NodeListener>();
    private final EventSupport<EdgeListener> edgeSupport = new EventSupport<EdgeListener>();
    private int listeners;

    private void calcListeners() {
        listeners = nodeSupport.getListenerCount() + edgeSupport.getListenerCount();
    }

    public boolean isEmpty() {
        return listeners == 0;
    }

    public void addEdgeListener(EdgeListener listener) {
        edgeSupport.addListener(listener);
        calcListeners();
    }

    public void addNodeListener(NodeListener listener) {
        nodeSupport.addListener(listener);
        calcListeners();
    }

    public void removeEdgeListener(EdgeListener listener) {
        edgeSupport.removeListener(listener);
        calcListeners();
    }

    public void removeNodeListener(NodeListener listener) {
        nodeSupport.removeListener(listener);
        calcListeners();
    }

    public void addGraphListener(GraphListener listener) {
        addEdgeListener(listener);
        addNodeListener(listener);
    }

    public void removeGraphListener(GraphListener listener) {
        if (listener == null) {
            return;
        }
        removeEdgeListener(listener);
        removeNodeListener(listener);
    }

    public void fire(GraphEvent e) {
        fire(e, e.getEventType(), NULL_RUNNABLE);
    }


    public void fire(GraphEvent e, Runnable commandIfNoVeto) {
        fire(e, e.getEventType(), commandIfNoVeto);
    }


    public void fire(GraphEvent e, GraphEvent.Type eventType, Runnable commandIfNoVeto) {
        if (listeners == 0) {
            commandIfNoVeto.run();
            return;
        }
        switch (eventType) {
            case NODE_ADDED: case NODE_REINSERTED:
                fireAddNode(e, commandIfNoVeto); break;
            case NODE_REMOVED:
                fireRemoveNode(e, commandIfNoVeto); break;
            case EDGE_ADDED: case EDGE_REINSERTED:
                fireAddEdge(e, commandIfNoVeto); break;
            case EDGE_REMOVED:
                fireRemoveEdge(e, commandIfNoVeto); break;
            case NODE_REORDERED:
                fireNodeReordered(e); break;
            case EDGE_REORDERED:
                fireEdgeReordered(e); break;
            default:
                throw new IllegalArgumentException("Unexpected event type: " + eventType);
        }
    }

    public void firePreEdge() {
        firePre(edgeSupport.getListeners());
    }

    public void firePostEdge() {
        firePost(edgeSupport.getListeners());
    }

    public void firePreNode() {
        firePre(nodeSupport.getListeners());
    }

    public void firePostNode() {
        firePost(nodeSupport.getListeners());
    }

    public void fireNodeReordered(GraphEvent e) {
        Args.isTrue(e.getEventType() == GraphEvent.Type.NODE_REORDERED);
        for (NodeListener listener : nodeSupport.getListeners()) {
            listener.nodeReordered(e);
        }
    }

    public void fireEdgeReordered(GraphEvent e) {
        Args.isTrue(e.getEventType() == GraphEvent.Type.EDGE_REORDERED);
        for (EdgeListener listener : edgeSupport.getListeners()) {
            listener.edgeReordered(e);
        }
    }

    public void fireNodeToBeAdded(GraphEvent e) {
        GraphEvent.Type eventType = e.getEventType();
        Args.isTrue(eventType == GraphEvent.Type.NODE_ADDED || eventType == GraphEvent.Type.NODE_REINSERTED);
        if (nodeSupport.isEmpty()) {
            return;
        }
        for (NodeListener listener : nodeSupport.getListeners()) {
            listener.nodeToBeAdded(e);
        }
    }

    private void fireAddNode(GraphEvent e, Runnable commandIfNoVeto) {
        firePreNode();
        try {
            Iterable<NodeListener> listeners = nodeSupport.getListeners();
            for (NodeListener listener : listeners) {
                listener.nodeToBeAdded(e);
            }
            commandIfNoVeto.run();
            for (NodeListener listener : listeners) {
                listener.nodeAdded(e);
            }
        } finally {
            firePostNode();
        }
    }

    public void fireNodeAdded(GraphEvent e) {
        GraphEvent.Type eventType = e.getEventType();
        Args.isTrue(eventType == GraphEvent.Type.NODE_ADDED || eventType == GraphEvent.Type.NODE_REINSERTED);
        if (nodeSupport.isEmpty()) {
            return;
        }
        for (NodeListener listener : nodeSupport.getListeners()) {
            listener.nodeAdded(e);
        }
    }

    public void fireNodeToBeRemoved(GraphEvent e) {
        Args.isTrue(e.getEventType() == GraphEvent.Type.NODE_REMOVED);
        if (nodeSupport.isEmpty()) {
            return;
        }
        for (NodeListener listener : nodeSupport.getListeners()) {
            listener.nodeToBeRemoved(e);
        }
    }

    private void fireRemoveNode(GraphEvent e, Runnable commandIfNoVeto) {
        firePreNode();
        try {
            Iterable<NodeListener> listeners = nodeSupport.getListeners();
            for (NodeListener listener : listeners) {
                listener.nodeToBeRemoved(e);
            }
            commandIfNoVeto.run();
            for (NodeListener listener : listeners) {
                listener.nodeRemoved(e);
            }
        } finally {
            firePostNode();
        }
    }

    public void fireNodeRemoved(GraphEvent e) {
        Args.isTrue(e.getEventType() == GraphEvent.Type.NODE_REMOVED);
        if (nodeSupport.isEmpty()) {
            return;
        }
        for (NodeListener listener : nodeSupport.getListeners()) {
            listener.nodeRemoved(e);
        }
    }

    public void fireEdgeToBeAdded(GraphEvent e) {
        GraphEvent.Type eventType = e.getEventType();
        Args.isTrue(eventType == GraphEvent.Type.EDGE_ADDED || eventType == GraphEvent.Type.EDGE_REINSERTED);
        if (edgeSupport.isEmpty()) {
            return;
        }
        for (EdgeListener listener : edgeSupport.getListeners()) {
            listener.edgeToBeAdded(e);
        }
    }

    private void fireAddEdge(GraphEvent e, Runnable commandIfNoVeto) {
        firePreEdge();
        try {
            Iterable<EdgeListener> listeners = edgeSupport.getListeners();
            for (EdgeListener listener : listeners) {
                listener.edgeToBeAdded(e);
            }
            commandIfNoVeto.run();
            for (EdgeListener listener : listeners) {
                listener.edgeAdded(e);
            }
        } finally {
            firePostEdge();
        }
    }

    public void fireEdgeAdded(GraphEvent e) {
        GraphEvent.Type eventType = e.getEventType();
        Args.isTrue(eventType == GraphEvent.Type.EDGE_ADDED || eventType == GraphEvent.Type.EDGE_REINSERTED);
        if (edgeSupport.isEmpty()) {
            return;
        }
        for (EdgeListener listener : edgeSupport.getListeners()) {
            listener.edgeAdded(e);
        }
    }

    public void fireEdgeToBeRemoved(GraphEvent e) {
        Args.isTrue(e.getEventType() == GraphEvent.Type.EDGE_REMOVED);
        if (edgeSupport.isEmpty()) {
            return;
        }
        for (EdgeListener listener : edgeSupport.getListeners()) {
            listener.edgeToBeRemoved(e);
        }
    }

    private void fireRemoveEdge(GraphEvent e, Runnable commandIfNoVeto) {
        firePreEdge();
        try {
            Iterable<EdgeListener> listeners = edgeSupport.getListeners();
            for (EdgeListener listener : listeners) {
                listener.edgeToBeRemoved(e);
            }
            commandIfNoVeto.run();
            for (EdgeListener listener : listeners) {
                listener.edgeRemoved(e);
            }
        } finally {
            firePostEdge();
        }
    }

    public void fireEdgeRemoved(GraphEvent e) {
        Args.isTrue(e.getEventType() == GraphEvent.Type.EDGE_REMOVED);
        if (edgeSupport.isEmpty()) {
            return;
        }
        for (EdgeListener listener : edgeSupport.getListeners()) {
            listener.edgeRemoved(e);
        }
    }

    private void firePre(Collection<? extends OperationListener> listeners) {
        for (OperationListener listener : listeners) {
            listener.preEvent();
        }
    }

    private void firePost(Collection<? extends OperationListener> listeners) {
        for (OperationListener listener : listeners) {
            listener.postEvent();
        }
    }

    public List<NodeListener> getNodeListeners() {
        return nodeSupport.getListeners();
    }

    public List<EdgeListener> getEdgeListeners() {
        return edgeSupport.getListeners();
    }
}

interface GraphListener extends NodeListener, EdgeListener {
}

interface OperationListener extends EventListener {
    
    void preEvent();

    
    void postEvent();
}

interface NodeListener extends OperationListener {
    void nodeAdded(GraphEvent e);
    void nodeRemoved(GraphEvent e);
    void nodeToBeAdded(GraphEvent e);
    void nodeToBeRemoved(GraphEvent e) ;
    void nodeReordered(GraphEvent e);
}

interface EdgeListener extends OperationListener {
    void edgeAdded(GraphEvent e);

    void edgeRemoved(GraphEvent e);

    
    void edgeToBeAdded(GraphEvent e);

    
    void edgeToBeRemoved(GraphEvent e);

    void edgeReordered(GraphEvent e);
}

class EventSupport<L> {
    private List<L> listeners = null;

    public List<L> getListeners() {
        return listeners != null ? Collections.unmodifiableList(listeners) : Collections.<L>emptyList();
    }

    private void lazyInit() {
        if (listeners == null) {
            listeners = new InverseArrayList<L>();
        }
    }

    public void addListener(L listener) {
        if (listener == null) {
            return;
        }
        lazyInit();
        listeners.add(listener);
    }

    public void removeListener(L listener) {
        if (listener == null || listeners == null) {
            return;
        }
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            listeners = null;
        }
    }

    public int getListenerCount() {
        if (listeners == null) {
            return 0;
        }
        return listeners.size();
    }

    public boolean isEmpty() {
        if (listeners == null) {
            return true;
        }
        return listeners.isEmpty();
    }

    private class InverseArrayList<L> extends ArrayList<L> {
        public Iterator<L> iterator() {
            return new Iterator<L>() {
                int pos = size() - 1;

                public boolean hasNext() {
                    return pos >= 0;
                }

                public L next() {
                    if (!hasNext()) {
                        throw new NoSuchElementException();
                    }
                    return get(pos--);
                }

                public void remove() {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }
}

class Args {
    private static final String GT = " must be greater than ";
    private static final String GTE = " must be greater or equal to ";
    private static final String LT = " must be less than ";
    private static final String LTE = " must be less or equal to ";

    private static final String EQUALS = " must be equal to ";

    public static void doesNotContainNull(Iterable<?> iterable) {
        notNull(iterable);
        for (Object o : iterable) {
            notNull("Iterable contains null", o);
        }
    }

    public static void isTrue(boolean condition) {
        isTrue("Condition failed", condition);
    }

    public static void isTrue(String msg, boolean condition) {
        if (!condition) {
            throw new RuntimeException(msg);
        }
    }

    public static void notNull(Object o) {
        notNull(null, o);
    }

    public static void notNull(String arg, Object o) {
        if (arg == null) {
            arg = "Argument";
        }
        if (o == null) {
            throw new IllegalArgumentException(arg + " is null");
        }
    }

    public static void notNull(Object... args) {
        notNull(null, args);
    }

    public static void notNull(String message, Object... args) {
        if (message == null) {
            message = "Some argument";
        }
        for (Object o : args) {
            notNull(message, o);
        }
    }

    public static void notEmpty(Iterable<?> iter) {
        notEmpty(null, iter);
    }

    public static void notEmpty(String arg, Iterable<?> iter) {
        if (arg == null) {
            arg = "Iterable";
        }
        notNull(iter);
        if (iter.iterator().hasNext()) return;
        throw new IllegalArgumentException(arg + " is empty");
    }

    public static void hasNoNull(Iterable<?> iter) {
        hasNoNull(null, iter);
    }

    public static void hasNoNull(String arg, Iterable<?> iter) {
        notNull(iter);
        if (arg == null) {
            arg = "Iterable";
        }
        for (Object o : iter) {
            if (o == null) {
                throw new IllegalArgumentException(arg + " contains null");
            }
        }
    }

    public static void equals(int value, int expected) {
        if (value == expected) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void equals(long value, long expected) {
        if (value == expected) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void equals(double value, double expected) {
        if (value == expected) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void equals(float value, float expected) {
        if (value == expected) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void equals(char value, char expected) {
        if (value == expected) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void equals(short value, short expected) {
        if (value == expected) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void equals(byte value, byte expected) {
        if (value == expected) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void equals(Object value, Object expected) {
        if (value == expected || value.equals(expected)) return;
        throw new IllegalArgumentException(value + EQUALS + expected);
    }

    public static void gt(int value, int from) {
        if (value > from) return;
        throw new IllegalArgumentException(value + GT + from);
    }

    public static void lt(int value, int from) {
        if (value < from) return;
        throw new IllegalArgumentException(value + LT + from);
    }

    public static void gte(int value, int from) {
        if (value >= from) return;
        throw new IllegalArgumentException(value + GTE + from);
    }

    public static void lte(int value, int from) {
        if (value <= from) return;
        throw new IllegalArgumentException(value + LTE + from);
    }

    public static void gt(long value, long from) {
        if (value > from) return;
        throw new IllegalArgumentException(value + GT + from);
    }

    public static void lt(long value, long from) {
        if (value < from) return;
        throw new IllegalArgumentException(value + LT + from);
    }

    public static void gte(long value, long from) {
        if (value >= from) return;
        throw new IllegalArgumentException(value + GTE + from);
    }

    public static void lte(long value, long from) {
        if (value <= from) return;
        throw new IllegalArgumentException(value + LTE + from);
    }

    public static void gt(short value, short from) {
        if (value > from) return;
        throw new IllegalArgumentException(value + GT + from);
    }

    public static void lt(short value, short from) {
        if (value < from) return;
        throw new IllegalArgumentException(value + LT + from);
    }

    public static void gte(short value, short from) {
        if (value >= from) return;
        throw new IllegalArgumentException(value + GTE + from);
    }

    public static void lte(short value, short from) {
        if (value <= from) return;
        throw new IllegalArgumentException(value + LTE + from);
    }

    public static void gt(byte value, byte from) {
        if (value > from) return;
        throw new IllegalArgumentException(value + GT + from);
    }

    public static void lt(byte value, byte from) {
        if (value < from) return;
        throw new IllegalArgumentException(value + LT + from);
    }

    public static void gte(byte value, byte from) {
        if (value >= from) return;
        throw new IllegalArgumentException(value + GTE + from);
    }

    public static void lte(byte value, byte from) {
        if (value <= from) return;
        throw new IllegalArgumentException(value + LTE + from);
    }

    public static void gt(char value, char from) {
        if (value > from) return;
        throw new IllegalArgumentException(value + GT + from);
    }

    public static void lt(char value, char from) {
        if (value < from) return;
        throw new IllegalArgumentException(value + LT + from);
    }

    public static void gte(char value, char from) {
        if (value >= from) return;
        throw new IllegalArgumentException(value + GTE + from);
    }

    public static void lte(char value, char from) {
        if (value <= from) return;
        throw new IllegalArgumentException(value + LTE + from);
    }

    public static void gt(double value, double from) {
        if (value > from) return;
        throw new IllegalArgumentException(value + GT + from);
    }

    public static void lt(double value, double from) {
        if (value < from) return;
        throw new IllegalArgumentException(value + LT + from);
    }

    public static void gte(double value, double from) {
        if (value >= from) return;
        throw new IllegalArgumentException(value + GTE + from);
    }

    public static void lte(double value, double from) {
        if (value <= from) return;
        throw new IllegalArgumentException(value + LTE + from);
    }

    public static void gt(float value, float from) {
        if (value > from) return;
        throw new IllegalArgumentException(value + GT + from);
    }

    public static void lt(float value, float from) {
        if (value < from) return;
        throw new IllegalArgumentException(value + LT + from);
    }

    public static void gte(float value, float from) {
        if (value >= from) return;
        throw new IllegalArgumentException(value + GTE + from);
    }

    public static void lte(float value, float from) {
        if (value <= from) return;
        throw new IllegalArgumentException(value + LTE + from);
    }

    public static <T> void gt(Comparable<T> c1, T c2) {
        if (c1.compareTo(c2) > 0) return;
        throw new IllegalArgumentException(c1 + GT + c2);
    }

    public static <T> void lt(Comparable<T> c1, T c2) {
        if (c1.compareTo(c2) < 0) return;
        throw new IllegalArgumentException(c1 + LT + c2);
    }

    public static <T> void gte(Comparable<T> c1, T c2) {
        if (c1.compareTo(c2) >= 0) return;
        throw new IllegalArgumentException(c1 + GTE + c2);
    }

    public static <T> void lte(Comparable<T> c1, T c2) {
        if (c1.compareTo(c2) <= 0) return;
        throw new IllegalArgumentException(c1 + LTE + c2);
    }

    public static <T> void inRangeII(Comparable<T> value, T from, T to) {
        gte(value, from);
        lte(value, to);
    }

    public static <T> void inRangeEE(Comparable<T> value, T from, T to) {
        gt(value, from);
        lt(value, to);
    }

    public static <T> void inRangeIE(Comparable<T> value, T from, T to) {
        gt(value, from);
        lt(value, to);
    }

    public static <T> void inRangeEI(Comparable<T> value, T from, T to) {
        gt(value, from);
        lte(value, to);
    }

    public static void inRangeII(int value, int from, int to) {
        gte(value, from);
        lte(value, to);
    }

    public static void inRangeEE(int value, int from, int to) {
        gt(value, from);
        lt(value, to);
    }

    public static void inRangeIE(int value, int from, int to) {
        gte(value, from);
        lt(value, to);
    }

    public static void inRangeEI(int value, int from, int to) {
        gt(value, from);
        lte(value, to);
    }

    public static void inRangeII(long value, long from, long to) {
        gte(value, from);
        lte(value, to);
    }

    public static void inRangeEE(long value, long from, long to) {
        gt(value, from);
        lt(value, to);
    }

    public static void inRangeIE(long value, long from, long to) {
        gte(value, from);
        lt(value, to);
    }

    public static void inRangeEI(long value, long from, long to) {
        gt(value, from);
        lte(value, to);
    }

    public static void inRangeII(short value, short from, short to) {
        gte(value, from);
        lte(value, to);
    }

    public static void inRangeEE(short value, short from, short to) {
        gt(value, from);
        lt(value, to);
    }

    public static void inRangeIE(short value, short from, short to) {
        gte(value, from);
        lt(value, to);
    }

    public static void inRangeEI(short value, short from, short to) {
        gt(value, from);
        lte(value, to);
    }

    public static void inRangeII(byte value, byte from, byte to) {
        gte(value, from);
        lte(value, to);
    }

    public static void inRangeEE(byte value, byte from, byte to) {
        gt(value, from);
        lt(value, to);
    }

    public static void inRangeIE(byte value, byte from, byte to) {
        gte(value, from);
        lt(value, to);
    }

    public static void inRangeEI(byte value, byte from, byte to) {
        gt(value, from);
        lte(value, to);
    }

    public static void check(boolean assertion, String messageIfFailed) {
        if (!assertion) {
            throw new RuntimeException(messageIfFailed);
        }
    }
}

final class SerializableObject implements Serializable {
    private static final long serialVersionUID = 0L;
}

abstract class AbstractPath implements Path {
    public boolean contains(Path path) {
        return find(path) != -1;
    }

    public ExtendedIterable<Node> nodes() {
        return new ExtendedIterable<Node>(new Iterable<Node>() {
            public Iterator<Node> iterator() {
                return new Iterator<Node>() {
                    boolean first = true;
                    final Iterator<Path> stepIterator = steps().iterator();

                    public boolean hasNext() {
                        return (first || stepIterator.hasNext());
                    }

                    public Node next() {
                        if (first) {
                            first = false;
                            return headNode();
                        }
                        return stepIterator.next().tailNode();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        });
    }

    public ExtendedIterable<Edge> edges() {
        return new ExtendedIterable<Edge>(new Iterable<Edge>() {
            public Iterator<Edge> iterator() {
                return new Iterator<Edge>() {
                    final Iterator<Path> stepIterator = steps().iterator();

                    public boolean hasNext() {
                        return stepIterator.hasNext();
                    }

                    public Edge next() {
                        return stepIterator.next().tailEdge();
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        });
    }

    public boolean isCycle() {
        return headNode() == tailNode() && size() > 0;
    }

    public boolean isEuler() {
        return hasDuplicates(edges());
    }

    public boolean isHamilton() {
        return hasDuplicates(nodes());
    }

    private boolean hasDuplicates(Iterable<? extends Tuple> iterable) {
        final Object tempKey = new Object();
        for (Tuple tuple : iterable) {
            if (tuple.has(tempKey)) {
                return false;
            }
            tuple.putWeakly(tempKey, null);
        }
        return true;
    }

    public int edgeCount() {
        return size();
    }

    public int nodeCount() {
        return size() + 1;
    }

    public int find(Path path) {
        return find(path, 0);
    }

    public boolean equals(Object o) {
        if (!(o instanceof Path)) {
            return false;
        }
        if (o == this) {
            return true;
        }
        Path other = (Path) o;
        if (size() != other.size()) {
            return false;
        }
        if (headNode() != other.headNode()) {
            return false;
        }
        Iterator<Path> myIter = steps().iterator();
        Iterator<Path> otherIter = other.steps().iterator();
        while (myIter.hasNext()) {
            if (myIter.next().tailEdge() != otherIter.next().tailEdge()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 17;
        hash += 37 * headNode().hashCode();
        for (Path p : steps()) {
            hash += 31 * p.tailEdge().hashCode();
        }
        return hash;
    }

    public int find(Path path, int from) {
        Args.notNull(path);
        List<Path> steps = steps().drainToList();
        Node head = headNode();
        out:
        for (int i = from,  len = size() - path.size() + 1; i < len; i++) {
            if (i > 0) {
                head = steps.get(i - 1).tailNode();
            }
            if (head == path.headNode()) {
                List<Path> followingSteps = steps.subList(i, i + path.size());
                int j = 0;
                for (Path step : path.steps()) {
                    if (!followingSteps.get(j).equals(step)) {
                        continue out;
                    }
                    j++;
                }
                return i;
            }
        }
        return -1;
    }

    public Path replaceFirst(Path subpath, Path replacement) {
        int index = find(subpath);
        if (index != -1) {
            return replace(index, index + subpath.size(), replacement);
        }
        return this;
    }

    public Path replaceAll(Path subpath, Path replacement) {
        Args.notNull(subpath, replacement);
        Path path = this;
        int index = 0;
        while (true) {
            index = path.find(subpath, index);
            if (index == -1) {
                break;
            }
            path = path.replace(index, index + subpath.size(), replacement);
            index += replacement.size() + 1;
        }
        return path;
    }

    public Path[] split(int position) {
        Path[] paths = new Path[2];
        paths[0] = headPath(position);
        paths[1] = tailPath(size() - position);
        return paths;
    }

    
    protected final void chechReplacementPreconditions(int start, int end, Path replacement) {
        if (start != 0) {
            Args.isTrue("Replacement does not start at the same node as the replaced part",
                    getNode(start) == replacement.headNode());
        }
        if (end != nodeCount()) {
            Args.isTrue("Replacement does not end at the same node as the replaced part",
                    getNode(end) == replacement.tailNode());
        }
    }

    public Path append(Path other) {
        return DefaultPath.newPath(this, other);
    }

    public Path replace(int start, int end, Path replacement) {
        Path headPath = headPath(start);
        Path tailPath = tailPath(edgeCount() - end);
        if (start == 0) {
            return replacement.append(tailPath);
        } else if (end == size()) {
            return headPath.append(replacement);
        } else {
            return headPath.append(replacement).append(tailPath);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(headNode().toString());
        for (Path s : steps()) {
            boolean inverted = s.headEdge().n2() != s.tailNode();
            sb.append(inverted ? "<--" : "-->");
            sb.append(s.tailNode().toString());
        }
        sb.append(']');
        return sb.toString();
    }
}

class CompoundListIterator<E> extends AbstractCompoundListIterator<E> {
    private final ListIterator<ListIterator<E>> iteratorIterator;

    public CompoundListIterator(ListIterator<E> iterator1, ListIterator<E> iterator2) {
        List<ListIterator<E>> list = new ArrayList<ListIterator<E>>(2);
        if (iterator1 != null) list.add(iterator1);
        if (iterator2 != null) list.add(iterator2);
        iteratorIterator = list.listIterator();
    }

    public CompoundListIterator(List<ListIterator<E>> listOfIterators) {
        Args.notNull(listOfIterators);
        iteratorIterator = listOfIterators.listIterator();
    }

    protected ListIterator<E> nextIterator() {
        return iteratorIterator.next();
    }

    protected boolean hasNextIterator() {
        return iteratorIterator.hasNext();
    }

    protected ListIterator<E> previousIterator() {
        return iteratorIterator.previous();
    }

    protected boolean hasPreviousIterator() {
        return iteratorIterator.hasPrevious();
    }
}

abstract class AbstractCompoundListIterator<E> implements ListIterator<E> {
    private ListIterator<E> currentIterator;
    private int index = 0;

    public AbstractCompoundListIterator() {
        currentIterator = Collections.<E>emptyList().listIterator();
    }

    protected E last;

    protected abstract ListIterator<E> nextIterator();
    protected abstract boolean hasNextIterator();
    protected abstract ListIterator<E> previousIterator();
    protected abstract boolean hasPreviousIterator();

    private void proceedForward() {
        while (!currentIterator.hasNext()) {
            if (hasNextIterator()) {
                currentIterator = nextIterator();
            } else {
                return;
            }
        }
    }

    private void proceedBackward() {
        while (!currentIterator.hasPrevious()) {
            if (hasPreviousIterator()) {
                currentIterator = previousIterator();
            } else {
                return;
            }
        }
    }

    public boolean hasNext() {
        proceedForward();
        return currentIterator.hasNext();
    }

    public E next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        proceedForward();
        E next = currentIterator.next();
        last = next;
        index++;
        return next;

    }

    public boolean hasPrevious() {
        proceedBackward();
        return currentIterator.hasPrevious();
    }

    public E previous() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        E prev = currentIterator.previous();
        last = prev;
        index--;
        return prev;
    }

    public int previousIndex() {
        return index - 1;
    }

    public int nextIndex() {
        return index;
    }

    public void set(E o) {
        currentIterator.set(o);
        last = null;
    }

    public void remove() {
        currentIterator.remove();
        last = null;
    }

    public void add(E o) {
        currentIterator.add(o);
        last = null;
    }
}

abstract class AbstractTuple implements Tuple {
    private static final Object PARENT = new SerializableObject();

    private Object value;

    public AbstractTuple() { }

    public AbstractTuple(Object value) {
        setValue(value);
    }

    public Object setValue(Object value) {
        Object old = this.value;
        this.value = value;
        return old;
    }

    public Object getValue() {
        return value;
    }

    public void copyInto(Tuple copy) {
        Args.notNull(copy);
        copy.setValue(getValue());
        for (Object key : keySet()) {
            Object value = get(key);
            copy.put(key, value);
        }
    }

    public boolean equalValues(Tuple that) {
        if (that == null) {
            return false;
        }
        Object thisValue = getValue();
        Object thatValue = that.getValue();
        if ((thisValue == null && thatValue != null) || (thisValue != null && !thisValue.equals(thatValue))) {
            return false;
        }
        Set<Object> thisKeys = keySet();
        Set<Object> thatKeys = that.keySet();
        if (thisKeys.size() != thatKeys.size()) {
            return false;
        }

        for (Object key : thisKeys) {
            Object value = get(key);
            Object otherValue = that.get(key);
            if (value == null && otherValue != null) {
                return false;
            }
            if (value != null && !value.equals(otherValue)) {
                return false;
            }
        }
        thatKeys.removeAll(thisKeys);
        for (Object key : thatKeys) {
            Object value = get(key);
            Object otherValue = that.get(key);
            if (value == null && otherValue != null) {
                return false;
            }
            if (!value.equals(otherValue)) {
                return false;
            }
        }
        return true;
    }

    public Node getNode(Object key) {
        return (Node)get(key);
    }

    public Edge getEdge(Object key) {
        return (Edge)get(key);
    }

    public Boolean getBoolean(Object key) {
        return (Boolean)get(key);
    }

    public int getInt(Object key) {
        Number number = ((Number)get(key));
        return number.intValue();
    }

    public short getShort(Object key) {
        Number number = ((Number)get(key));
        return number.shortValue();
    }


    public long getLong(Object key) {
        Number number = ((Number)get(key));
        return number.longValue();
    }

    public double getDouble(Object key) {
        Number number = ((Number)get(key));
        return number.doubleValue();
    }

    public float getFloat(Object key) {
        Number number = ((Number)get(key));
        return number.floatValue();
    }

    public String getString(Object key) {
        return (String)get(key);
    }

    public Character getChar(Object key) {
        return ((Character)get(key)).charValue();
    }

    public InspectableGraph getInspectableGraph(Object key) {
        return (InspectableGraph)get(key);
    }

    public Graph getGraph(Object key) {
        return (Graph)get(key);
    }

    public Number getNumber(Object key) {
        return (Number)get(key);
    }

    public Tuple getTuple(Object key) {
        return (Tuple)getLocally(key);
    }


    abstract Object getLocally(Object key);
    abstract boolean hasLocally(Object key);

    public final Object get(Object key) {
        Object value = getLocally(key);
        if (value == null && !hasLocally(key)) {
            Tuple parent = getParentTuple();
            if (parent == null) {
                return null;
            }
            return parent.get(key);
        }
        return value;
    }

    public final boolean has(Object key) {
        boolean exists = hasLocally(key);
        if (!exists) {
            Tuple parent = getParentTuple();
            if (parent == null) {
                return false;
            }
            return parent.has(key);
        }
        return exists;
    }

    public void setParentTuple(Tuple parent) {
        putWeakly(PARENT, parent);
    }

    public Tuple getParentTuple() {
        return getTuple(PARENT);
    }
}

abstract class AbstractGraph extends AbstractInspectableGraph implements Graph {
    public int removeEdges(Iterable<Edge> edges) {
        if (edges == null) {
            return 0;
        }
        int removed = 0;
        for (Edge e : edges) {
            if (removeEdge(e)) {
                removed++;
            }
        }
        return removed;
    }

    public Node[] newNodes(int count) {
        if (count < 0) {
            throw new IllegalArgumentException();
        }
        Node[] ids = new Node[count];
        for (int i = 0; i < count; ++i) {
            ids[i] = newNode(null);
        }
        return ids;
    }

    public Node newNode() {
        return newNode(null);
    }

    public Edge newEdge(Node n1, Node n2) {
        return newEdge(n1, n2, null);
    }

    public Node[] newNodes(Object ... values) {
        Node[] ids = new Node[values.length];
        for (int i = 0; i < values.length; ++i) {
            ids[i] = newNode(values[i]);
        }
        return ids;
    }

    public int removeNodes(Iterable<Node> nodes) {
        if (nodes == null) {
            return 0;
        }
        int removed = 0;
        for (Node node : nodes) {
            if (removeNode(node)) {
                removed++;
            }
        }
        return removed;
    }

    public int removeAllEdges() {
        int countEdges = edgeCount();
        for (Edge e : edges()) {
            removeEdge(e);
        }
        return countEdges;
    }

    public int removeAllNodes() {
        int countNodes = nodeCount();
        for (Node n : nodes()) {
            removeNode(n);
        }
        return countNodes;
    }

    public boolean isPrimary() {
        return false;
    }

    public void importGraph(Graph g) {
        importGraph(g, g.nodes());
    }

    public Collection<Edge> importGraph(Graph g, Iterable<Node> nodes) {
        Args.notNull(g);
        Args.notNull(nodes);
        Collection<Edge> innerEdges = new LinkedList<Edge>();
        Collection<Edge> interEdges = new LinkedList<Edge>();
        Collection<Node> importedNodes = ExtendedIterable.wrap(nodes).drainTo(new LinkedList<Node>());
        for (Node n : importedNodes) {
            if (!g.containsNode(n)) {
                throw new IllegalArgumentException("Node not in imported graph");
            }
            for (Edge e : g.edges(n)) {
                if (importedNodes.contains(e.n1()) && importedNodes.contains(e.n2())) {

                    innerEdges.add(e);
                } else {

                    interEdges.add(e);
                }
            }
            g.removeNode(n);
        }
        for (Node n : importedNodes) {
            reinsertNode(n);
        }
        for (Edge inner : innerEdges) {
            reinsertEdge(inner);
        }
        return interEdges;
    }
}

interface Clusterer extends Iterable<Collection<Node>> {
    Collection<Object> getClusters();
    Collection<Node> getCluster(Object key);
    InspectableGraph getGraph();

    Object findClusterOf(Node node);
}

class Dfs extends AbstractSearch {
    private static enum EdgeType {
        treeEdge, forwardEdge, backEdge, crossEdge;
    }

    private Object EDGE_INFO;
    private Object NODE_INFO;

    private int time;

    private final Direction direction;

    public Dfs(InspectableGraph graph, Direction direction) {
        this(graph, null, direction);
    }

    public Dfs(InspectableGraph graph, Node startNode, Direction direction) {
        super(graph, startNode);
        Args.notNull(direction);
        this.direction = direction;
    }

    private void initKeys() {
        EDGE_INFO = new Object();
        NODE_INFO = new Object();
    }

    protected void executeImpl() {
        initKeys();
        time = 0;
        boolean exit = dfs(startNode);
        if (!exit) {
            for (Node n : graph.nodes()) {
                if (isUnexplored(n)) {
                    exit = dfs(n);
                    if (exit) {
                        break;
                    }
                }
            }
        }
    }

    protected boolean dfs(Node start) {
        Object tree = new Object();
        incTreeNumber();
        markNodeTree(start, tree);
        if (visitNewTree(start)) {
            return true;
        }
        Stack stack = new Stack();
        stack.push(start.asPath(), start, graph.edges(start, direction).iterator());
        outLoop:
        while (!stack.isEmpty()) {
            ExecutionPoint executionPoint = stack.pop();
            Node root = executionPoint.node;
            Iterator<Edge> iterator = executionPoint.iterator;
            NodeInfo rootInfo = getNode(root);
            if (rootInfo == null || rootInfo.justCreated) {
                Edge parent = (rootInfo == null ? null : rootInfo.parent);
                rootInfo = new NodeInfo(time++, parent);
                rootInfo.justCreated = false;
                markNode(root, rootInfo);
                markNodeTree(root, tree);
                if (visitPre(executionPoint.parent)) {
                    return true;
                }
            }
            while (iterator.hasNext()) {
                Edge e = iterator.next();
                Path currentPath = executionPoint.parent.append(e.asPath(executionPoint.parent.tailNode()));
                currentPath = storePath(currentPath);
                if (getEdge(e) != null) {
                    continue;
                }
                Node other = e.opposite(root);
                NodeInfo otherInfo = getNode(other);
                if (otherInfo == null) {
                    markEdge(e, EdgeType.treeEdge);
                    if (visitTreeEdge(currentPath)) {
                        return true;
                    }

                    stack.push(executionPoint.parent, root, iterator);
                    stack.push(currentPath, other, graph.edges(other, direction).iterator());
                    otherInfo = new NodeInfo(time, e);
                    markNode(other, otherInfo);
                    continue outLoop;
                } else if (otherInfo.doneVisiting == false) {
                    markEdge(e, EdgeType.backEdge);
                    if (visitBackEdge(currentPath)) {
                        return true;
                    }
                } else {
                    if (otherInfo.time.endTime < rootInfo.time.startTime) {
                        markEdge(e, EdgeType.crossEdge);
                        if (visitCrossEdge(currentPath)) {
                            return true;
                        }
                    } else {
                        markEdge(e, EdgeType.forwardEdge);
                        if (visitForwardEdge(currentPath)) {
                            return true;
                        }
                    }
                }
            }
            rootInfo.time.endTime = time++;
            if (visitPost(executionPoint.parent)) {
                return true;
            }
            rootInfo.doneVisiting = true;
        }
        return false;
    }



    
    protected boolean visitTreeEdge(Path path) { return false; }

    
    protected boolean visitPre(Path path) { return false; }

    
    protected boolean visitPost(Path path) { return false; }

    
    protected boolean visitForwardEdge(Path path) { return false; }

    
    protected boolean visitBackEdge(Path path) { return false; }

    
    protected boolean visitCrossEdge(Path path) { return false; }

    private NodeInfo getNode(Node node) {
        return (NodeInfo)node.get(NODE_INFO);
    }

    private void markEdge(Edge edge, EdgeType type) {
        edge.putWeakly(EDGE_INFO, type);
    }

    private EdgeType getEdge(Edge e) {
        return (EdgeType)e.get(EDGE_INFO);
    }

    private void markNode(Node node, NodeInfo info) {
        node.putWeakly(NODE_INFO, info);
    }

    public boolean isTreeEdge(Edge e) {
        return isType(e, EdgeType.treeEdge);
    }

    public boolean isCrossEdge(Edge e) {
        return isType(e, EdgeType.crossEdge);
    }

    public boolean isForwardEdge(Edge e) {
        return isType(e, EdgeType.forwardEdge);
    }

    public boolean isBackEdge(Edge e) {
        return isType(e, EdgeType.backEdge);
    }

    private boolean isType(Edge e, EdgeType type) {
        EdgeType edgeType = getEdge(e);
        if (edgeType == null) {
            return false;
        }
        return edgeType == type;
    }

    public boolean isUnexplored(Node node) {
        return getNode(node) == null;
    }

    public boolean isVisited(Node node) {
        NodeInfo info = getNode(node);
        if (info == null) {
            return false;
        }
        return info.doneVisiting;
    }

    public boolean isVisiting(Node node) {
        NodeInfo info = getNode(node);
        if (info == null) {
            return false;
        }
        return !info.doneVisiting;
    }

    public Time getTime(Node node) {
        NodeInfo info = getNode(node);
        if (info == null) {
            return null;
        }
        return info.time;
    }

    public Node getParent(Node node) {
        Edge parent = getParentEdge(node);
        if (parent == null) {
            return null;
        }
        return parent.opposite(node);
    }

    public Edge getParentEdge(Node node) {
        NodeInfo info = getNode(node);
        if (info == null) {
            return null;
        }
        return info.parent;
    }

    private static class NodeInfo {
        final Time time;
        boolean doneVisiting = false;
        boolean justCreated = true;
        final Edge parent;

        NodeInfo(int time) {
            this(time, null);
        }

        NodeInfo(int time, Edge parent) {
            this.time = new Time(time);
            this.parent = parent;
        }
    }

    public static class Time {
        int startTime = -1;
        int endTime = -1;

        Time(int start) {
            this.startTime = start;
        }

        public int getStart() {
            return startTime;
        }

        public int getFinish() {
            return endTime;
        }

        public String toString() {
            return "[" + startTime + ".." + endTime + "]";
        }
    }

    private static class Stack {
        private final LinkedList<ExecutionPoint> list = new LinkedList<ExecutionPoint>();

        void push(Path current, Node node, Iterator<Edge> iterator) {
            list.addLast(new ExecutionPoint(current, node, iterator));
        }

        ExecutionPoint pop() {
            return list.removeLast();
        }

        boolean isEmpty() {
            return list.isEmpty();
        }
    }

    private static class ExecutionPoint {
        final Node node;
        final Iterator<Edge> iterator;
        final Path parent;

        ExecutionPoint(Path parent, Node node, Iterator<Edge> iterator) {
            this.parent = parent;
            this.node = node;
            this.iterator = iterator;
        }
    }
}

abstract class AbstractSearch {
    protected final InspectableGraph graph;
    protected Node startNode;

    private int treeCount;

    private Object NODE_TREE_INFO;

    public AbstractSearch(InspectableGraph graph) {
        this(graph, null);
    }

    public AbstractSearch(InspectableGraph graph, Node startNode) {
        Args.notNull(graph);
        this.graph = graph;
        this.startNode = startNode;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node node) {
        this.startNode = node;
    }

    public InspectableGraph getGraph() {
        return graph;
    }

    public final void execute() {
        NODE_TREE_INFO = new Object();
        if (startNode == null) {
            if (graph.nodeCount() == 0) {
                return;
            }
            startNode = graph.aNode();
        }
        Args.isTrue("Start node not contained in graph", graph.containsNode(startNode));
        treeCount = 0;
        executeImpl();
    }

    protected abstract void executeImpl();

    protected void incTreeNumber() {
        treeCount++;
    }

    public int getComponentCount() {
        return treeCount;
    }

    protected void markNodeTree(Node node, Object treeIdentifier) {
        node.putWeakly(NODE_TREE_INFO, treeIdentifier);
    }


    protected Path storePath(Path path) {
        return path;
    }

    
    protected boolean visitNewTree(Node node) { return false; }

    public Object getComponentIdentifier(Node node) {
        return node.get(NODE_TREE_INFO);
    }
}

abstract class AbstractInspectableGraph implements InspectableGraph, Serializable {
    transient GraphEventSupport graphEventSupport = new GraphEventSupport();
    int edgeCount;

    private final Set<Hint> hints = EnumSet.noneOf(Hint.class);

    private final Tuple tuple = new TupleImpl();

    public void hint(Hint hint) {
        Args.notNull(hint);
        hints.add(hint);
    }

    final boolean containsHint(Hint hint) {
        return hints.contains(hint);
    }

    public void addGraphListener(GraphListener listener) {
        graphEventSupport.addGraphListener(listener);
    }

    public void removeGraphListener(GraphListener listener) {
        graphEventSupport.removeGraphListener(listener);
    }

    public void addNodeListener(NodeListener listener) {
        graphEventSupport.addNodeListener(listener);
    }

    public void removeNodeListener(NodeListener listener) {
        graphEventSupport.removeNodeListener(listener);
    }

    public void addEdgeListener(EdgeListener listener) {
        graphEventSupport.addEdgeListener(listener);
    }

    public void removeEdgeListener(EdgeListener listener) {
        graphEventSupport.removeEdgeListener(listener);
    }

    public List<NodeListener> getNodeListeners() {
        return graphEventSupport.getNodeListeners();
    }

    public List<EdgeListener> getEdgeListeners() {
        return graphEventSupport.getEdgeListeners();
    }

    public Tuple tuple() {
        return tuple;
    }

    public boolean isEmpty() {
        return nodeCount() == 0;
    }

    public boolean areAdjacent(Node n1, Node n2) {
        return edges(n1, n2).iterator().hasNext();
    }

    public boolean areAdjacent(Node n1, Node n2, Direction direction) {
        return edges(n1, n2, direction).iterator().hasNext();
    }

    public ExtendedListIterable<Edge> edges(Node node) {
        return edges(node, Direction.EITHER);
    }

    public ExtendedListIterable<Edge> edges(Node n1, Node n2) {
        return edges(n1, n2, Direction.EITHER);
    }

    public ExtendedListIterable<Node> adjacentNodes(Node n) {
        return adjacentNodes(n, Direction.EITHER);
    }

    public Edge anEdge() {
        return firstEdge(edges());
    }

    public Edge anEdge(Node node) {
        return firstEdge(edges(node));
    }

    public Edge anEdge(Node node, Direction direction) {
        return firstEdge(edges(node, direction));
    }

    public Edge anEdge(Node n1, Node n2) {
        return firstEdge(edges(n1, n2));
    }

    public Edge anEdge(Node n1, Node n2, Direction direction) {
        return firstEdge(edges(n1, n2, direction));
    }

    public Node aNode() {
        return firstNode(nodes());
    }

    public Node aNode(Node neighbor) {
        return firstNode(adjacentNodes(neighbor));
    }

    public Node aNode(Node neighbor, Direction direction) {
        return firstNode(adjacentNodes(neighbor, direction));
    }

    private Edge firstEdge(Iterable<Edge> e) {
        Iterator<Edge> edges = e.iterator();
        if (edges.hasNext()) {
            return edges.next();
        }
        throw new NoSuchElementException();
    }

    private Node firstNode(Iterable<Node> n) {
        Iterator<Node> nodes = n.iterator();
        if (nodes.hasNext()) {
            return nodes.next();
        }
        throw new NoSuchElementException();
    }

    public int degree(Node node) {
        return outDegree(node) + inDegree(node);
    }

    public int degree(Node node, Direction direction) {
        switch (direction) {
            case OUT:
                return outDegree(node);
            case IN:
                return inDegree(node);
            default:
                return degree(node);
        }
    }

    private void readObject(ObjectInputStream in) throws Exception {
        in.defaultReadObject();
        graphEventSupport = new GraphEventSupport();
    }

    public String toString() {
        return Graphs.printPretty(this).toString();
    }
}

enum Hint {
    
    FAST_NODE_ITERATION,

    
    FAST_EDGE_ITERATION
}

class Graphs {
    public static Set<Node> collectNodes(InspectableGraph graph, Node start, Direction direction) {
        return collectNodes(graph, start, direction, new HashSet<Node>());
    }

    public static Set<Node> collectNodes(InspectableGraph graph, Node start, Direction direction, Set<Node> set) {
        Args.notNull(graph, "graph");
        Args.notNull(start, "node");
        Args.notNull(direction, "direction");
        Args.notNull(set, "set");

        LinkedList<Node> stack = new LinkedList<Node>();
        stack.add(start);
        while (!stack.isEmpty()) {
            Node current = stack.removeLast();
            for (Node next : graph.adjacentNodes(current, direction)) {
                boolean changed = set.add(next);
                if (changed) {
                    stack.addLast(next);
                }
            }
        }
        return set;
    }

    public static Appendable printPretty(InspectableGraph g) {
        return printPretty(g, new StringBuilder());
    }

    public static Appendable printPretty(InspectableGraph g, Appendable appendable) {
        Args.notNull("Appendable", appendable);
        try {
            if (g == null) {
                appendable.append("null");
                return appendable;
            }
            appendable.append("Nodes (count = ").append("" + g.nodeCount()).append("):\n");
            for (Node n : g.nodes()) {
                appendable.append(n.toString()).append('\n');
            }
            appendable.append("\nEdges (count = ").append("" + g.edgeCount()).append("):\n");
            for (Edge e : g.edges()) {
                appendable.append(e.toString()).append('\n');
            }
            return appendable;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Appendable printCompact(InspectableGraph g) {
        return printCompact(g, new StringBuilder());
    }

    public static Appendable printCompact(InspectableGraph g, Appendable appendable) {
        Args.notNull("Appendable", appendable);
        try {
            appendable.append("[N={");
            int pos = g.nodeCount();
            for (Node n : g.nodes()) {
                appendable.append(n.toString());
                if (pos > 1) {
                    appendable.append(", ");
                }
                pos--;
            }
            appendable.append("}, E={");
            pos = g.edgeCount();
            for (Edge e : g.edges()) {
                appendable.append(e.toString());
                if (pos > 1) {
                    appendable.append(", ");
                }
                pos--;
            }
            appendable.append("}]");
            return appendable;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static InspectableGraph undirected(InspectableGraph graph) {
        Args.notNull(graph);
        if (graph instanceof UndirectedInspectableGraph) {
            return graph;
        }
        return new UndirectedInspectableGraph(graph);
    }

    public static InspectableGraph inverted(InspectableGraph graph) {
        Args.notNull(graph);
        if (graph instanceof InvertedInspectableGraph) {
            ((InvertedInspectableGraph)graph).getDelegateGraph();
        }
        return new InvertedInspectableGraph(graph);
    }

    
    public static int maxDegree(InspectableGraph g, Direction direction) {
        Args.notNull(g, direction);
        int max = 0;
        for (Node n : g.nodes()) {
            int d = g.degree(n, direction);
            if (max < d) {
                max = d;
            }
        }
        return max;
    }

    
    public static int maxDegree(InspectableGraph g) {
        return maxDegree(g, Direction.EITHER);
    }

    
    public static int minDegree(InspectableGraph g, Direction direction) {
        Args.notNull(g, direction);
        int min = Integer.MAX_VALUE;
        for (Node n : g.nodes()) {
            int d = g.degree(n, direction);
            if (min > d) {
                min = d;
            }
        }
        return min;
    }

    
    public static int minDegree(InspectableGraph g) {
        return minDegree(g, Direction.EITHER);
    }

    public static InspectableGraph union(InspectableGraph g1, InspectableGraph g2) {
        Args.notNull(g1, g2);
        SecondaryGraph sg = new SecondaryGraph(g1);
        sg.adoptGraph(g2);
        DualListener l = new UnionListener(sg);
        l.setGraphs(g1, g2);
        g1.addGraphListener(l);
        g2.addGraphListener(l);
        return sg;
    }

    public static InspectableGraph intersection(InspectableGraph g1, InspectableGraph g2) {
        Args.notNull(g1, g2);
        Args.notNull(g1, g2);
        SecondaryGraph sg = new SecondaryGraph(g1);
        sg.retainGraph(g2);
        DualListener l = new IntersectionListener(sg);
        l.setGraphs(g1, g2);
        g1.addGraphListener(l);
        g2.addGraphListener(l);
        return sg;
    }

    public static InspectableGraph subtraction(InspectableGraph g1, InspectableGraph g2) {
        Args.notNull(g1, g2);
        SecondaryGraph sg = new SecondaryGraph(g1);
        sg.removeGraph(g2);
        DualListener l = new SubtractionListener(sg);
        l.setGraphs(g1, g2);
        g1.addGraphListener(l);
        g2.addGraphListener(l);
        return sg;
    }

    public static InspectableGraph xor(InspectableGraph g1, InspectableGraph g2) {
        Args.notNull(g1, g2);
        int size1 = g1.nodeCount() + g1.edgeCount();
        int size2 = g2.nodeCount() + g2.edgeCount();
        InspectableGraph bigGraph = size1 > size2 ? g1 : g2;
        InspectableGraph smallGraph = bigGraph == g1 ? g2 : g1;
        SecondaryGraph sg = new SecondaryGraph(bigGraph);
        for (Node n : smallGraph.nodes()) {
            if (bigGraph.containsNode(n)) {
                sg.removeNode(n);
            }
        }
        for (Edge e : smallGraph.edges()) {
            if (bigGraph.containsEdge(e)) {
                sg.removeEdge(e);
            }
        }
        DualListener l = new XorListener(sg);
        l.setGraphs(g1, g2);
        g1.addGraphListener(l);
        g2.addGraphListener(l);
        return sg;
    }

    
    public static boolean equalGraphs(InspectableGraph g1, InspectableGraph g2) {
        Args.notNull(g1, g2);
        if (g1 == g2) {
            return true;
        }
        if (g1.nodeCount() != g2.nodeCount()) {
            return false;
        }
        if (g1.edgeCount() != g2.edgeCount()) {
            return false;
        }
        for (Node n : g1.nodes()) {
            if (!g2.containsNode(n)) {
                return false;
            }
        }
        for (Edge e : g1.edges()) {
            if (!g2.containsEdge(e)) {
                return false;
            }
        }
        return true;
    }

    private static abstract class DualListener extends EmptyGraphListener {
        final SecondaryGraph g;
        InspectableGraph g1, g2;
        DualListener(SecondaryGraph g) {
            this.g = g;
        }
        void setGraphs(InspectableGraph g1, InspectableGraph g2) {
            this.g1 = g1;
            this.g2 = g2;
        }
    }

    private static class IntersectionListener extends DualListener {
        IntersectionListener(SecondaryGraph g) { super(g); }

        @Override public void nodeAdded(GraphEvent e) {
            Node n = e.getNode();
            if (!g1.containsNode(n) || !g2.containsNode(n)) {
                return;
            }
            g.adoptNode(n);
        }

        @Override public void nodeRemoved(GraphEvent e) {
            g.removeNode(e.getNode());
        }

        @Override public void edgeAdded(GraphEvent ev) {
            Edge e = ev.getEdge();
            if (!g1.containsEdge(e) || !g2.containsEdge(e)) {
                return;
            }
            g.adoptEdge(e);
        }

        @Override public void edgeRemoved(GraphEvent ev) {
            g.removeEdge(ev.getEdge());
        }
    }

    private static class UnionListener extends DualListener {
        UnionListener(SecondaryGraph g) { super(g); }

        @Override public void nodeAdded(GraphEvent e) {
            g.adoptNode(e.getNode());
        }

        @Override public void nodeRemoved(GraphEvent e) {
            Node n = e.getNode();
            int count = 0;
            if (g1.containsNode(n)) {
                count++;
            }
            if (g2.containsNode(n)) {
                count++;
            }
            if (count == 1) {
                return;
            }
            g.removeNode(n);
            return;
        }

        @Override public void edgeAdded(GraphEvent e) {
            g.adoptEdge(e.getEdge());
        }

        @Override public void edgeRemoved(GraphEvent ev) {
            Edge e = ev.getEdge();
            int count = 0;
            if (g1.containsEdge(e)) {
                count++;
            }
            if (g2.containsEdge(e)) {
                count++;
            }
            if (count == 1) {
                return;
            }
            g.removeEdge(e);
        }
    }

    private static class SubtractionListener extends DualListener {
        SubtractionListener(SecondaryGraph g) { super(g); }

        @Override public void nodeAdded(GraphEvent e) {
            Node n = e.getNode();
            InspectableGraph source = e.getSource();
            if (source == g2) {
                g.removeNode(n);
            } else if (!g2.containsNode(n)) {
                g.adoptNode(n);
            }
        }

        @Override public void nodeRemoved(GraphEvent e) {
            Node n = e.getNode();
            InspectableGraph source = e.getSource();
            if (source == g1) {
                g.removeNode(n);
            } else if (g1.containsNode(n)) {
                g.adoptNode(n);
                g.adoptEdges(g1.edges(n));
            }
        }

        @Override public void edgeAdded(GraphEvent ev) {
            Edge e = ev.getEdge();
            InspectableGraph source = ev.getSource();
            if (source == g2) {
                g.removeEdge(e);
            } else if (!g2.containsEdge(e)) {
                g.adoptEdge(e);
            }
        }

        @Override public void edgeRemoved(GraphEvent ev) {
            Edge e = ev.getEdge();
            InspectableGraph source = ev.getSource();
            if (source == g1) {
                g.removeEdge(e);
            } else if (g1.containsEdge(e)) {
                g.adoptEdge(e);
            }
        }
    }

    private static class XorListener extends DualListener {
        XorListener(SecondaryGraph g) { super(g); }

        @Override public void nodeAdded(GraphEvent e) {
            Node n = e.getNode();
            int count = 0;
            if (g1.containsNode(n)) {
                count++;
            }
            if (g2.containsNode(n)) {
                count++;
            }
            if (count == 2) {
                g.removeNode(n);
                return;
            }
            g.adoptNode(n);
        }

        @Override public void nodeRemoved(GraphEvent e) {
            Node n = e.getNode();
            if (g.containsNode(n)) {
                g.removeNode(n);
            } else {
                g.adoptNode(n);
                InspectableGraph other = e.getSource() == g1 ? g2 : g1;
                for (Edge edge : other.edges(n)) {
                    if (g.containsNode(edge.opposite(n))) {
                        g.adoptEdge(edge);
                    }
                }
            }
        }

        @Override public void edgeAdded(GraphEvent ev) {
            Edge e = ev.getEdge();
            int count = 0;
            if (g1.containsEdge(e)) {
                count++;
            }
            if (g2.containsEdge(e)) {
                count++;
            }
            if (count == 2) {
                g.removeEdge(e);
                return;
            }
            if (g.containsNode(e.n1()) && (g.containsNode(e.n2()))) {
                g.adoptEdge(e);
            }
        }

        @Override public void edgeRemoved(GraphEvent ev) {
            Edge e = ev.getEdge();
            if (g.containsEdge(e)) {
                g.removeEdge(e);
            } else {
                if (g.containsNode(e.n1()) && (g.containsNode(e.n2()))) {
                    g.adoptEdge(e);
                }
            }
        }
    }

    public static NodeListener attachNodeNamer(InspectableGraph g) {
        NodeListener listener = new EmptyGraphListener() {
            int id = 0;

            @Override
            public void nodeAdded(GraphEvent e) {
                if (e.getEventType() == GraphEvent.Type.NODE_REINSERTED) {
                    return;
                }
                e.getNode().setValue(id++);
            }
        };
        g.addNodeListener(listener);
        return listener;
    }
}

abstract class EmptyGraphListener implements GraphListener {
    public void preEvent() { }
    public void postEvent() { }

    public void nodeToBeAdded(GraphEvent e) { }
    public void nodeAdded(GraphEvent e) { }
    public void nodeToBeRemoved(GraphEvent e) { }
    public void nodeRemoved(GraphEvent e) { }
    public void nodeReordered(GraphEvent e) { }

    public void edgeToBeAdded(GraphEvent e) { }
    public void edgeAdded(GraphEvent e) { }
    public void edgeToBeRemoved(GraphEvent e) { }
    public void edgeRemoved(GraphEvent e) { }
    public void edgeReordered(GraphEvent e) { }
}

class InvertedInspectableGraph extends InspectableGraphForwarder {
    public InvertedInspectableGraph(InspectableGraph graph) {
        super(graph);
    }

    public ExtendedListIterable<Edge> edges(Node node, Direction direction) {
        return inspectableGraph.edges(node, direction.flip());
    }

    public ExtendedListIterable<Edge> edges(Node n1, Node n2, Direction direction) {
        return inspectableGraph.edges(n1, n2, direction.flip());
    }

    public boolean areAdjacent(Node n1, Node n2, Direction direction) {
        return inspectableGraph.areAdjacent(n1, n2, direction.flip());
    }

    public int inDegree(Node node) {
        return inspectableGraph.outDegree(node);
    }

    public int outDegree(Node node) {
        return inspectableGraph.inDegree(node);
    }

    public ExtendedListIterable<Node> adjacentNodes(Node node, Direction direction) {
        return inspectableGraph.adjacentNodes(node, direction.flip());
    }
}

class UndirectedInspectableGraph extends InspectableGraphForwarder {
    public UndirectedInspectableGraph(InspectableGraph graph) {
        super(graph);
    }

    public boolean areAdjacent(Node n1, Node n2) {
        return inspectableGraph.areAdjacent(n1, n2) || inspectableGraph.areAdjacent(n2, n1);
    }

    
    public boolean areAdjacent(Node n1, Node n2, Direction direction) {
        return areAdjacent(n1, n2);
    }

    public int inDegree(Node node) {
        return inspectableGraph.degree(node);
    }

    public int outDegree(Node node) {
        return inspectableGraph.degree(node);
    }

    public int degree(Node node) {
        return inspectableGraph.degree(node);
    }

    
    public ExtendedListIterable<Edge> edges(Node node, Direction direction) {
        return inspectableGraph.edges(node, Direction.EITHER);
    }

    public ExtendedListIterable<Edge> edges(Node n1, Node n2) {
        return inspectableGraph.edges(n1, n2);
    }

    
    public ExtendedListIterable<Edge> edges(Node n1, Node n2, Direction direction) {
        return inspectableGraph.edges(n1, n2, Direction.EITHER);
    }
}

class InspectableGraphForwarder extends AbstractInspectableGraph implements InspectableGraph, Serializable {
    protected final InspectableGraph inspectableGraph;
    private final GraphEventSupport eventSupport = new GraphEventSupport();
    protected NodeListener nodeListener;
    protected EdgeListener edgeListener;

    public InspectableGraphForwarder(InspectableGraph delegate) {
        Args.notNull(delegate);
        this.inspectableGraph = delegate;
    }

    public InspectableGraph getDelegateGraph() {
        return inspectableGraph;
    }

    private void lazyInitNodeListener() {
        if (nodeListener == null) {
            nodeListener = new TrampolineNodeListener(this, inspectableGraph);
            inspectableGraph.addNodeListener(nodeListener);
        }
    }

    private void lazyInitEdgeListener() {
        if (edgeListener == null) {
            edgeListener = new TrampolineEdgeListener(this, inspectableGraph);
            inspectableGraph.addEdgeListener(edgeListener);
        }
    }

    public void addGraphListener(GraphListener listener) {
        eventSupport.addGraphListener(listener);
        lazyInitNodeListener();
        lazyInitEdgeListener();
    }

    public void removeGraphListener(GraphListener listener) {
        eventSupport.removeGraphListener(listener);
    }

    public void addNodeListener(NodeListener listener) {
        eventSupport.addNodeListener(listener);
        lazyInitNodeListener();
    }

    public void removeNodeListener(NodeListener listener) {
        eventSupport.removeNodeListener(listener);
    }

    public void addEdgeListener(EdgeListener listener) {
        eventSupport.addEdgeListener(listener);
        lazyInitEdgeListener();
    }

    public void removeEdgeListener(EdgeListener listener) {
        eventSupport.removeEdgeListener(listener);
    }

    public List<NodeListener> getNodeListeners() {
        return eventSupport.getNodeListeners();
    }

    public List<EdgeListener> getEdgeListeners() {
        return eventSupport.getEdgeListeners();
    }

    public ExtendedListIterable<Node> adjacentNodes(Node node, Direction direction) {
        return inspectableGraph.adjacentNodes(node, direction);
    }

    public int nodeCount() {
        return inspectableGraph.nodeCount();
    }

    public int edgeCount() {
        return inspectableGraph.edgeCount();
    }

    public ExtendedListIterable<Node> nodes() {
        return inspectableGraph.nodes();
    }

    public ExtendedListIterable<Edge> edges(Node node, Direction direction) {
        return inspectableGraph.edges(node, direction);
    }

    public ExtendedListIterable<Edge> edges(Node n1, Node n2, Direction direction) {
        return inspectableGraph.edges(n1, n2, direction);
    }

    public boolean containsEdge(Edge edge) {
        return inspectableGraph.containsEdge(edge);
    }

    public boolean containsNode(Node node) {
        return inspectableGraph.containsNode(node);
    }

    public int inDegree(Node node) {
        return inspectableGraph.inDegree(node);
    }

    public int outDegree(Node node) {
        return inspectableGraph.outDegree(node);
    }

    public ExtendedListIterable<Edge> edges() {
        return inspectableGraph.edges();
    }

    private static abstract class Detached {
        private final WeakReference<InspectableGraphForwarder> ref;
        private final InspectableGraph target;
        private InspectableGraphForwarder gd;
        protected GraphEventSupport eventSupport;

        private int depth = 0;

        Detached(InspectableGraphForwarder gd, InspectableGraph target) {
            this.ref = new WeakReference<InspectableGraphForwarder>(gd);
            this.target = target;
        }

        protected abstract void removeSelf(InspectableGraph target);
        protected abstract void preEventImpl();
        protected abstract void postEventImpl();

        public final void preEvent() {
            depth++;
            gd = ref.get();
            if (gd == null) {
                removeSelf(target);
                return;
            }
            eventSupport = gd.eventSupport;
            preEventImpl();
        }

        public final void postEvent() {
            depth--;
            postEventImpl();
            if (depth == 0) {
                eventSupport = null;
                gd = null;
            }
        }

        protected GraphEvent deriveEvent(GraphEvent e) {
            return new GraphEvent(ref.get(), e.getEventType(), e.getData());
        }
    }

    private static class TrampolineNodeListener extends Detached implements NodeListener {
        TrampolineNodeListener(InspectableGraphForwarder gd, InspectableGraph source) {
            super(gd, source);
        }

        public void nodeToBeRemoved(GraphEvent e) {
            eventSupport.fireNodeToBeRemoved(deriveEvent(e));
        }

        public void nodeToBeAdded(GraphEvent e) {
            eventSupport.fireNodeToBeAdded(deriveEvent(e));
        }

        public void nodeRemoved(GraphEvent e) {
            eventSupport.fireNodeRemoved(deriveEvent(e));
        }

        public void nodeAdded(GraphEvent e) {
            eventSupport.fireNodeAdded(deriveEvent(e));
        }

        public void nodeReordered(GraphEvent e) {

        }

        protected void removeSelf(InspectableGraph target) {
            target.removeNodeListener(this);
        }

        protected void preEventImpl() {
            eventSupport.firePreNode();
        }

        protected void postEventImpl() {
            eventSupport.firePostNode();
        }
    }

    private static class TrampolineEdgeListener extends Detached implements EdgeListener {
        TrampolineEdgeListener(InspectableGraphForwarder gd, InspectableGraph source) {
            super(gd, source);
        }

        public void edgeToBeAdded(GraphEvent e) {
            eventSupport.fireEdgeToBeAdded(deriveEvent(e));
        }

        public void edgeReordered(GraphEvent e) {

        }

        public void edgeRemoved(GraphEvent e) {
            eventSupport.fireEdgeRemoved(deriveEvent(e));
        }

        public void edgeAdded(GraphEvent e) {
            eventSupport.fireEdgeAdded(deriveEvent(e));
        }

        public void edgeToBeRemoved(GraphEvent e) {
            eventSupport.fireEdgeToBeRemoved(deriveEvent(e));
        }

        protected void removeSelf(InspectableGraph target) {
            target.removeEdgeListener(this);
        }

        protected void preEventImpl() {
            eventSupport.firePreEdge();
        }

        protected void postEventImpl() {
            eventSupport.firePostEdge();
        }
    }
}

class Clusterers {
    private Clusterers() { }

    
    public static Clusterer connectedComponents(InspectableGraph g) {
        return ConnectedComponents.execute(g);
    }

    
    public static Clusterer stronglyConnectedComponents(InspectableGraph g) {
        return StronglyConnectedComponents.execute(g);
    }

    
    public static FoldingGraph fold(Clusterer clusterer, Graph graph) {
        Args.notNull(graph);
        FoldingGraph foldingGraph = new FoldingGraph(graph);
        for (Object cluster : clusterer.getClusters()) {
            foldingGraph.fold(clusterer.getCluster(cluster));
        }

        return foldingGraph;
    }
}

class FoldingGraph extends GraphForwarder {
    private final Object SUBGRAPH = new SerializableObject();
    private final Object REAL_EDGE = new SerializableObject();
    private final Object PARENT = new SerializableObject();
    private final Object KIDS = new SerializableObject();
    private final Factory<Graph> graphFactory;

    private final Graph graph;

    public FoldingGraph(Graph graph) {
        this(graph, true);
    }

    public FoldingGraph(Graph graph, boolean usePrimaries) {
        super(graph);
        this.graph = graph;
        if (usePrimaries) {
            graphFactory = new Factory<Graph>() {
                public Graph create(Object ignored) {
                    return new PrimaryGraph();
                }
            };
        } else {
            graphFactory = new Factory<Graph>() {
                public Graph create(Object ignored) {
                    return new SecondaryGraph();
                }
            };
        }
    }

    public FoldingGraph(Graph graph, Factory<Graph> graphFactory) {
        super(graph);
        Args.notNull(graphFactory);
        this.graph = graph;
        this.graphFactory = graphFactory;
    }

    public Node fold() {
        return fold(Collections.<Node>emptyList());
    }

    public Node fold(Node ... nodes) {
        Args.notNull((Object[])nodes);
        return fold(Arrays.asList(nodes));
    }

    public Node fold(Iterable<Node> nodes) {
        Args.notNull(nodes);
        Collection<Node> importedNodes = ExtendedIterable.wrap(nodes).drainToSet();
        Node folder = graph.newNode();
        Graph subgraph = graphFactory.create(null);
        folder.putWeakly(PARENT, null);

        Collection<Node> kids = getKids(folder);
        for (Node n : importedNodes) {
            n.putWeakly(PARENT, folder);
            if (isFolder(n)) {
                kids.add(n);
            }
        }
        folder.putWeakly(SUBGRAPH, subgraph);
        Collection<Edge> interedges = subgraph.importGraph(graph, importedNodes);
        for (Edge interedge : interedges) {
            Edge syntheticEdge = graph.newEdge(
                    subgraph.containsNode(interedge.n1()) ? folder : interedge.n1(),
                    subgraph.containsNode(interedge.n2()) ? folder : interedge.n2()
            );
            syntheticEdge.putWeakly(REAL_EDGE, getRealEdge(interedge));
        }
        return folder;
    }

    public boolean isSyntheticEdge(Edge e) {
        if (e == null) {
            return false;
        }
        return e.has(REAL_EDGE);
    }

    public Edge getRealEdge(Edge e) {
        Args.notNull(e);
        if (!isSyntheticEdge(e)) {
            return e;
        }
        return (Edge)e.get(REAL_EDGE);
    }

    public boolean isFolder(Node node) {
        return node.has(SUBGRAPH);
    }

    public Graph viewFolder(Node folder) {
        if (folder == null) {
            return graph;
        }
        checkFolder(folder);
        return (Graph)folder.get(SUBGRAPH);
    }

    public void unfold(Node folder) {
        checkFolder(folder);
        Graph subgraph = folder.getGraph(SUBGRAPH);
        Node parent = folder.getNode(PARENT);
        Graph parentGraph = parent != null ? parent.getGraph(SUBGRAPH) : graph;

        Collection<Edge> syntheticEdges = parentGraph.edges(folder).drainToSet();
        Collection<Node> importedNodes = subgraph.nodes().drainToList();
        parentGraph.removeEdges(syntheticEdges);
        parentGraph.importGraph(subgraph);
        folder.remove(SUBGRAPH);
        parentGraph.removeNode(folder);
        for (Edge syntheticEdge : syntheticEdges) {
            if (isSyntheticEdge(syntheticEdge)) {
                Direction direction = syntheticEdge.n1() == folder ? Direction.OUT : Direction.IN;
                Edge realEdge = getRealEdge(syntheticEdge);

                Node searchNode = direction == Direction.OUT ? realEdge.n1() : realEdge.n2();
                Node constantNode = direction == Direction.OUT ? syntheticEdge.n2() : syntheticEdge.n1();
                Node unfoldedParent = searchNode.getNode(PARENT);
                while (unfoldedParent != folder) {
                    searchNode = unfoldedParent;
                    unfoldedParent = searchNode.getNode(PARENT);
                }
                if (!parentGraph.containsNode(searchNode)) {
                    continue;
                }

                if (realEdge.isIncident(searchNode) && realEdge.isIncident(constantNode)) {

                    parentGraph.reinsertEdge(realEdge);
                } else {
                    Edge newEdge = direction == Direction.OUT ?
                            parentGraph.newEdge(searchNode, constantNode) :
                            parentGraph.newEdge(constantNode, searchNode);
                    newEdge.putWeakly(REAL_EDGE, realEdge);
                }
            }
        }

        Collection<Node> kids = getKids(folder);
        if (parent == null) {
            for (Node kid : kids) {
                kid.remove(PARENT);
            }
        } else {
            for (Node kid : kids) {
                if (isFolder(kid)) {
                    kid.putWeakly(PARENT, parent);
                }
            }
        }

        if (parent != null) {
            getKids(parent).remove(folder);
        }

        for (Node importedNode : importedNodes) {
            importedNode.putWeakly(PARENT, parent);
        }
    }

    public Node getParent(Node node) {
        Args.notNull(node);
        return node.getNode(PARENT);
    }

    private void checkFolder(Node folder) {
        if (!isFolder(folder)) {
            throw new IllegalArgumentException("Not a folder node");
        }
    }

    @SuppressWarnings("unchecked")
    private Collection<Node> getKids(Node parent) {
        Collection<Node> kids = (Collection<Node>)parent.get(KIDS);
        if (kids == null) {
            kids = new HashSet<Node>();
            parent.putWeakly(KIDS, kids);
        }
        return kids;
    }
}

class GraphForwarder extends InspectableGraphForwarder implements Graph, Serializable {
    private final Graph graph;

    public GraphForwarder(Graph graph) {
        super(graph);
        this.graph = graph;
    }

    @Override
    public Graph getDelegateGraph() {
        return graph;
    }

    public Edge newEdge(Node node1, Node node2) {
        return newEdge(node1, node2, null);
    }

    public Edge newEdge(Node node1, Node node2, Object value) {
        return graph.newEdge(node1, node2, value);
    }

    public boolean removeEdge(Edge edge) {
        return graph.removeEdge(edge);
    }

    public int removeEdges(Iterable<Edge> edges) {
        if (edges == null) {
            return 0;
        }
        int count = 0;
        for (Edge e : edges) {
            if (removeEdge(e)) {
                count++;
            }
        }
        return count;
    }

    public int removeAllEdges() {
        return removeEdges(edges());
    }

    public Node newNode() {
        return newNode(null);
    }

    public Node newNode(Object value) {
        return graph.newNode(value);
    }

    public Node[] newNodes(int count) {
        Args.gte(count, 0);
        Node[] n = new Node[count];
        for (int i = 0; i < n.length; i++) {
            n[i] = newNode(null);
        }
        return n;
    }

    public Node[] newNodes(Object ... values) {
        Node[] n = new Node[values.length];
        for (int i = 0; i < n.length; i++) {
            n[i] = newNode(values[i]);
        }
        return n;
    }

    public boolean removeNode(Node node) {
        return graph.removeNode(node);
    }

    public int removeNodes(Iterable<Node> nodes) {
        if (nodes == null) {
            return 0;
        }
        int count = 0;
        for (Node n : nodes) {
            if (removeNode(n)) {
                count++;
            }
        }
        return count;
    }

    public int removeAllNodes() {
        return removeNodes(nodes());
    }

    public boolean isPrimary() {
        return graph.isPrimary();
    }

    public boolean reinsertNode(Node n) {
        return graph.reinsertNode(n);
    }

    public boolean reinsertEdge(Edge e) {
        return graph.reinsertEdge(e);
    }

    public void importGraph(Graph g) {
        graph.importGraph(g);
    }

    public Collection<Edge> importGraph(Graph g, Iterable<Node> nodes) {
        return graph.importGraph(g, nodes);
    }

    public OrderManager getOrderManager() {
        final OrderManager om = graph.getOrderManager();
        return new OrderManager() {
            public void moveNodeToFront(Node node) {
                om.moveNodeToFront(node);
            }

            public void moveNodeToBack(Node node) {
                om.moveNodeToBack(node);
            }

            public void moveNodeBefore(Node node, Node beforeWhat) {
                om.moveNodeBefore(node, beforeWhat);
            }

            public void moveNodeAfter(Node node, Node afterWhat) {
                om.moveNodeAfter(node, afterWhat);
            }

            public void moveEdgeToFront(Edge edge, boolean onSourceNode) {
                om.moveEdgeToFront(edge, onSourceNode);
            }

            public void moveEdgeToBack(Edge edge, boolean onSourceNode) {
                om.moveEdgeToBack(edge, onSourceNode);
            }

            public void moveEdgeBefore(Edge edge, boolean onSourceNode, Edge beforeWhat) {
                om.moveEdgeBefore(edge, onSourceNode, beforeWhat);
            }

            public void moveEdgeAfter(Edge edge, boolean onSourceNode, Edge afterWhat) {
                om.moveEdgeAfter(edge, onSourceNode, afterWhat);
            }
        };
    }
}

class StronglyConnectedComponents implements Clusterer {
    private final LinkedList<Node> stack = new LinkedList<Node>();
    private final List<Collection<Node>> components = new ArrayList<Collection<Node>>();
    private int indexCounter = 0;
    private int componentCounter = 0;

    private final InspectableGraph graph;

    private StronglyConnectedComponents(InspectableGraph graph) {
        Args.notNull(graph);
        this.graph = graph;
    }

    private final Object DATA = new Object();

    static StronglyConnectedComponents execute(InspectableGraph g) {
        return new StronglyConnectedComponents(g).execute();
    }

    private StronglyConnectedComponents execute() {
        for (Node n : graph.nodes()) {
            if (!n.has(DATA)) {
                visit(n);
            }
        }
        return this;
    }

    private void visit(Node u) {
        NodeData uData = new NodeData();
        u.putWeakly(DATA, uData);
        uData.root = indexCounter;
        uData.index = indexCounter;
        indexCounter++;
        stack.addLast(u);
        for (Edge e : graph.edges(u, Direction.OUT)) {
            if (e.has(DATA)) continue;
            e.putWeakly(DATA, null);
            Node w = e.opposite(u);
            if (!w.has(DATA)) {
                visit(w);
            }
            NodeData wData = (NodeData)w.get(DATA);
            if (wData.component == null) {
                uData.root = Math.min(uData.root, wData.root);
            }
        }
        if (uData.root == uData.index) {
            Object componentId = componentCounter++;
            Collection<Node> component = new ArrayList<Node>();
            Node w;
            do {
                w = stack.removeLast();
                ((NodeData)w.get(DATA)).component = componentId;
                component.add(w);
            } while (w != u);
            components.add(component);
        }
    }

    public Collection<Object> getClusters() {
        return new AbstractList<Object>() {
            @Override
            public Object get(int index) {
                if (index < 0 || index >= size()) {
                    throw new IndexOutOfBoundsException("Invalid index: " + index + ", size: " + size());
                }
                return Integer.valueOf(index);
            }

            @Override
            public int size() {
                return componentCounter;
            }
        };
    }

    public Object findClusterOf(Node node) {
        NodeData data = (NodeData)node.get(DATA);
        if (data == null) {
            return null;
        }
        return data.component;
    }

    public Collection<Node> getCluster(Object key) {
        if (!(key instanceof Integer)) {
            return Collections.<Node>emptySet();
        }
        int componentId = (Integer)key;
        if (componentId < 0 || componentId >= components.size()) {
            return Collections.<Node>emptySet();
        }
        return components.get(componentId);
    }

    public InspectableGraph getGraph() {
        return graph;
    }

    public Iterator<Collection<Node>> iterator() {
        return Collections.unmodifiableList(components).iterator();
    }

    @Override
    public String toString() {
        return "[Components: " + components + "]";
    }

    private static class NodeData {
        int root;
        int index;
        Object component;
    }
}

class ConnectedComponents extends Dfs implements Clusterer {
    private final Map<Object, Collection<Node>> components
            = DVMap.newLinkedHashMapWithLinkedLists();
    private final Object MARK = new Object();

    private ConnectedComponents(InspectableGraph graph) {
        super(graph, Direction.EITHER);
    }

    private ConnectedComponents(InspectableGraph graph, Node startNode) {
        super(graph, startNode, Direction.EITHER);
    }

    static ConnectedComponents execute(InspectableGraph g) {
        ConnectedComponents cc = new ConnectedComponents(g);
        cc.execute();
        return cc;
    }

    static ConnectedComponents execute(InspectableGraph g, Node startNode) {
        ConnectedComponents cc = new ConnectedComponents(Graphs.undirected(g), startNode);
        cc.execute();
        return cc;
    }

    public Iterator<Collection<Node>> iterator() {
        return Collections.unmodifiableMap(components).values().iterator();
    }

    public Collection<Node> getCluster(Object cluster) {
        if (!components.containsKey(cluster)) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableCollection(components.get(cluster));
    }

    public Set<Object> getClusters() {
        return Collections.unmodifiableSet(components.keySet());
    }

    @Override public boolean visitPre(Path path) {
        Node node = path.tailNode();
        Object tree = getComponentIdentifier(node);
        Collection<Node> component = components.get(tree);
        component.add(node);
        node.putWeakly(MARK, tree);
        return false;
    }

    public Object findClusterOf(Node node) {
        return getComponentIdentifier(node);
    }

    @Override
    public String toString() {
        return "[Components: " + components.values() + "]";
    }
}

class DVMap<K, V> implements Map<K, V> {
    private final Map<K, V> delegate;
    private final Factory<V> factory;

    public DVMap(Map<K, V> delegate, Factory<V> factory) {
        if (delegate == null) {
            throw new IllegalArgumentException("argument is null");
        }
        this.delegate = delegate;
        this.factory = factory;
    }

    public DVMap(Factory<V> factory) {
        this(new HashMap<K, V>(), factory);
    }

    public DVMap(final Copyable<? extends V> defaultValue) {
        this(new Factory<V>() {
            public V create(Object o) {
                return defaultValue.copy();
            }
        });
    }

    public DVMap(final V defaultValue) {
        this(new Factory<V>() {
            public V create(Object o) {
                return defaultValue;
            }
        });
    }

    public void clear() {
        delegate.clear();
    }

    
    public Map<K, V> getDelegate() {
        return delegate;
    }

    public boolean containsKey(Object key) {
        return delegate.containsKey(key);
    }

    public boolean containsValue(Object value) {
        return delegate.containsValue(value);
    }

    public Set<Entry<K, V>> entrySet() {
        return delegate.entrySet();
    }

    @SuppressWarnings("unchecked")
    public V get(Object key) {
        if (!delegate.containsKey(key)) {
            V value = factory.create(key);
            delegate.put((K)key, value);
            return value;
        }
        return delegate.get(key);
    }

    public V getIfExists(Object key) {
        return delegate.get(key);
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public Set<K> keySet() {
        return delegate.keySet();
    }

    public V put(K key, V value) {
        return delegate.put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> map) {
        delegate.putAll(map);
    }

    public V remove(Object key) {
        V v = delegate.remove(key);
        if (v == null) {
            return factory.create(key);
        }
        return v;
    }

    public int size() {
        return delegate.size();
    }

    public Collection<V> values() {
        return delegate.values();
    }

    @Override
    public String toString() {
        return delegate.toString();
    }

    public static <K, V> DVMap<K, Collection<V>> newHashMapWithLinkedLists() {
        return new DVMap<K, Collection<V>>(
                new HashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new LinkedList<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newHashMapWithArrayLists() {
        return new DVMap<K, Collection<V>>(
                new HashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new ArrayList<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newHashMapWithHashSets() {
        return new DVMap<K, Collection<V>>(
                new HashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new HashSet<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newHashMapWithTreeSets(final Comparator<V> c) {
        return new DVMap<K, Collection<V>>(
                new HashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new TreeSet<V>(c);
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newHashMapWithTreeSets() {
        return new DVMap<K, Collection<V>>(
                new HashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new TreeSet<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newTreeMapWithLinkedLists() {
        return new DVMap<K, Collection<V>>(
                new TreeMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new LinkedList<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newTreeMapWithArrayLists() {
        return new DVMap<K, Collection<V>>(
                new TreeMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new ArrayList<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newTreeMapWithHashSets() {
        return new DVMap<K, Collection<V>>(
                new TreeMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new HashSet<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newTreeMapWithTreeSets(final Comparator<V> c) {
        return new DVMap<K, Collection<V>>(
                new TreeMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new TreeSet<V>(c);
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newTreeMapWithTreeSets() {
        return new DVMap<K, Collection<V>>(
                new TreeMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new TreeSet<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newLinkedHashMapWithLinkedLists() {
        return new DVMap<K, Collection<V>>(
                new LinkedHashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new LinkedList<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newLinkedHashMapWithArrayLists() {
        return new DVMap<K, Collection<V>>(
                new LinkedHashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new ArrayList<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newLinkedHashMapWithHashSets() {
        return new DVMap<K, Collection<V>>(
                new LinkedHashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new HashSet<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newLinkedHashMapWithLinkedHashSets() {
        return new DVMap<K, Collection<V>>(
                new HashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new LinkedHashSet<V>();
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newLinkedHashMapWithTreeSets(final Comparator<V> c) {
        return new DVMap<K, Collection<V>>(
                new LinkedHashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new TreeSet<V>(c);
                    }
                });
    }

    public static <K, V> DVMap<K, Collection<V>> newLinkedHashMapWithTreeSets() {
        return new DVMap<K, Collection<V>>(
                new LinkedHashMap<K, Collection<V>>(),
                new Factory<Collection<V>>() {
                    public Collection<V> create(Object o) {
                        return new TreeSet<V>();
                    }
                });
    }
}

interface Factory<V> {
    V create(Object o);
}

final class PrimaryGraph extends AbstractListGraph {
    private static final long serialVersionUID = -4941194412829796815L;

    @Override final FastLinkedList<EdgeImpl> getOutEdges(NodeImpl node) {
        return node.outEdges;
    }

    @Override final FastLinkedList<EdgeImpl> getInEdges(NodeImpl node) {
        return node.inEdges;
    }

    @Override final Accessor<NodeImpl> getNodeRef(NodeImpl node) {
        return node.reference;
    }

    @Override final Accessor<EdgeImpl> getEdgeOutRef(EdgeImpl edge) {
        return edge.outReference;
    }

    @Override final Accessor<EdgeImpl> getEdgeInRef(EdgeImpl edge) {
        return edge.inReference;
    }

    @Override final void removeEdgeRefs(EdgeImpl edge) {
        edge.inReference = null;
        edge.outReference = null;
    }

    @Override final void setNodeRef(NodeImpl node, Accessor<NodeImpl> ref) {
        node.reference = ref;
    }

    @Override final void setEdgeOutRef(EdgeImpl edge, Accessor<EdgeImpl> ref) {
        edge.outReference = ref;
    }

    @Override final void setEdgeInRef(EdgeImpl edge, Accessor<EdgeImpl> ref) {
        edge.inReference = ref;
    }

    @Override final void removeNodeRef(NodeImpl node) {
        node.reference = null;
    }

    @Override final void initNode(NodeImpl node) { }

    @Override public final boolean isPrimary() {
        return true;
    }

    public boolean reinsertEdge(Edge e) {
        final EdgeImpl edge = (EdgeImpl)e;
        if (getEdgeInRef(edge) != null) {
            if (containsEdge(e)) {
                return false;
            }
            throw new IllegalArgumentException("Edge must not belong to any graph, to be reinserted to one");
        }
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.EDGE_REINSERTED, edge), new Runnable() {
            public void run() {
                if (!containsNode(edge.n1)) {
                    reinsertNode(edge.n1);
                }
                if (!containsNode(edge.n2)) {
                    reinsertNode(edge.n2);
                }
                setEdgeOutRef(edge, edge.n1.outEdges.addLast(edge));
                setEdgeInRef(edge, edge.n2.inEdges.addLast(edge));
                edgeCount++;
            }
        });
        return true;
    }

    public boolean reinsertNode(Node n) {
        final NodeImpl node = (NodeImpl)n;
        if (getNodeRef(node) != null) {
            if (containsNode(n)) {
                return false;
            }
            throw new IllegalArgumentException("Node must not belong to any graph, to be reinserted to one");
        }
        graphEventSupport.fire(new GraphEvent(this, GraphEvent.Type.NODE_REINSERTED, node), new Runnable() {
            public void run() {
                setNodeRef(node, nodes.addLast(node));
            }
        });
        return true;
    }
}

interface Copyable<T> {
    T copy();
}

class DefaultPath extends AbstractPath {
    private final Node head;
    private final Node tail;
    private final Path left;
    private final Path right;
    private final int size;

    private DefaultPath(Path left, Path right) {
        if (left.tailNode() != right.headNode()) {
            throw new IllegalArgumentException("Paths: " + left + ", " + right + " are not consequtive.");
        }
        this.left = left;
        this.right = right;
        this.head = left.headNode();
        this.tail = right.tailNode();
        this.size = left.size() + right.size();
    }

    public Node headNode() {
        return head;
    }

    public Node tailNode() {
        return tail;
    }

    public Edge headEdge() {
        if (left.edgeCount() > 0) {
            return left.headEdge();
        }
        return right.headEdge();
    }

    public Edge tailEdge() {
        if (right.edgeCount() == 0) {
            return left.tailEdge();
        }
        return right.tailEdge();
    }

    public ExtendedIterable<Path> steps() {
        return new ExtendedIterable<Path>(new Iterable<Path>() {
            public Iterator<Path> iterator() {
                return new CompoundIterator<Path>(
                        left.steps().iterator(),
                        right.steps().iterator());
            }
        });
    }

    public Path append(Path other) {
        return newPath(this, other);
    }

    public int size() {
        return size;
    }

    private int maskNegative(int index, int size) {
        if (index < 0) {
            index += size;
            if (index < 0) throw new IndexOutOfBoundsException("Illegal index: " + index
                    + ", valid range: [" + -size + "..." + size + "]");
        }
        return index;
    }

    public Node getNode(int index) {
        index = maskNegative(index, size + 1);
        if (index < left.nodeCount()) {
            return left.getNode(index);
        }
        return right.getNode(index - left.edgeCount());
    }

    public Edge getEdge(int index) {
        index = maskNegative(index, size);
        if (index < 0) {
            index = index % edgeCount();
            if (index < 0) index += edgeCount();
        }
        if (index < left.edgeCount()) {
            return left.getEdge(index);
        }
        return right.getEdge(index - left.edgeCount());
    }

    public Path headPath(int steps) {
        if (steps == edgeCount()) {
            return this;
        } else if (steps <= left.edgeCount()) {
            return left.headPath(steps);
        }
        return newPath(left, right.headPath(steps - left.edgeCount()));
    }

    public Path tailPath(int steps) {
        if (steps == edgeCount()) {
            return this;
        } else if (steps <= right.edgeCount()) {
            return right.tailPath(steps);
        }
        return newPath(left.tailPath(steps - right.edgeCount()), right);
    }

    public Path slice(int start, int end) {
        if (end <= left.edgeCount()) {
            return left.slice(start, end);
        } else if (start >= left.edgeCount()) {
            return right.slice(start - left.edgeCount(), end - left.edgeCount());
        } else {
            if (start == 0 && end == nodeCount()) {
                return this;
            }
            return newPath(
                    left.tailPath(left.edgeCount() - start),
                    right.headPath(end - left.edgeCount()));
        }
    }

    public Path reverse() {
        List<Path> list = steps().drainToList();
        Collections.reverse(list);
        for (int i = 0; i < list.size(); i++) {
            list.set(i, list.get(i).reverse());
        }
        return Paths.newPath(list);
    }

    static Path newPath(Path left, Path right) {
        if (right.headNode() != left.tailNode()) {
            throw new IllegalArgumentException("Paths: " + left + " and " + right + " are not consequtive");
        }
        if (left.edgeCount() == 0) {
            return right;
        } else if (right.edgeCount() == 0) {
            return left;
        }
        return new DefaultPath(left, right);
    }
}

class Paths {
    private  Paths() { }

    
    public static Path newPath(Iterable<Path> paths) {
        LinkedList<Path> list = new LinkedList<Path>();
        for (Path p : paths) {
            list.add(p);
        }
        if (list.isEmpty()) {
            throw new IllegalArgumentException("Empty paths given");
        }
        while (list.size() > 1) {
            for (ListIterator<Path> i = list.listIterator(); i.hasNext(); ) {
                Path p1 = i.next();
                if (!i.hasNext()) {
                    break;
                }
                i.remove();
                Path p2 = i.next();
                i.set(p1.append(p2));
            }
        }
        return list.get(0);
    }


    
    public static Path findPath(InspectableGraph graph, Node start, Node target, Direction direction) {
        for (Path path : Traverser.newDfs().notRepeatingEdges().build().traverse(graph, start, direction)) {
            if (path.tailNode() == target) {
                return path;
            }
        }
        return null;
    }
}

class CompoundIterator<E> extends AbstractCompoundIterator<E> {
    private final Iterator<Iterator<E>> iteratorsIterator;

    public CompoundIterator(Iterator<E> i1, Iterator<E> i2) {
        this(i1, i2, null);
    }

    
    public CompoundIterator(Iterator<E> i1, Iterator<E> i2,
                            IteratorRemoveStrategy<E> removeStrategy) {
        super(removeStrategy);
        List<Iterator<E>> iterators = new ArrayList<Iterator<E>>(2);
        if (i1 != null) {
            iterators.add(i1);
        }
        if (i2 != null) {
            iterators.add(i2);
        }
        iteratorsIterator = iterators.iterator();
    }

    public CompoundIterator(List<Iterator<E>> iterators) {
        this(iterators, null);
    }

    public CompoundIterator(List<Iterator<E>> iterators,
                            IteratorRemoveStrategy<E> removeStrategy) {
        super(removeStrategy);
        iteratorsIterator = iterators.iterator();
    }

    protected Iterator<E> nextIterator() {
        return iteratorsIterator.next();
    }

    protected boolean hasNextIterator() {
        return iteratorsIterator.hasNext();
    }
}

interface IteratorRemoveStrategy<E> {
    void removed(E element);
}

abstract class AbstractCompoundIterator<E> implements Iterator<E> {
    private Iterator<E> currentIterator;
    private IteratorRemoveStrategy<E> removeStrategy;
    protected E last;

    protected abstract Iterator<E> nextIterator();
    protected abstract boolean hasNextIterator();

    public AbstractCompoundIterator() {
        this(null);
    }

    public AbstractCompoundIterator(IteratorRemoveStrategy<E> removeStrategy) {
        this.removeStrategy = removeStrategy;
    }

    private void proceed() {
        if (currentIterator == null) {
            if (hasNextIterator()) {
                currentIterator = nextIterator();
            } else {
                return;
            }
        }
        if (!currentIterator.hasNext()) {
            currentIterator = null;
            while (hasNextIterator()) {
                currentIterator = nextIterator();
                if (currentIterator.hasNext()) {
                    return;
                } else {
                    currentIterator = null;
                }
            }
        }
    }

    public boolean hasNext() {
        proceed();
        return currentIterator != null;
    }

    public E next() {
        proceed();
        return last = currentIterator.next();
    }

    public void remove() {
        currentIterator.remove();
        if (removeStrategy != null) {
            removeStrategy.removed(last);
        }
    }
}

class Traverser {
    private final Factory<PathQueue> pathQueueFactory;
    private final Filter<Path> filter;

    private Traverser(Factory<PathQueue> pathQueueFactory, Filter<Path> filter) {
        this.pathQueueFactory = pathQueueFactory;
        this.filter = filter;
    }

    
    public PathIterable traverse(
            final InspectableGraph graph,
            final Node startNode,
            final Direction direction) {
        return new PathIterable() {
            public PathIterator iterator() {
                return new PathIterator() {
                    final PathQueue queue = pathQueueFactory.create(null); {
                        addPathIfValid(startNode.asPath());
                    }

                    Path toExpand = null;

                    private void expand() {
                        if (toExpand != null) {
                            for (Edge e : graph.edges(toExpand.tailNode(), direction)) {
                                addPathIfValid(toExpand.append(e.asPath(toExpand.tailNode())));
                            }
                        }
                        toExpand = null;
                    }

                    public void skipExplorationOfLastPath() {
                        if (toExpand == null) {
                            throw new IllegalStateException("No path was returned, or this method has been called " +
                                    "twice before accessing another path via next()");
                        }
                        toExpand = null;
                    }

                    public boolean hasNext() {
                        expand();
                        return queue.hasNext();
                    }

                    public Path next() {
                        expand();
                        if (!queue.hasNext()) {
                            throw new NoSuchElementException();
                        }
                        return toExpand = queue.poll();
                    }

                    private void addPathIfValid(Path path) {
                        if (filter.accept(path)) {
                            queue.addPath(path);
                        }
                    }

                    public void remove() {
                        throw new UnsupportedOperationException();
                    }
                };
            }
        };
    }

    
    public static TraverserBuilder newDfs() {
        return new TraverserBuilder(new Factory<PathQueue>() {
            public PathQueue create(Object o) {
                return new LIFO();
            }
        });
    }

    
    public static TraverserBuilder newBfs() {
        return new TraverserBuilder(new Factory<PathQueue>() {
            public PathQueue create(Object o) {
                return new FIFO();
            }
        });
    }

    
    public static TraverserBuilder newCustom(final Comparator<? super Path> pathComparator) {
        if (pathComparator == null) {
            throw new NullPointerException("pathComparator");
        }
        return new TraverserBuilder(new Factory<PathQueue>() {
            public PathQueue create(Object o) {
                return new PriorityQueue(pathComparator);
            }
        });
    }

    
    public static class TraverserBuilder {
        private final Factory<PathQueue> pathQueueFactory;
        private boolean uniqueNodes;
        private boolean excludingStart;

        private boolean uniqueEdges;

        private TraverserBuilder(Factory<PathQueue> pathQueueFactory) {
            this.pathQueueFactory = pathQueueFactory;
        }

        
        public TraverserBuilder notRepeatingNodes() {
            if (uniqueNodes) {
                throw new IllegalStateException("Cannot set withoutRepeatingNodes twice");
            }
            uniqueNodes = true;
            return this;
        }

        
        public TraverserBuilder notRepeatingNodesExcludingStart() {
            notRepeatingNodes();
            excludingStart = true;
            return this;
        }

        
        public TraverserBuilder notRepeatingEdges() {
            if (uniqueEdges) {
                throw new IllegalStateException("Cannot set withoutRepeatingEdges twice");
            }
            uniqueEdges = true;
            return this;
        }

        
        public Traverser build() {
            Filter<Path> filter = null;
            if (uniqueNodes) {
                if (excludingStart) {
                    filter = new NoDuplicateNodeExcludingStartFilter();
                } else {
                    filter = new NoDuplicateNodeFilter();
                }
            }
            if (uniqueEdges) {
                Filter<Path> edgeFilter = new NoDuplicateEdgeFilter();
                if (filter == null) {
                    filter = edgeFilter;
                } else {
                    filter = Filters.and(filter, edgeFilter);
                }
            }
            if (filter == null) {
                filter = Filters.alwaysTrue();
            }
            return new Traverser(pathQueueFactory, filter);
        }
    }

    private interface PathQueue {
        void addPath(Path path);
        boolean hasNext();
        Path poll();
    }

    private static abstract class AbstractQueue implements PathQueue {
        protected final Deque<Path> deque = new ArrayDeque<Path>();

        public Path poll() {
            return deque.pollFirst();
        }

        public boolean hasNext() {
            return !deque.isEmpty();
        }
    }

    private static class FIFO extends AbstractQueue {
        public void addPath(Path path) {
            deque.addLast(path);
        }
    }

    private static class LIFO extends AbstractQueue {
        public void addPath(Path path) {
            deque.addFirst(path);
        }
    }

    private static class PriorityQueue implements PathQueue {
        private final Comparator<? super Path> comparator;
        private final java.util.PriorityQueue<Path> queue;

        PriorityQueue(Comparator<? super Path> comparator) {
            this.comparator = comparator;
            this.queue = new java.util.PriorityQueue<Path>(11, comparator);
        }

        public void addPath(Path path) {
            queue.add(path);
        }

        public Path poll() {
            return queue.poll();
        }

        public boolean hasNext() {
            return !queue.isEmpty();
        }
    }

    private static abstract class NoDuplicateTupleFilter implements Filter<Path> {
        private final Object marked = new Object();

        protected boolean acceptAndMark(Tuple tuple) {
            if (tuple.has(marked)) {
                return false;
            }
            tuple.putWeakly(marked, null);
            return true;
        }
    }

    private static class NoDuplicateNodeFilter extends NoDuplicateTupleFilter {
        public boolean accept(Path path) {
            return acceptAndMark(path.tailNode());
        }
    }

    private static class NoDuplicateNodeExcludingStartFilter extends NoDuplicateNodeFilter {
        public boolean accept(Path path) {
            if (path.size() == 0) {
                return true;
            }
            return super.accept(path);
        }
    }

    private static class NoDuplicateEdgeFilter extends NoDuplicateTupleFilter {
        public boolean accept(Path path) {
            if (path.edgeCount() == 0) {
                return true;
            }
            return acceptAndMark(path.tailEdge());
        }
    }

    
    public interface PathIterable extends Iterable<Path> {
        PathIterator iterator();
    }

    
    public interface PathIterator extends Iterator<Path> {

        
        void skipExplorationOfLastPath();
    }
}

class Filters {
    private Filters() { }

    public static Filter<Node> degreeEqual(final InspectableGraph graph, final Direction direction, final int degree) {
        Args.notNull(graph, direction);
        return new Filter<Node>() {
            public boolean accept(Node n) {
                return graph.degree(n, direction) == degree;
            }
        };
    }

    public static Filter<Node> inDegreeEqual(InspectableGraph graph, int degree) {
        return degreeEqual(graph, Direction.IN, degree);
    }

    public static Filter<Node> outDegreeEqual(InspectableGraph graph, int degree) {
        return degreeEqual(graph, Direction.OUT, degree);
    }

    public static Filter<Node> degreeEqual(InspectableGraph graph, int degree) {
        return degreeEqual(graph, Direction.EITHER, degree);
    }

    public static Filter<Node> degreeAtLeast(final InspectableGraph graph, final Direction direction, final int degree) {
        Args.notNull(graph, direction);
        return new Filter<Node>() {
            public boolean accept(Node n) {
                return graph.degree(n, direction) >= degree;
            }
        };
    }

    public static Filter<Node> inDegreeAtLeast(InspectableGraph graph, int degree) {
        return degreeAtLeast(graph, Direction.IN, degree);
    }

    public static Filter<Node> outDegreeAtLeast(InspectableGraph graph, int degree) {
        return degreeAtLeast(graph, Direction.OUT, degree);
    }

    public static Filter<Node> degreeAtLeast(InspectableGraph graph, int degree) {
        return degreeAtLeast(graph, Direction.EITHER, degree);
    }

    public static Filter<Node> degreeAtMost(final InspectableGraph graph, final Direction direction, final int degree) {
        Args.notNull(graph, direction);
        return new Filter<Node>() {
            public boolean accept(Node n) {
                return graph.degree(n, direction) <= degree;
            }
        };
    }

    public static Filter<Node> inDegreeAtMost(InspectableGraph graph, int degree) {
        return degreeAtMost(graph, Direction.IN, degree);
    }

    public static Filter<Node> outDegreeAtMost(InspectableGraph graph, int degree) {
        return degreeAtMost(graph, Direction.OUT, degree);
    }

    public static Filter<Node> degreeAtMost(InspectableGraph graph, int degree) {
        return degreeAtMost(graph, Direction.EITHER, degree);
    }

    public static <T> Filter<T> not(final Filter<? super T> f) {
        Args.notNull(f);
        return new Filter<T>() {
            public boolean accept(T element) {
                return !f.accept(element);
            }
        };
    }

    public static <T> Filter<T> or(final Filter<? super T> f1, final Filter<? super T> f2) {
        Args.notNull(f1, f2);
        return new Filter<T>() {
            public boolean accept(T element) {
                return f1.accept(element) || f2.accept(element);
            }
        };
    }

    public static <T> Filter<T> xor(final Filter<? super T> f1, final Filter<? super T> f2) {
        Args.notNull(f1, f2);
        return new Filter<T>() {
            public boolean accept(T element) {
                return f1.accept(element) ^ f2.accept(element);
            }
        };
    }

    public static <T> Filter<T> and(final Filter<? super T> f1, final Filter<? super T> f2) {
        Args.notNull(f1, f2);
        return new Filter<T>() {
            public boolean accept(T element) {
                return f1.accept(element) && f2.accept(element);
            }
        };
    }

    public static <T extends Tuple> Filter<T> equalProperty(final Object key, final Object value) {
        return new Filter<T>() {
            public boolean accept(T element) {
                if (value == null) {
                    return element.has(key) && element.get(key) == null;
                } else {
                    return value.equals(element.get(key));
                }
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> Filter<T> alwaysTrue() {
        return (Filter<T>)alwaysTrue;
    }

    @SuppressWarnings("unchecked")
    public static <T> Filter<T> alwaysFalse() {
        return (Filter<T>)alwaysFalse;
    }

    private static final Filter<Object> alwaysTrue = new Filter<Object>() {
        public boolean accept(Object o) {
            return true;
        }
    };

    private static final Filter<Object> alwaysFalse = new Filter<Object>() {
        public boolean accept(Object o) {
            return false;
        }
    };
}
