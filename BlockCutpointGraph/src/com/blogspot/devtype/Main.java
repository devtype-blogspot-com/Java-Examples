package com.blogspot.devtype;

import java.io.Serializable;
import java.util.*;

public class Main {
    public static void test(String[] args) {
        UndirectedGraph<Integer, DefaultEdge> g = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        ArrayList<Integer> vs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            vs.add(i);
            g.addVertex(vs.get(i));
        }
        g.addEdge(vs.get(0), vs.get(1));
        g.addEdge(vs.get(1), vs.get(2));
        g.addEdge(vs.get(2), vs.get(0));
        g.addEdge(vs.get(3), vs.get(2));
        g.addEdge(vs.get(4), vs.get(3));
        g.addEdge(vs.get(4), vs.get(2));
        g.addEdge(vs.get(5), vs.get(4));

        // http://jgrapht.org/javadoc/org/jgrapht/alg/BlockCutpointGraph.html
        BlockCutpointGraph bcg = new BlockCutpointGraph(g);

        System.out.println("Точки сочленения: " + bcg.getCutpoints());
        System.out.println("Блоки вершин:");
        for (int i = 0; i < vs.size(); i++) {
            System.out.println(bcg.getBlock(vs.get(i)).vertexSet());
        }
        System.out.println("Выход:");
        for (int i = 0; i < vs.size(); i++) {
            Set s;
            if (bcg.isCutpoint(vs.get(i)) && i > 0) {
                s = bcg.getBlock(vs.get(i - 1)).vertexSet();
            } else {
                s = bcg.getBlock(vs.get(i)).vertexSet();
            }
            System.out.println(Collections.min(s, null));
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt(), m = scanner.nextInt();
        UndirectedGraph<Integer, DefaultEdge> g = new SimpleGraph<Integer, DefaultEdge>(DefaultEdge.class);
        ArrayList<Integer> vs = new ArrayList<>();
        for (int i = 0; i < n; i++) {
            vs.add(i);
            g.addVertex(vs.get(i));
        }
        for (int i = 0; i < m; i++) {
            int x = scanner.nextInt();
            int y = scanner.nextInt();
            g.addEdge(vs.get(x), vs.get(y));
        }
        BlockCutpointGraph bcg = new BlockCutpointGraph(g);
        StringBuilder sb = new StringBuilder("");
        HashMap<UndirectedGraph<Integer, DefaultEdge>, Integer> mins = new HashMap<>();
        for (int i = 0; i < vs.size(); i++) {
            UndirectedGraph<Integer, DefaultEdge> u;
            if (bcg.isCutpoint(vs.get(i)) && i > 0) {
                u = bcg.getBlock(vs.get(i - 1));
            } else {
                u = bcg.getBlock(vs.get(i));
            }
            if (mins.containsKey(u)) {
                sb.append(mins.get(u) + "\n");
            } else {
                Integer minVertex = Collections.min(u.vertexSet(), null);
                mins.put(u, minVertex);
                sb.append(minVertex + "\n");
            }
        }
        System.out.println(sb);
    }
}

class IntrusiveEdge implements Cloneable, Serializable {
    private static final long serialVersionUID = 3258408452177932855L;
    Object source;
    Object target;

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError();
        }
    }
}

class DefaultEdge extends IntrusiveEdge {
    private static final long serialVersionUID = 3258408452177932855L;

    protected Object getSource() {
        return source;
    }

    protected Object getTarget() {
        return target;
    }

    @Override
    public String toString() {
        return "(" + source + " : " + target + ")";
    }
}

interface EdgeFactory<V, E> {
    public E createEdge(V sourceVertex, V targetVertex);
}

interface Graph<V, E> {
    public Set<E> getAllEdges(V sourceVertex, V targetVertex);

    public E getEdge(V sourceVertex, V targetVertex);

    public EdgeFactory<V, E> getEdgeFactory();

    public E addEdge(V sourceVertex, V targetVertex);

    public boolean addEdge(V sourceVertex, V targetVertex, E e);

    public boolean addVertex(V v);

    public boolean containsEdge(V sourceVertex, V targetVertex);

    public boolean containsEdge(E e);

    public boolean containsVertex(V v);

    public Set<E> edgeSet();

    public Set<E> edgesOf(V vertex);

    public boolean removeAllEdges(Collection<? extends E> edges);

    public Set<E> removeAllEdges(V sourceVertex, V targetVertex);

    public boolean removeAllVertices(Collection<? extends V> vertices);

    public E removeEdge(V sourceVertex, V targetVertex);

    public boolean removeEdge(E e);

    public boolean removeVertex(V v);

    public Set<V> vertexSet();

    public V getEdgeSource(E e);

    public V getEdgeTarget(E e);

    public double getEdgeWeight(E e);
}

interface UndirectedGraph<V, E> extends Graph<V, E> {
    public int degreeOf(V vertex);
}

class BlockCutpointGraph<V, E> extends SimpleGraph<UndirectedGraph<V, E>, DefaultEdge> {
    private static final long serialVersionUID = -9101341117013163934L;
    private Set<V> cutpoints = new HashSet<V>();
    private DirectedGraph<V, DefaultEdge> dfsTree;
    private UndirectedGraph<V, E> graph;
    private int numOrder;
    private Deque<BCGEdge> stack = new ArrayDeque<BCGEdge>();
    private Map<V, Set<UndirectedGraph<V, E>>> vertex2biconnectedSubgraphs = new HashMap<V, Set<UndirectedGraph<V, E>>>();
    private Map<V, UndirectedGraph<V, E>> vertex2block = new HashMap<V, UndirectedGraph<V, E>>();
    private Map<V, Integer> vertex2numOrder = new HashMap<V, Integer>();

    public BlockCutpointGraph(UndirectedGraph<V, E> graph) {
        super(DefaultEdge.class);
        this.graph = graph;
        this.dfsTree = new SimpleDirectedGraph<V, DefaultEdge>(DefaultEdge.class);
        V s = graph.vertexSet().iterator().next();
        this.dfsTree.addVertex(s);
        dfsVisit(s, s);
        if (this.dfsTree.edgesOf(s).size() > 1) {
            this.cutpoints.add(s);
        } else {
            this.cutpoints.remove(s);
        }
        for (Iterator<V> iter = this.cutpoints.iterator(); iter.hasNext(); ) {
            V cutpoint = iter.next();
            UndirectedGraph<V, E> subgraph = new SimpleGraph<V, E>(this.graph.getEdgeFactory());
            subgraph.addVertex(cutpoint);
            this.vertex2block.put(cutpoint, subgraph);
            addVertex(subgraph);
            Set<UndirectedGraph<V, E>> biconnectedSubgraphs = getBiconnectedSubgraphs(cutpoint);
            for (Iterator<UndirectedGraph<V, E>> iterator = biconnectedSubgraphs.iterator(); iterator.hasNext(); ) {
                UndirectedGraph<V, E> biconnectedSubgraph = iterator.next();
                addEdge(subgraph, biconnectedSubgraph);
            }
        }
    }

    public UndirectedGraph<V, E> getBlock(V vertex) {
        if (!this.graph.vertexSet().contains(vertex)) {
            throw new IllegalArgumentException("No such vertex in the graph!");
        }
        return this.vertex2block.get(vertex);
    }

    public Set<V> getCutpoints() {
        return this.cutpoints;
    }

    public boolean isCutpoint(V vertex) {
        if (!this.graph.vertexSet().contains(vertex)) {
            throw new IllegalArgumentException("No such vertex in the graph!");
        }
        return this.cutpoints.contains(vertex);
    }

    private void biconnectedComponentFinished(V s, V n) {
        this.cutpoints.add(s);
        Set<V> vertexComponent = new HashSet<V>();
        Set<BCGEdge> edgeComponent = new HashSet<BCGEdge>();
        BCGEdge edge = this.stack.removeLast();
        while ((getNumOrder(edge.getSource()) >= getNumOrder(n)) && !this.stack.isEmpty()) {
            edgeComponent.add(edge);
            vertexComponent.add(edge.getSource());
            vertexComponent.add(edge.getTarget());
            edge = this.stack.removeLast();
        }
        edgeComponent.add(edge);
        vertexComponent.add(edge.getSource());
        vertexComponent.add(edge.getTarget());
        VertexComponentForbiddenFunction mask = new VertexComponentForbiddenFunction(vertexComponent);
        UndirectedGraph<V, E> biconnectedSubgraph = new UndirectedMaskSubgraph<V, E>(this.graph, mask);
        for (Iterator<V> iter = vertexComponent.iterator(); iter.hasNext(); ) {
            V vertex = iter.next();
            this.vertex2block.put(vertex, biconnectedSubgraph);
            getBiconnectedSubgraphs(vertex).add(biconnectedSubgraph);
        }
        addVertex(biconnectedSubgraph);
    }

    private int dfsVisit(V s, V father) {
        this.numOrder++;
        int minS = this.numOrder;
        setNumOrder(s, this.numOrder);
        for (Iterator<E> iter = this.graph.edgesOf(s).iterator(); iter.hasNext(); ) {
            E edge = iter.next();
            V n = Graphs.getOppositeVertex(this.graph, edge, s);
            if (getNumOrder(n) == 0) {
                this.dfsTree.addVertex(n);
                BCGEdge dfsEdge = new BCGEdge(s, n);
                this.dfsTree.addEdge(s, n, dfsEdge);

                this.stack.add(dfsEdge);



                int minN = dfsVisit(n, s);
                minS = Math.min(minN, minS);
                if (minN >= getNumOrder(s)) {


                    biconnectedComponentFinished(s, n);
                }
            } else if ((getNumOrder(n) < getNumOrder(s)) && !n.equals(father)) {
                BCGEdge backwardEdge = new BCGEdge(s, n);
                this.stack.add(backwardEdge);


                minS = Math.min(getNumOrder(n), minS);
            }
        }



        return minS;
    }

    private Set<UndirectedGraph<V, E>> getBiconnectedSubgraphs(V vertex) {
        Set<UndirectedGraph<V, E>> biconnectedSubgraphs = this.vertex2biconnectedSubgraphs.get(vertex);
        if (biconnectedSubgraphs == null) {
            biconnectedSubgraphs = new HashSet<UndirectedGraph<V, E>>();
            this.vertex2biconnectedSubgraphs.put(vertex, biconnectedSubgraphs);
        }
        return biconnectedSubgraphs;
    }

    private int getNumOrder(V vertex) {

        Integer numOrder = this.vertex2numOrder.get(vertex);
        if (numOrder == null) {
            return 0;
        } else {
            return numOrder.intValue();
        }
    }

    private void setNumOrder(V vertex, int numOrder) {
        this.vertex2numOrder.put(vertex, Integer.valueOf(numOrder));
    }


    private class BCGEdge
            extends DefaultEdge {

        private static final long serialVersionUID = -5115006161815760059L;

        private V source;

        private V target;

        public BCGEdge(V source, V target) {
            super();
            this.source = source;
            this.target = target;
        }

        @Override
        public V getSource() {
            return this.source;
        }

        @Override
        public V getTarget() {
            return this.target;
        }
    }

    private class VertexComponentForbiddenFunction
            implements MaskFunctor<V, E> {
        private Set<V> vertexComponent;

        public VertexComponentForbiddenFunction(Set<V> vertexComponent) {
            this.vertexComponent = vertexComponent;
        }

        @Override
        public boolean isEdgeMasked(E edge) {
            return false;
        }

        @Override
        public boolean isVertexMasked(V vertex) {
            if (this.vertexComponent.contains(vertex)) {

                return false;
            } else {
                return true;
            }
        }
    }
}

interface DirectedGraph<V, E> extends Graph<V, E> {
    public int inDegreeOf(V vertex);

    public Set<E> incomingEdgesOf(V vertex);

    public int outDegreeOf(V vertex);

    public Set<E> outgoingEdgesOf(V vertex);
}

class SimpleGraph<V, E> extends AbstractBaseGraph<V, E> implements UndirectedGraph<V, E> {
    private static final long serialVersionUID = 3545796589454112304L;

    public SimpleGraph(EdgeFactory<V, E> ef) {
        super(ef, false, false);
    }

    public SimpleGraph(Class<? extends E> edgeClass) {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    public static <V, E> UndirectedGraphBuilderBase<V, E, ? extends SimpleGraph<V, E>, ?> builder(Class<? extends E> edgeClass) {
        return new UndirectedGraphBuilder<V, E, SimpleGraph<V, E>>(new SimpleGraph<V, E>(edgeClass));
    }

    public static <V, E> UndirectedGraphBuilderBase<V, E, ? extends SimpleGraph<V, E>, ?> builder(EdgeFactory<V, E> ef) {
        return new UndirectedGraphBuilder<V, E, SimpleGraph<V, E>>(new SimpleGraph<V, E>(ef));
    }
}

abstract class AbstractBaseGraph<V, E> extends AbstractGraph<V, E> implements Graph<V, E>, Cloneable, Serializable {
    private static final long serialVersionUID = -1263088497616142427L;
    private static final String LOOPS_NOT_ALLOWED = "loops not allowed";
    boolean allowingLoops;
    private EdgeFactory<V, E> edgeFactory;
    private EdgeSetFactory<V, E> edgeSetFactory;
    private Map<E, IntrusiveEdge> edgeMap;
    private transient Set<E> unmodifiableEdgeSet = null;
    private transient Set<V> unmodifiableVertexSet = null;
    private Specifics specifics;
    private boolean allowingMultipleEdges;
    private transient TypeUtil<V> vertexTypeDecl = null;

    protected AbstractBaseGraph(
            EdgeFactory<V, E> ef,
            boolean allowMultipleEdges,
            boolean allowLoops) {
        if (ef == null) {
            throw new NullPointerException();
        }

        edgeMap = new LinkedHashMap<E, IntrusiveEdge>();
        edgeFactory = ef;
        allowingLoops = allowLoops;
        allowingMultipleEdges = allowMultipleEdges;

        specifics = createSpecifics();

        this.edgeSetFactory = new ArrayListFactory<V, E>();
    }

    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
        return specifics.getAllEdges(sourceVertex, targetVertex);
    }

    public boolean isAllowingLoops() {
        return allowingLoops;
    }

    public boolean isAllowingMultipleEdges() {
        return allowingMultipleEdges;
    }

    @Override
    public E getEdge(V sourceVertex, V targetVertex) {
        return specifics.getEdge(sourceVertex, targetVertex);
    }


    @Override
    public EdgeFactory<V, E> getEdgeFactory() {
        return edgeFactory;
    }


    public void setEdgeSetFactory(EdgeSetFactory<V, E> edgeSetFactory) {
        this.edgeSetFactory = edgeSetFactory;
    }


    @Override
    public E addEdge(V sourceVertex, V targetVertex) {

        if (!allowingMultipleEdges
                && containsEdge(sourceVertex, targetVertex)) {
            return null;
        }

        if (!allowingLoops && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        E e = edgeFactory.createEdge(sourceVertex, targetVertex);

        if (containsEdge(e)) {

            return null;
        } else {
            IntrusiveEdge intrusiveEdge =
                    createIntrusiveEdge(e, sourceVertex, targetVertex);

            edgeMap.put(e, intrusiveEdge);
            specifics.addEdgeToTouchingVertices(e);

            return e;
        }
    }


    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e) {
        if (e == null) {
            throw new NullPointerException();
        } else if (containsEdge(e)) {
            return false;
        }


        if (!allowingMultipleEdges
                && containsEdge(sourceVertex, targetVertex)) {
            return false;
        }

        if (!allowingLoops && sourceVertex.equals(targetVertex)) {
            throw new IllegalArgumentException(LOOPS_NOT_ALLOWED);
        }

        IntrusiveEdge intrusiveEdge =
                createIntrusiveEdge(e, sourceVertex, targetVertex);

        edgeMap.put(e, intrusiveEdge);
        specifics.addEdgeToTouchingVertices(e);

        return true;
    }

    private IntrusiveEdge createIntrusiveEdge(
            E e,
            V sourceVertex,
            V targetVertex) {
        IntrusiveEdge intrusiveEdge;
        if (e instanceof IntrusiveEdge) {
            intrusiveEdge = (IntrusiveEdge) e;
        } else {
            intrusiveEdge = new IntrusiveEdge();
        }
        intrusiveEdge.source = sourceVertex;
        intrusiveEdge.target = targetVertex;
        return intrusiveEdge;
    }


    @Override
    public boolean addVertex(V v) {
        if (v == null) {
            throw new NullPointerException();
        } else if (containsVertex(v)) {
            return false;
        } else {
            specifics.addVertex(v);

            return true;
        }
    }


    @Override
    public V getEdgeSource(E e) {
        return TypeUtil.uncheckedCast(
                getIntrusiveEdge(e).source,
                vertexTypeDecl);
    }


    @Override
    public V getEdgeTarget(E e) {
        return TypeUtil.uncheckedCast(
                getIntrusiveEdge(e).target,
                vertexTypeDecl);
    }

    private IntrusiveEdge getIntrusiveEdge(E e) {
        if (e instanceof IntrusiveEdge) {
            return (IntrusiveEdge) e;
        }

        return edgeMap.get(e);
    }


    @Override
    public Object clone() {
        try {
            TypeUtil<AbstractBaseGraph<V, E>> typeDecl = null;

            AbstractBaseGraph<V, E> newGraph =
                    TypeUtil.uncheckedCast(super.clone(), typeDecl);

            newGraph.edgeMap = new LinkedHashMap<E, IntrusiveEdge>();

            newGraph.edgeFactory = this.edgeFactory;
            newGraph.unmodifiableEdgeSet = null;
            newGraph.unmodifiableVertexSet = null;




            newGraph.specifics = newGraph.createSpecifics();

            Graphs.addGraph(newGraph, this);

            return newGraph;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }


    @Override
    public boolean containsEdge(E e) {
        return edgeMap.containsKey(e);
    }


    @Override
    public boolean containsVertex(V v) {
        return specifics.getVertexSet().contains(v);
    }


    public int degreeOf(V vertex) {
        return specifics.degreeOf(vertex);
    }


    @Override
    public Set<E> edgeSet() {
        if (unmodifiableEdgeSet == null) {
            unmodifiableEdgeSet = Collections.unmodifiableSet(edgeMap.keySet());
        }

        return unmodifiableEdgeSet;
    }


    @Override
    public Set<E> edgesOf(V vertex) {
        return specifics.edgesOf(vertex);
    }


    public int inDegreeOf(V vertex) {
        return specifics.inDegreeOf(vertex);
    }


    public Set<E> incomingEdgesOf(V vertex) {
        return specifics.incomingEdgesOf(vertex);
    }


    public int outDegreeOf(V vertex) {
        return specifics.outDegreeOf(vertex);
    }


    public Set<E> outgoingEdgesOf(V vertex) {
        return specifics.outgoingEdgesOf(vertex);
    }


    @Override
    public E removeEdge(V sourceVertex, V targetVertex) {
        E e = getEdge(sourceVertex, targetVertex);

        if (e != null) {
            specifics.removeEdgeFromTouchingVertices(e);
            edgeMap.remove(e);
        }

        return e;
    }


    @Override
    public boolean removeEdge(E e) {
        if (containsEdge(e)) {
            specifics.removeEdgeFromTouchingVertices(e);
            edgeMap.remove(e);

            return true;
        } else {
            return false;
        }
    }


    @Override
    public boolean removeVertex(V v) {
        if (containsVertex(v)) {
            Set<E> touchingEdgesList = edgesOf(v);



            removeAllEdges(new ArrayList<E>(touchingEdgesList));

            specifics.getVertexSet().remove(v);

            return true;
        } else {
            return false;
        }
    }


    @Override
    public Set<V> vertexSet() {
        if (unmodifiableVertexSet == null) {
            unmodifiableVertexSet =
                    Collections.unmodifiableSet(specifics.getVertexSet());
        }

        return unmodifiableVertexSet;
    }


    @Override
    public double getEdgeWeight(E e) {
        if (e instanceof DefaultWeightedEdge) {
            return ((DefaultWeightedEdge) e).getWeight();
        } else if (e == null) {
            throw new NullPointerException();
        } else {
            return WeightedGraph.DEFAULT_EDGE_WEIGHT;
        }
    }


    public void setEdgeWeight(E e, double weight) {
        ((DefaultWeightedEdge) e).weight = weight;
    }

    private Specifics createSpecifics() {
        if (this instanceof DirectedGraph<?, ?>) {
            return createDirectedSpecifics();
        } else if (this instanceof UndirectedGraph<?, ?>) {
            return createUndirectedSpecifics();
        } else {
            throw new IllegalArgumentException(
                    "must be instance of either DirectedGraph or UndirectedGraph");
        }
    }

    protected UndirectedSpecifics createUndirectedSpecifics() {
        return new UndirectedSpecifics();
    }

    protected DirectedSpecifics createDirectedSpecifics() {
        return new DirectedSpecifics();
    }



    private abstract class Specifics
            implements Serializable {
        private static final long serialVersionUID = 785196247314761183L;

        public abstract void addVertex(V vertex);

        public abstract Set<V> getVertexSet();


        public abstract Set<E> getAllEdges(V sourceVertex,
                                           V targetVertex);


        public abstract E getEdge(V sourceVertex, V targetVertex);


        public abstract void addEdgeToTouchingVertices(E e);


        public abstract int degreeOf(V vertex);


        public abstract Set<E> edgesOf(V vertex);


        public abstract int inDegreeOf(V vertex);


        public abstract Set<E> incomingEdgesOf(V vertex);


        public abstract int outDegreeOf(V vertex);


        public abstract Set<E> outgoingEdgesOf(V vertex);


        public abstract void removeEdgeFromTouchingVertices(E e);
    }

    private static class ArrayListFactory<VV, EE>
            implements EdgeSetFactory<VV, EE>,
            Serializable {
        private static final long serialVersionUID = 5936902837403445985L;

        @Override
        public Set<EE> createEdgeSet(VV vertex) {


            return new ArrayUnenforcedSet<EE>(1);
        }
    }


    protected static class DirectedEdgeContainer<VV, EE>
            implements Serializable {
        private static final long serialVersionUID = 7494242245729767106L;
        Set<EE> incoming;
        Set<EE> outgoing;
        private transient Set<EE> unmodifiableIncoming = null;
        private transient Set<EE> unmodifiableOutgoing = null;

        DirectedEdgeContainer(EdgeSetFactory<VV, EE> edgeSetFactory,
                              VV vertex) {
            incoming = edgeSetFactory.createEdgeSet(vertex);
            outgoing = edgeSetFactory.createEdgeSet(vertex);
        }


        public Set<EE> getUnmodifiableIncomingEdges() {
            if (unmodifiableIncoming == null) {
                unmodifiableIncoming = Collections.unmodifiableSet(incoming);
            }

            return unmodifiableIncoming;
        }


        public Set<EE> getUnmodifiableOutgoingEdges() {
            if (unmodifiableOutgoing == null) {
                unmodifiableOutgoing = Collections.unmodifiableSet(outgoing);
            }

            return unmodifiableOutgoing;
        }


        public void addIncomingEdge(EE e) {
            incoming.add(e);
        }


        public void addOutgoingEdge(EE e) {
            outgoing.add(e);
        }


        public void removeIncomingEdge(EE e) {
            incoming.remove(e);
        }


        public void removeOutgoingEdge(EE e) {
            outgoing.remove(e);
        }
    }


    protected class DirectedSpecifics
            extends Specifics
            implements Serializable {
        private static final long serialVersionUID = 8971725103718958232L;
        private static final String NOT_IN_DIRECTED_GRAPH =
                "no such operation in a directed graph";

        protected Map<V, DirectedEdgeContainer<V, E>> vertexMapDirected;

        public DirectedSpecifics() {
            this(new LinkedHashMap<V, DirectedEdgeContainer<V, E>>());
        }

        public DirectedSpecifics(Map<V, DirectedEdgeContainer<V, E>> vertexMap) {
            this.vertexMapDirected = vertexMap;
        }

        @Override
        public void addVertex(V v) {

            vertexMapDirected.put(v, null);
        }

        @Override
        public Set<V> getVertexSet() {
            return vertexMapDirected.keySet();
        }


        @Override
        public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
            Set<E> edges = null;

            if (containsVertex(sourceVertex)
                    && containsVertex(targetVertex)) {
                edges = new ArrayUnenforcedSet<E>();

                DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

                Iterator<E> iter = ec.outgoing.iterator();

                while (iter.hasNext()) {
                    E e = iter.next();

                    if (getEdgeTarget(e).equals(targetVertex)) {
                        edges.add(e);
                    }
                }
            }

            return edges;
        }


        @Override
        public E getEdge(V sourceVertex, V targetVertex) {
            if (containsVertex(sourceVertex)
                    && containsVertex(targetVertex)) {
                DirectedEdgeContainer<V, E> ec = getEdgeContainer(sourceVertex);

                Iterator<E> iter = ec.outgoing.iterator();

                while (iter.hasNext()) {
                    E e = iter.next();

                    if (getEdgeTarget(e).equals(targetVertex)) {
                        return e;
                    }
                }
            }

            return null;
        }

        @Override
        public void addEdgeToTouchingVertices(E e) {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).addOutgoingEdge(e);
            getEdgeContainer(target).addIncomingEdge(e);
        }


        @Override
        public int degreeOf(V vertex) {
            throw new UnsupportedOperationException(NOT_IN_DIRECTED_GRAPH);
        }


        @Override
        public Set<E> edgesOf(V vertex) {
            ArrayUnenforcedSet<E> inAndOut =
                    new ArrayUnenforcedSet<E>(getEdgeContainer(vertex).incoming);
            inAndOut.addAll(getEdgeContainer(vertex).outgoing);


            if (allowingLoops) {
                Set<E> loops = getAllEdges(vertex, vertex);

                for (int i = 0; i < inAndOut.size(); ) {
                    Object e = inAndOut.get(i);

                    if (loops.contains(e)) {
                        inAndOut.remove(i);
                        loops.remove(e);
                    } else {
                        i++;
                    }
                }
            }

            return Collections.unmodifiableSet(inAndOut);
        }


        @Override
        public int inDegreeOf(V vertex) {
            return getEdgeContainer(vertex).incoming.size();
        }


        @Override
        public Set<E> incomingEdgesOf(V vertex) {
            return getEdgeContainer(vertex).getUnmodifiableIncomingEdges();
        }


        @Override
        public int outDegreeOf(V vertex) {
            return getEdgeContainer(vertex).outgoing.size();
        }


        @Override
        public Set<E> outgoingEdgesOf(V vertex) {
            return getEdgeContainer(vertex).getUnmodifiableOutgoingEdges();
        }

        @Override
        public void removeEdgeFromTouchingVertices(E e) {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).removeOutgoingEdge(e);
            getEdgeContainer(target).removeIncomingEdge(e);
        }


        private DirectedEdgeContainer<V, E> getEdgeContainer(V vertex) {

            DirectedEdgeContainer<V, E> ec = vertexMapDirected.get(vertex);

            if (ec == null) {
                ec = new DirectedEdgeContainer<V, E>(edgeSetFactory, vertex);
                vertexMapDirected.put(vertex, ec);
            }

            return ec;
        }
    }


    private static class UndirectedEdgeContainer<VV, EE>
            implements Serializable {
        private static final long serialVersionUID = -6623207588411170010L;
        Set<EE> vertexEdges;
        private transient Set<EE> unmodifiableVertexEdges = null;

        UndirectedEdgeContainer(
                EdgeSetFactory<VV, EE> edgeSetFactory,
                VV vertex) {
            vertexEdges = edgeSetFactory.createEdgeSet(vertex);
        }


        public Set<EE> getUnmodifiableVertexEdges() {
            if (unmodifiableVertexEdges == null) {
                unmodifiableVertexEdges =
                        Collections.unmodifiableSet(vertexEdges);
            }

            return unmodifiableVertexEdges;
        }


        public void addEdge(EE e) {
            vertexEdges.add(e);
        }


        public int edgeCount() {
            return vertexEdges.size();
        }


        public void removeEdge(EE e) {
            vertexEdges.remove(e);
        }
    }


    protected class UndirectedSpecifics
            extends Specifics
            implements Serializable {
        private static final long serialVersionUID = 6494588405178655873L;
        private static final String NOT_IN_UNDIRECTED_GRAPH =
                "no such operation in an undirected graph";

        private Map<V, UndirectedEdgeContainer<V, E>> vertexMapUndirected;

        public UndirectedSpecifics() {
            this(new LinkedHashMap<V, UndirectedEdgeContainer<V, E>>());
        }

        public UndirectedSpecifics(
                Map<V, UndirectedEdgeContainer<V, E>> vertexMap) {
            this.vertexMapUndirected = vertexMap;
        }

        @Override
        public void addVertex(V v) {

            vertexMapUndirected.put(v, null);
        }

        @Override
        public Set<V> getVertexSet() {
            return vertexMapUndirected.keySet();
        }


        @Override
        public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
            Set<E> edges = null;

            if (containsVertex(sourceVertex)
                    && containsVertex(targetVertex)) {
                edges = new ArrayUnenforcedSet<E>();

                Iterator<E> iter =
                        getEdgeContainer(sourceVertex).vertexEdges.iterator();

                while (iter.hasNext()) {
                    E e = iter.next();

                    boolean equal =
                            isEqualsStraightOrInverted(
                                    sourceVertex,
                                    targetVertex,
                                    e);

                    if (equal) {
                        edges.add(e);
                    }
                }
            }

            return edges;
        }


        @Override
        public E getEdge(V sourceVertex, V targetVertex) {
            if (containsVertex(sourceVertex)
                    && containsVertex(targetVertex)) {
                Iterator<E> iter =
                        getEdgeContainer(sourceVertex).vertexEdges.iterator();

                while (iter.hasNext()) {
                    E e = iter.next();

                    boolean equal =
                            isEqualsStraightOrInverted(
                                    sourceVertex,
                                    targetVertex,
                                    e);

                    if (equal) {
                        return e;
                    }
                }
            }

            return null;
        }

        private boolean isEqualsStraightOrInverted(
                Object sourceVertex,
                Object targetVertex,
                E e) {
            boolean equalStraight =
                    sourceVertex.equals(getEdgeSource(e))
                            && targetVertex.equals(getEdgeTarget(e));

            boolean equalInverted =
                    sourceVertex.equals(getEdgeTarget(e))
                            && targetVertex.equals(getEdgeSource(e));
            return equalStraight || equalInverted;
        }

        @Override
        public void addEdgeToTouchingVertices(E e) {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).addEdge(e);

            if (!source.equals(target)) {
                getEdgeContainer(target).addEdge(e);
            }
        }

        @Override
        public int degreeOf(V vertex) {
            if (allowingLoops) {

                int degree = 0;
                Set<E> edges = getEdgeContainer(vertex).vertexEdges;

                for (E e : edges) {
                    if (getEdgeSource(e).equals(getEdgeTarget(e))) {
                        degree += 2;
                    } else {
                        degree += 1;
                    }
                }

                return degree;
            } else {
                return getEdgeContainer(vertex).edgeCount();
            }
        }


        @Override
        public Set<E> edgesOf(V vertex) {
            return getEdgeContainer(vertex).getUnmodifiableVertexEdges();
        }


        @Override
        public int inDegreeOf(V vertex) {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }


        @Override
        public Set<E> incomingEdgesOf(V vertex) {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }


        @Override
        public int outDegreeOf(V vertex) {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }


        @Override
        public Set<E> outgoingEdgesOf(V vertex) {
            throw new UnsupportedOperationException(NOT_IN_UNDIRECTED_GRAPH);
        }

        @Override
        public void removeEdgeFromTouchingVertices(E e) {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            getEdgeContainer(source).removeEdge(e);

            if (!source.equals(target)) {
                getEdgeContainer(target).removeEdge(e);
            }
        }


        private UndirectedEdgeContainer<V, E> getEdgeContainer(V vertex) {

            UndirectedEdgeContainer<V, E> ec = vertexMapUndirected.get(vertex);

            if (ec == null) {
                ec = new UndirectedEdgeContainer<V, E>(
                        edgeSetFactory,
                        vertex);
                vertexMapUndirected.put(vertex, ec);
            }

            return ec;
        }
    }
}

class SimpleDirectedGraph<V, E> extends AbstractBaseGraph<V, E> implements DirectedGraph<V, E> {
    private static final long serialVersionUID = 4049358608472879671L;

    public SimpleDirectedGraph(Class<? extends E> edgeClass) {
        this(new ClassBasedEdgeFactory<V, E>(edgeClass));
    }

    public SimpleDirectedGraph(EdgeFactory<V, E> ef) {
        super(ef, false, false);
    }

    public static <V, E> DirectedGraphBuilderBase<V,
            E, ? extends SimpleDirectedGraph<V, E>, ?> builder(
            Class<? extends E> edgeClass) {
        return new DirectedGraphBuilder<V, E, SimpleDirectedGraph<V, E>>(
                new SimpleDirectedGraph<V, E>(edgeClass));
    }

    public static <V, E> DirectedGraphBuilderBase<V,
            E, ? extends SimpleDirectedGraph<V, E>, ?> builder(EdgeFactory<V, E> ef) {
        return new DirectedGraphBuilder<V, E, SimpleDirectedGraph<V, E>>(
                new SimpleDirectedGraph<V, E>(ef));
    }
}

class ClassBasedEdgeFactory<V, E> implements EdgeFactory<V, E>, Serializable {
    private static final long serialVersionUID = 3618135658586388792L;
    private final Class<? extends E> edgeClass;

    public ClassBasedEdgeFactory(Class<? extends E> edgeClass) {
        this.edgeClass = edgeClass;
    }

    @Override
    public E createEdge(V source, V target) {
        try {
            return edgeClass.newInstance();
        } catch (Exception ex) {
            throw new RuntimeException("Edge factory failed", ex);
        }
    }
}

abstract class DirectedGraphBuilderBase<V,
        E,
        G extends DirectedGraph<V, E>,
        B extends DirectedGraphBuilderBase<V, E, G, B>>
        extends AbstractGraphBuilder<V, E, G, B> {
    public DirectedGraphBuilderBase(G baseGraph) {
        super(baseGraph);
    }

    @Override
    public UnmodifiableDirectedGraph<V, E> buildUnmodifiable() {
        return new UnmodifiableDirectedGraph<V, E>(this.graph);
    }
}

abstract class AbstractGraphBuilder<V,
        E, G extends Graph<V, E>, B extends AbstractGraphBuilder<V, E, G, B>> {
    protected final G graph;

    public AbstractGraphBuilder(G baseGraph) {
        this.graph = baseGraph;
    }

    protected abstract B self();

    public B addVertex(V vertex) {
        this.graph.addVertex(vertex);
        return this.self();
    }

    public B addVertices(V... vertices) {
        for (V vertex : vertices) {
            this.addVertex(vertex);
        }
        return this.self();
    }

    public B addEdge(V source, V target) {
        Graphs.addEdgeWithVertices(this.graph, source, target);
        return this.self();
    }

    public B addEdgeChain(V first, V second, V... rest) {
        this.addEdge(first, second);
        V last = second;
        for (V vertex : rest) {
            this.addEdge(last, vertex);
            last = vertex;
        }
        return this.self();
    }

    public B addGraph(Graph<? extends V, ? extends E> sourceGraph) {
        Graphs.addGraph(this.graph, sourceGraph);
        return this.self();
    }

    public B removeVertex(V vertex) {
        this.graph.removeVertex(vertex);
        return this.self();
    }

    public B removeVertices(V... vertices) {
        for (V vertex : vertices) {
            this.removeVertex(vertex);
        }
        return this.self();
    }

    public B removeEdge(V source, V target) {
        this.graph.removeEdge(source, target);
        return this.self();
    }

    public G build() {
        return this.graph;
    }

    public UnmodifiableGraph<V, E> buildUnmodifiable() {
        return new UnmodifiableGraph<V, E>(this.graph);
    }
}

class UnmodifiableGraph<V, E>
        extends GraphDelegator<V, E>
        implements Serializable {


    private static final long serialVersionUID = 3544957670722713913L;
    private static final String UNMODIFIABLE = "this graph is unmodifiable";



    public UnmodifiableGraph(Graph<V, E> g) {
        super(g);
    }



    @Override
    public E addEdge(V sourceVertex, V targetVertex) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public boolean addVertex(V v) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public boolean removeAllEdges(Collection<? extends E> edges) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public boolean removeEdge(E e) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public E removeEdge(V sourceVertex, V targetVertex) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public boolean removeVertex(V v) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }
}

class GraphDelegator<V, E>
        extends AbstractGraph<V, E>
        implements Graph<V, E>,
        Serializable {


    private static final long serialVersionUID = 3257005445226181425L;



    private Graph<V, E> delegate;



    public GraphDelegator(Graph<V, E> g) {
        super();

        if (g == null) {
            throw new IllegalArgumentException("g must not be null.");
        }

        delegate = g;
    }



    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
        return delegate.getAllEdges(sourceVertex, targetVertex);
    }


    @Override
    public E getEdge(V sourceVertex, V targetVertex) {
        return delegate.getEdge(sourceVertex, targetVertex);
    }


    @Override
    public EdgeFactory<V, E> getEdgeFactory() {
        return delegate.getEdgeFactory();
    }


    @Override
    public E addEdge(V sourceVertex, V targetVertex) {
        return delegate.addEdge(sourceVertex, targetVertex);
    }


    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e) {
        return delegate.addEdge(sourceVertex, targetVertex, e);
    }


    @Override
    public boolean addVertex(V v) {
        return delegate.addVertex(v);
    }


    @Override
    public boolean containsEdge(E e) {
        return delegate.containsEdge(e);
    }


    @Override
    public boolean containsVertex(V v) {
        return delegate.containsVertex(v);
    }


    public int degreeOf(V vertex) {
        return ((UndirectedGraph<V, E>) delegate).degreeOf(vertex);
    }


    @Override
    public Set<E> edgeSet() {
        return delegate.edgeSet();
    }


    @Override
    public Set<E> edgesOf(V vertex) {
        return delegate.edgesOf(vertex);
    }


    public int inDegreeOf(V vertex) {
        return ((DirectedGraph<V, ? extends E>) delegate).inDegreeOf(vertex);
    }


    public Set<E> incomingEdgesOf(V vertex) {
        return ((DirectedGraph<V, E>) delegate).incomingEdgesOf(vertex);
    }


    public int outDegreeOf(V vertex) {
        return ((DirectedGraph<V, ? extends E>) delegate).outDegreeOf(vertex);
    }


    public Set<E> outgoingEdgesOf(V vertex) {
        return ((DirectedGraph<V, E>) delegate).outgoingEdgesOf(vertex);
    }


    @Override
    public boolean removeEdge(E e) {
        return delegate.removeEdge(e);
    }


    @Override
    public E removeEdge(V sourceVertex, V targetVertex) {
        return delegate.removeEdge(sourceVertex, targetVertex);
    }


    @Override
    public boolean removeVertex(V v) {
        return delegate.removeVertex(v);
    }


    @Override
    public String toString() {
        return delegate.toString();
    }


    @Override
    public Set<V> vertexSet() {
        return delegate.vertexSet();
    }


    @Override
    public V getEdgeSource(E e) {
        return delegate.getEdgeSource(e);
    }


    @Override
    public V getEdgeTarget(E e) {
        return delegate.getEdgeTarget(e);
    }


    @Override
    public double getEdgeWeight(E e) {
        return delegate.getEdgeWeight(e);
    }

    public void setEdgeWeight(E e, double weight) {
        ((WeightedGraph<V, E>) delegate).setEdgeWeight(e, weight);
    }
}

abstract class AbstractGraph<V, E>
        implements Graph<V, E> {
    protected AbstractGraph() {
    }

    @Override
    public boolean containsEdge(V sourceVertex, V targetVertex) {
        return getEdge(sourceVertex, targetVertex) != null;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends E> edges) {
        boolean modified = false;

        for (E e : edges) {
            modified |= removeEdge(e);
        }

        return modified;
    }

    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
        Set<E> removed = getAllEdges(sourceVertex, targetVertex);
        if (removed == null) {
            return null;
        }
        removeAllEdges(removed);

        return removed;
    }

    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices) {
        boolean modified = false;

        for (V v : vertices) {
            modified |= removeVertex(v);
        }

        return modified;
    }

    @Override
    public String toString() {
        return toStringFromSets(
                vertexSet(),
                edgeSet(),
                (this instanceof DirectedGraph<?, ?>));
    }

    protected boolean removeAllEdges(E[] edges) {
        boolean modified = false;

        for (int i = 0; i < edges.length; i++) {
            modified |= removeEdge(edges[i]);
        }

        return modified;
    }

    protected String toStringFromSets(
            Collection<? extends V> vertexSet,
            Collection<? extends E> edgeSet,
            boolean directed) {
        List<String> renderedEdges = new ArrayList<String>();

        StringBuffer sb = new StringBuffer();
        for (E e : edgeSet) {
            if ((e.getClass() != DefaultEdge.class)
                    && (e.getClass() != DefaultWeightedEdge.class)) {
                sb.append(e.toString());
                sb.append("=");
            }
            if (directed) {
                sb.append("(");
            } else {
                sb.append("{");
            }
            sb.append(getEdgeSource(e));
            sb.append(",");
            sb.append(getEdgeTarget(e));
            if (directed) {
                sb.append(")");
            } else {
                sb.append("}");
            }


            renderedEdges.add(sb.toString());
            sb.setLength(0);
        }

        return "(" + vertexSet + ", " + renderedEdges + ")";
    }

    @Override
    public int hashCode() {
        int hash = vertexSet().hashCode();

        for (E e : edgeSet()) {
            int part = e.hashCode();

            int source = getEdgeSource(e).hashCode();
            int target = getEdgeTarget(e).hashCode();


            int pairing =
                    ((source + target)
                            * (source + target + 1) / 2) + target;
            part = (27 * part) + pairing;

            long weight = (long) getEdgeWeight(e);
            part = (27 * part) + (int) (weight ^ (weight >>> 32));

            hash += part;
        }

        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if ((obj == null) || (getClass() != obj.getClass())) {
            return false;
        }

        TypeUtil<Graph<V, E>> typeDecl = null;
        Graph<V, E> g = TypeUtil.uncheckedCast(obj, typeDecl);

        if (!vertexSet().equals(g.vertexSet())) {
            return false;
        }
        if (edgeSet().size() != g.edgeSet().size()) {
            return false;
        }

        for (E e : edgeSet()) {
            V source = getEdgeSource(e);
            V target = getEdgeTarget(e);

            if (!g.containsEdge(e)) {
                return false;
            }

            if (!g.getEdgeSource(e).equals(source)
                    || !g.getEdgeTarget(e).equals(target)) {
                return false;
            }

            if (Math.abs(getEdgeWeight(e) - g.getEdgeWeight(e)) > 10e-7) {
                return false;
            }
        }

        return true;
    }
}

class DefaultWeightedEdge extends DefaultEdge {
    private static final long serialVersionUID = 229708706467350994L;
    double weight = WeightedGraph.DEFAULT_EDGE_WEIGHT;

    protected double getWeight() {
        return weight;
    }
}

interface WeightedGraph<V, E> extends Graph<V, E> {
    public static double DEFAULT_EDGE_WEIGHT = 1.0;

    public void setEdgeWeight(E e, double weight);
}

class UnmodifiableDirectedGraph<V, E> extends UnmodifiableGraph<V, E> implements DirectedGraph<V, E> {
    private static final long serialVersionUID = 3978701783725913906L;

    public UnmodifiableDirectedGraph(DirectedGraph<V, E> g) {
        super(g);
    }
}

class TypeUtil<T> {
    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object o, TypeUtil<T> typeDecl) {
        return (T) o;
    }
}

abstract class Graphs {
    public static <V, E> E addEdge(
            Graph<V, E> g,
            V sourceVertex,
            V targetVertex,
            double weight) {
        EdgeFactory<V, E> ef = g.getEdgeFactory();
        E e = ef.createEdge(sourceVertex, targetVertex);




        ((WeightedGraph<V, E>) g).setEdgeWeight(e, weight);

        return g.addEdge(sourceVertex, targetVertex, e) ? e : null;
    }

    public static <V, E> E addEdgeWithVertices(
            Graph<V, E> g,
            V sourceVertex,
            V targetVertex) {
        g.addVertex(sourceVertex);
        g.addVertex(targetVertex);

        return g.addEdge(sourceVertex, targetVertex);
    }

    public static <V, E> boolean addEdgeWithVertices(
            Graph<V, E> targetGraph,
            Graph<V, E> sourceGraph,
            E edge) {
        V sourceVertex = sourceGraph.getEdgeSource(edge);
        V targetVertex = sourceGraph.getEdgeTarget(edge);

        targetGraph.addVertex(sourceVertex);
        targetGraph.addVertex(targetVertex);

        return targetGraph.addEdge(sourceVertex, targetVertex, edge);
    }

    public static <V, E> E addEdgeWithVertices(
            Graph<V, E> g,
            V sourceVertex,
            V targetVertex,
            double weight) {
        g.addVertex(sourceVertex);
        g.addVertex(targetVertex);

        return addEdge(g, sourceVertex, targetVertex, weight);
    }

    public static <V, E> boolean addGraph(
            Graph<? super V, ? super E> destination,
            Graph<V, E> source) {
        boolean modified = addAllVertices(destination, source.vertexSet());
        modified |= addAllEdges(destination, source, source.edgeSet());

        return modified;
    }

    public static <V, E> void addGraphReversed(
            DirectedGraph<? super V, ? super E> destination,
            DirectedGraph<V, E> source) {
        addAllVertices(destination, source.vertexSet());

        for (E edge : source.edgeSet()) {
            destination.addEdge(
                    source.getEdgeTarget(edge),
                    source.getEdgeSource(edge));
        }
    }

    public static <V, E> boolean addAllEdges(
            Graph<? super V, ? super E> destination,
            Graph<V, E> source,
            Collection<? extends E> edges) {
        boolean modified = false;

        for (E e : edges) {
            V s = source.getEdgeSource(e);
            V t = source.getEdgeTarget(e);
            destination.addVertex(s);
            destination.addVertex(t);
            modified |= destination.addEdge(s, t, e);
        }

        return modified;
    }

    public static <V, E> boolean addAllVertices(
            Graph<? super V, ? super E> destination,
            Collection<? extends V> vertices) {
        boolean modified = false;

        for (V v : vertices) {
            modified |= destination.addVertex(v);
        }

        return modified;
    }

    public static <V, E> List<V> neighborListOf(Graph<V, E> g,
                                                V vertex) {
        List<V> neighbors = new ArrayList<V>();

        for (E e : g.edgesOf(vertex)) {
            neighbors.add(getOppositeVertex(g, e, vertex));
        }

        return neighbors;
    }

    public static <V, E> List<V> predecessorListOf(
            DirectedGraph<V, E> g,
            V vertex) {
        List<V> predecessors = new ArrayList<V>();
        Set<? extends E> edges = g.incomingEdgesOf(vertex);

        for (E e : edges) {
            predecessors.add(getOppositeVertex(g, e, vertex));
        }

        return predecessors;
    }

    public static <V, E> List<V> successorListOf(
            DirectedGraph<V, E> g,
            V vertex) {
        List<V> successors = new ArrayList<V>();
        Set<? extends E> edges = g.outgoingEdgesOf(vertex);

        for (E e : edges) {
            successors.add(getOppositeVertex(g, e, vertex));
        }

        return successors;
    }

    public static <V, E> UndirectedGraph<V, E> undirectedGraph(Graph<V, E> g) {
        if (g instanceof DirectedGraph<?, ?>) {
            return new AsUndirectedGraph<V, E>((DirectedGraph<V, E>) g);
        } else if (g instanceof UndirectedGraph<?, ?>) {
            return (UndirectedGraph<V, E>) g;
        } else {
            throw new IllegalArgumentException(
                    "Graph must be either DirectedGraph or UndirectedGraph");
        }
    }

    public static <V, E> boolean testIncidence(Graph<V, E> g, E e, V v) {
        return (g.getEdgeSource(e).equals(v))
                || (g.getEdgeTarget(e).equals(v));
    }

    public static <V, E> V getOppositeVertex(Graph<V, E> g, E e, V v) {
        V source = g.getEdgeSource(e);
        V target = g.getEdgeTarget(e);
        if (v.equals(source)) {
            return target;
        } else if (v.equals(target)) {
            return source;
        } else {
            throw new IllegalArgumentException(
                    "no such vertex: " + v.toString());
        }
    }

    public static <V, E> List<V> getPathVertexList(GraphPath<V, E> path) {
        Graph<V, E> g = path.getGraph();
        List<V> list = new ArrayList<V>();
        V v = path.getStartVertex();
        list.add(v);
        for (E e : path.getEdgeList()) {
            v = getOppositeVertex(g, e, v);
            list.add(v);
        }
        return list;
    }
}

interface GraphPath<V, E> {
    public Graph<V, E> getGraph();

    public V getStartVertex();

    public V getEndVertex();

    public List<E> getEdgeList();

    public double getWeight();
}

class AsUndirectedGraph<V, E>
        extends GraphDelegator<V, E>
        implements Serializable,
        UndirectedGraph<V, E> {


    private static final long serialVersionUID = 3257845485078065462L;
    private static final String NO_EDGE_ADD =
            "this graph does not support edge addition";
    private static final String UNDIRECTED =
            "this graph only supports undirected operations";



    public AsUndirectedGraph(DirectedGraph<V, E> g) {
        super(g);
    }



    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
        Set<E> forwardList = super.getAllEdges(sourceVertex, targetVertex);

        if (sourceVertex.equals(targetVertex)) {

            return forwardList;
        }

        Set<E> reverseList = super.getAllEdges(targetVertex, sourceVertex);
        Set<E> list =
                new ArrayUnenforcedSet<E>(
                        forwardList.size() + reverseList.size());
        list.addAll(forwardList);
        list.addAll(reverseList);

        return list;
    }


    @Override
    public E getEdge(V sourceVertex, V targetVertex) {
        E edge = super.getEdge(sourceVertex, targetVertex);

        if (edge != null) {
            return edge;
        }


        return super.getEdge(targetVertex, sourceVertex);
    }


    @Override
    public E addEdge(V sourceVertex, V targetVertex) {
        throw new UnsupportedOperationException(NO_EDGE_ADD);
    }


    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E e) {
        throw new UnsupportedOperationException(NO_EDGE_ADD);
    }


    @Override
    public int degreeOf(V vertex) {

        return super.inDegreeOf(vertex) + super.outDegreeOf(vertex);
    }


    @Override
    public int inDegreeOf(V vertex) {
        throw new UnsupportedOperationException(UNDIRECTED);
    }


    @Override
    public Set<E> incomingEdgesOf(V vertex) {
        throw new UnsupportedOperationException(UNDIRECTED);
    }


    @Override
    public int outDegreeOf(V vertex) {
        throw new UnsupportedOperationException(UNDIRECTED);
    }


    @Override
    public Set<E> outgoingEdgesOf(V vertex) {
        throw new UnsupportedOperationException(UNDIRECTED);
    }


    @Override
    public String toString() {
        return super.toStringFromSets(vertexSet(), edgeSet(), false);
    }
}

interface EdgeSetFactory<V, E> {
    public Set<E> createEdgeSet(V vertex);
}

abstract class UndirectedGraphBuilderBase<V,
        E,
        G extends UndirectedGraph<V, E>,
        B extends UndirectedGraphBuilderBase<V, E, G, B>>
        extends AbstractGraphBuilder<V, E, G, B> {
    public UndirectedGraphBuilderBase(G baseGraph) {
        super(baseGraph);
    }

    @Override
    public UnmodifiableUndirectedGraph<V, E> buildUnmodifiable() {
        return new UnmodifiableUndirectedGraph<V, E>(this.graph);
    }
}

final class UndirectedGraphBuilder<V, E, G extends UndirectedGraph<V, E>>
        extends UndirectedGraphBuilderBase<V, E, G, UndirectedGraphBuilder<V, E, G>> {
    public UndirectedGraphBuilder(G baseGraph) {
        super(baseGraph);
    }

    @Override
    protected UndirectedGraphBuilder<V, E, G> self() {
        return this;
    }
}

class UnmodifiableUndirectedGraph<V, E>
        extends UnmodifiableGraph<V, E>
        implements UndirectedGraph<V, E> {
    private static final long serialVersionUID = 3258134639355704624L;

    public UnmodifiableUndirectedGraph(UndirectedGraph<V, E> g) {
        super(g);
    }
}

final class DirectedGraphBuilder<V, E, G extends DirectedGraph<V, E>> extends DirectedGraphBuilderBase<V, E, G, DirectedGraphBuilder<V, E, G>> {
    public DirectedGraphBuilder(G baseGraph) {
        super(baseGraph);
    }

    @Override
    protected DirectedGraphBuilder<V, E, G> self() {
        return this;
    }
}

class UndirectedMaskSubgraph<V, E>
        extends MaskSubgraph<V, E>
        implements UndirectedGraph<V, E> {
    public UndirectedMaskSubgraph(
            UndirectedGraph<V, E> base,
            MaskFunctor<V, E> mask) {
        super(base, mask);
    }
}

class MaskSubgraph<V, E>
        extends AbstractGraph<V, E> {


    private static final String UNMODIFIABLE = "this graph is unmodifiable";


    private Graph<V, E> base;

    private Set<E> edges;

    private MaskFunctor<V, E> mask;

    private Set<V> vertices;



    public MaskSubgraph(Graph<V, E> base, MaskFunctor<V, E> mask) {
        super();
        this.base = base;
        this.mask = mask;

        this.vertices = new MaskVertexSet<V, E>(base.vertexSet(), mask);
        this.edges = new MaskEdgeSet<V, E>(base, base.edgeSet(), mask);
    }



    @Override
    public E addEdge(V sourceVertex, V targetVertex) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean addEdge(V sourceVertex, V targetVertex, E edge) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }


    @Override
    public boolean addVertex(V v) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean containsEdge(E e) {
        return edgeSet().contains(e);
    }

    @Override
    public boolean containsVertex(V v) {
        return !this.mask.isVertexMasked(v) && this.base.containsVertex(v);
    }


    public int degreeOf(V vertex) {
        return edgesOf(vertex).size();
    }

    @Override
    public Set<E> edgeSet() {
        return this.edges;
    }

    @Override
    public Set<E> edgesOf(V vertex) {

        return new MaskEdgeSet<V, E>(
                this.base,
                this.base.edgesOf(vertex),
                this.mask);
    }

    @Override
    public Set<E> getAllEdges(V sourceVertex, V targetVertex) {
        Set<E> edges = null;

        if (containsVertex(sourceVertex) && containsVertex(targetVertex)) {
            return new MaskEdgeSet<V, E>(
                    this.base,
                    this.base.getAllEdges(
                            sourceVertex,
                            targetVertex),
                    this.mask);
        }

        return edges;
    }

    @Override
    public E getEdge(V sourceVertex, V targetVertex) {
        Set<E> edges = getAllEdges(sourceVertex, targetVertex);

        if ((edges == null) || edges.isEmpty()) {
            return null;
        } else {
            return edges.iterator().next();
        }
    }

    @Override
    public EdgeFactory<V, E> getEdgeFactory() {
        return this.base.getEdgeFactory();
    }

    @Override
    public V getEdgeSource(E edge) {

        return this.base.getEdgeSource(edge);
    }

    @Override
    public V getEdgeTarget(E edge) {

        return this.base.getEdgeTarget(edge);
    }

    @Override
    public double getEdgeWeight(E edge) {

        return this.base.getEdgeWeight(edge);
    }


    public Set<E> incomingEdgesOf(V vertex) {

        return new MaskEdgeSet<V, E>(
                this.base,
                ((DirectedGraph<V, E>) this.base).incomingEdgesOf(vertex),
                this.mask);
    }


    public int inDegreeOf(V vertex) {
        return incomingEdgesOf(vertex).size();
    }


    public int outDegreeOf(V vertex) {
        return outgoingEdgesOf(vertex).size();
    }


    public Set<E> outgoingEdgesOf(V vertex) {

        return new MaskEdgeSet<V, E>(
                this.base,
                ((DirectedGraph<V, E>) this.base).outgoingEdgesOf(vertex),
                this.mask);
    }

    @Override
    public boolean removeAllEdges(Collection<? extends E> edges) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Set<E> removeAllEdges(V sourceVertex, V targetVertex) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeAllVertices(Collection<? extends V> vertices) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeEdge(E e) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public E removeEdge(V sourceVertex, V targetVertex) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public boolean removeVertex(V v) {
        throw new UnsupportedOperationException(UNMODIFIABLE);
    }

    @Override
    public Set<V> vertexSet() {
        return this.vertices;
    }
}

interface MaskFunctor<V, E> {
    public boolean isEdgeMasked(E edge);

    public boolean isVertexMasked(V vertex);
}

class MaskEdgeSet<V, E>
        extends AbstractSet<E> {


    private Set<E> edgeSet;

    private Graph<V, E> graph;

    private MaskFunctor<V, E> mask;

    private transient TypeUtil<E> edgeTypeDecl = null;

    private int size;


    public MaskEdgeSet(
            Graph<V, E> graph,
            Set<E> edgeSet,
            MaskFunctor<V, E> mask) {
        this.graph = graph;
        this.edgeSet = edgeSet;
        this.mask = mask;
        this.size = -1;
    }



    @Override
    public boolean contains(Object o) {
        return this.edgeSet.contains(o)
                && !this.mask.isEdgeMasked(TypeUtil.uncheckedCast(o, edgeTypeDecl));
    }


    @Override
    public Iterator<E> iterator() {
        return new PrefetchIterator<E>(new MaskEdgeSetNextElementFunctor());
    }


    @Override
    public int size() {
        if (this.size == -1) {
            this.size = 0;
            for (Iterator<E> iter = iterator(); iter.hasNext(); ) {
                iter.next();
                this.size++;
            }
        }
        return this.size;
    }


    private class MaskEdgeSetNextElementFunctor
            implements PrefetchIterator.NextElementFunctor<E> {
        private Iterator<E> iter;

        public MaskEdgeSetNextElementFunctor() {
            this.iter = MaskEdgeSet.this.edgeSet.iterator();
        }

        @Override
        public E nextElement()
                throws NoSuchElementException {
            E edge = this.iter.next();
            while (isMasked(edge)) {
                edge = this.iter.next();
            }
            return edge;
        }

        private boolean isMasked(E edge) {
            return MaskEdgeSet.this.mask.isEdgeMasked(edge)
                    || MaskEdgeSet.this.mask.isVertexMasked(
                    MaskEdgeSet.this.graph.getEdgeSource(edge))
                    || MaskEdgeSet.this.mask.isVertexMasked(
                    MaskEdgeSet.this.graph.getEdgeTarget(edge));
        }
    }
}

class PrefetchIterator<E>
        implements Iterator<E>,
        Enumeration<E> {


    private NextElementFunctor<E> innerEnum;
    private E getNextLastResult;
    private boolean isGetNextLastResultUpToDate = false;
    private boolean endOfEnumerationReached = false;
    private boolean flagIsEnumerationStartedEmpty = true;
    private int innerFunctorUsageCounter = 0;


    public PrefetchIterator(NextElementFunctor<E> aEnum) {
        innerEnum = aEnum;
    }



    private E getNextElementFromInnerFunctor() {
        innerFunctorUsageCounter++;
        E result = this.innerEnum.nextElement();



        flagIsEnumerationStartedEmpty = false;
        return result;
    }


    @Override
    public E nextElement() {
        E result = null;
        if (this.isGetNextLastResultUpToDate) {
            result = this.getNextLastResult;
        } else {
            result = getNextElementFromInnerFunctor();
        }

        this.isGetNextLastResultUpToDate = false;
        return result;
    }


    @Override
    public boolean hasMoreElements() {
        if (endOfEnumerationReached) {
            return false;
        }

        if (isGetNextLastResultUpToDate) {
            return true;
        } else {
            try {
                this.getNextLastResult = getNextElementFromInnerFunctor();
                this.isGetNextLastResultUpToDate = true;
                return true;
            } catch (NoSuchElementException noSuchE) {
                endOfEnumerationReached = true;
                return false;
            }
        }
    }


    public boolean isEnumerationStartedEmpty() {
        if (this.innerFunctorUsageCounter == 0) {
            if (hasMoreElements()) {
                return false;
            } else {
                return true;
            }
        } else


        {
            return flagIsEnumerationStartedEmpty;
        }
    }

    @Override
    public boolean hasNext() {
        return this.hasMoreElements();
    }

    @Override
    public E next() {
        return this.nextElement();
    }


    @Override
    public void remove()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }


    public interface NextElementFunctor<EE> {

        public EE nextElement()
                throws NoSuchElementException;
    }
}

class ArrayUnenforcedSet<E>
        extends ArrayList<E>
        implements Set<E> {

    private static final long serialVersionUID = -7413250161201811238L;

    public ArrayUnenforcedSet() {
        super();
    }

    public ArrayUnenforcedSet(Collection<? extends E> c) {
        super(c);
    }

    public ArrayUnenforcedSet(int n) {
        super(n);
    }


    @Override
    public boolean equals(Object o) {
        return new SetForEquality().equals(o);
    }

    @Override
    public int hashCode() {
        return new SetForEquality().hashCode();
    }

    private class SetForEquality
            extends AbstractSet<E> {
        @Override
        public Iterator<E> iterator() {
            return ArrayUnenforcedSet.this.iterator();
        }

        @Override
        public int size() {
            return ArrayUnenforcedSet.this.size();
        }
    }
}

class MaskVertexSet<V, E>
        extends AbstractSet<V> {


    private MaskFunctor<V, E> mask;

    private int size;

    private Set<V> vertexSet;

    private transient TypeUtil<V> vertexTypeDecl = null;


    public MaskVertexSet(Set<V> vertexSet, MaskFunctor<V, E> mask) {
        this.vertexSet = vertexSet;
        this.mask = mask;
        this.size = -1;
    }



    @Override
    public boolean contains(Object o) {
        return
                !this.mask.isVertexMasked(TypeUtil.uncheckedCast(o, vertexTypeDecl))
                        && this.vertexSet.contains(o);
    }


    @Override
    public Iterator<V> iterator() {
        return new PrefetchIterator<V>(new MaskVertexSetNextElementFunctor());
    }


    @Override
    public int size() {
        if (this.size == -1) {
            this.size = 0;
            for (Iterator<V> iter = iterator(); iter.hasNext(); ) {
                iter.next();
                this.size++;
            }
        }
        return this.size;
    }


    private class MaskVertexSetNextElementFunctor
            implements PrefetchIterator.NextElementFunctor<V> {
        private Iterator<V> iter;

        public MaskVertexSetNextElementFunctor() {
            this.iter = MaskVertexSet.this.vertexSet.iterator();
        }

        @Override
        public V nextElement()
                throws NoSuchElementException {
            V element = this.iter.next();
            while (MaskVertexSet.this.mask.isVertexMasked(element)) {
                element = this.iter.next();
            }
            return element;
        }
    }
}
