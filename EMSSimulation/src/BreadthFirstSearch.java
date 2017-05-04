import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Breadth First Search for Graph
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class BreadthFirstSearch {
    private HashMap<Integer, Boolean> visited;
    private HashMap<Integer, Integer> previousVertex;
    private LinkedHashMap<Integer, Integer> distanceTo;
    private Graph graph;
    private int origin;
    private HashSet<Integer> compoundOrigin;
    private boolean multiBFS;

    BreadthFirstSearch(Graph graph, int origin) {
        this.multiBFS = false;
        this.graph = graph;
        this.origin = origin;
        this.previousVertex = new HashMap<Integer, Integer>();
        this.distanceTo = new LinkedHashMap<Integer, Integer>();
        this.visited = new HashMap<Integer, Boolean>();
        for (Integer ID : this.graph.getVertices().keySet()) {
            visited.put(ID, false);
        }
        bfs();
    }

    BreadthFirstSearch(Graph graph, HashSet<Integer> compoundOrigin) {
        this.multiBFS = true;
        this.graph = graph;
        this.compoundOrigin = compoundOrigin;
        this.previousVertex = new HashMap<Integer, Integer>();
        this.distanceTo = new LinkedHashMap<Integer, Integer>();
        this.visited = new HashMap<Integer, Boolean>();
        for (Integer ID : this.graph.getVertices().keySet()) {
            visited.put(ID, false);
        }
        mbfs();
    }

    private void bfs() {
        Queue<Integer> q = new LinkedBlockingQueue<Integer>();
        q.add(this.origin);
        this.visited.put(this.origin, true);
        this.distanceTo.put(this.origin, 0);
        while (!q.isEmpty()) {
            Vertex vertex = this.graph.getVertices().get(q.remove());
            for (Edge e : vertex.getEdges()) {
                if (e.isBroken()) {
                    continue;
                }
                int otherVertex = e.getAdjacent(vertex).getID();
                if (!this.visited.get(otherVertex)) {
                    q.add(otherVertex);
                    this.visited.put(otherVertex, true);
                    this.previousVertex.put(otherVertex, vertex.getID());
                    this.distanceTo.put(otherVertex, this.distanceTo.get(vertex.getID()) + 1);
                }
            }
        }
    }

    private void mbfs() {
        Queue<Integer> q = new LinkedBlockingQueue<Integer>();
        for (int vertexID : this.compoundOrigin) {
            q.add(vertexID);
            this.visited.put(vertexID, true);
            this.distanceTo.put(vertexID, 0);
        }
        while (!q.isEmpty()) {
            Vertex vertex = this.graph.getVertices().get(q.remove());
            for (Edge e : vertex.getEdges()) {
                int otherVertex = e.getAdjacent(vertex).getID();
                if (!this.visited.get(otherVertex)) {
                    q.add(otherVertex);
                    this.visited.put(otherVertex, true);
                    this.previousVertex.put(otherVertex, vertex.getID());
                    this.distanceTo.put(otherVertex, this.distanceTo.get(vertex.getID()) + 1);
                }
            }
        }
    }

    boolean hasPathTo(int vertex) {
        return this.visited.get(vertex);
    }

    Iterable<Integer> pathTo(int destination) {
        if (!hasPathTo(destination)) {
            return null;
        }
        Stack<Integer> path = new Stack<Integer>();
        if (this.multiBFS) {
            for (int x = destination; !this.compoundOrigin.contains(x); x = this.previousVertex.get(x)) {
                path.push(x);
            }
            for (Edge edge : this.graph.getVertex(path.peek()).getEdges()) {
                int otherVertex = edge.getAdjacent(this.graph.getVertex(path.peek())).getID();
                if (this.compoundOrigin.contains(otherVertex)) {
                    path.push(otherVertex);
                    break;
                }
            }
        } else {
            for (int x = destination; x != this.origin; x = this.previousVertex.get(x)) {
                path.push(x);
            }
            path.push(this.origin);
        }
        Collections.reverse(path);
        return path;
    }

    LinkedList<Edge> roadsTo(int destination) {
        if (!hasPathTo(destination)) {
            return null;
        }
        Stack<Integer> path = new Stack<Integer>();
        if (this.multiBFS) {
            if (this.compoundOrigin.contains(destination)) {
                return null;
            }
            for (int x = destination; !this.compoundOrigin.contains(x); x = this.previousVertex.get(x)) {
                path.push(x);
            }
            for (Edge edge : this.graph.getVertex(path.peek()).getEdges()) {
                int otherVertex = edge.getAdjacent(this.graph.getVertex(path.peek())).getID();
                if (this.compoundOrigin.contains(otherVertex)) {
                    path.push(otherVertex);
                    break;
                }
            }
        } else {
            for (int x = destination; x != this.origin; x = this.previousVertex.get(x)) {
                path.push(x);
            }
            path.push(this.origin);
        }
        LinkedList<Edge> roads = new LinkedList<Edge>();
        int prev = -1;
        while (!path.isEmpty()) {
            int current = path.pop();
            if (prev == -1) {
                prev = current;
            } else {
                String edgeID;
                if (prev < current) {
                    edgeID = prev + "-" + current;
                } else {
                    edgeID = current + "-" + prev;
                }
                roads.add(this.graph.getEdge(edgeID));
                prev = current;
            }
        }
        return roads;
    }

    Integer visitedFirst(Vertex a, Vertex b, int destination) {
        Iterable<Integer> path = this.pathTo(destination);
        for (int ID : path) {
            if (ID == a.getID() || ID == b.getID()) {
                return ID;
            }
        }
        return null;
    }

    int distanceTo(int destination) {
        return this.distanceTo.get(destination);
    }

    Integer distanceTo(HashSet<Vertex> destination) {
        for (int vertex : this.distanceTo.keySet()) {
            if (destination.contains(vertex)) {
                return this.distanceTo(vertex);
            }
        }
        return null;
    }
}
