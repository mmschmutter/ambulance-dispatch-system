import java.util.HashSet;
/**
 * Broken Road object
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class BrokenRoad {
    private HashSet<Edge> edges;
    private HashSet<Vertex> vertices;
    boolean discovered;
    boolean beingFixed;
    String start;
    String end;

    BrokenRoad(String start, String end, HashSet<Edge> edges) {
        this.edges = edges;
        this.vertices = new HashSet<Vertex>();
        this.discovered = false;
        this.beingFixed = false;
        this.start = start;
        this.end = end;
        HashSet<Edge> intersections = new HashSet<Edge>();
        for (Edge edge : this.edges) {
            intersections.addAll(edge.getIntersection());
        }
        this.edges.addAll(intersections);
        for (Edge edge : this.edges) {
            this.vertices.add(edge.getA());
            this.vertices.add(edge.getB());
        }
    }

    boolean includes(Edge edge) {
        return this.edges.contains(edge);
    }

    void breakRoad() {
        for (Edge edge : this.edges) {
            edge.breakEdge();
        }
    }

    void fixRoad() {
        for (Edge edge : this.edges) {
            edge.fixEdge();
        }
    }

    void setDiscovered() {
        this.discovered = true;
    }

    boolean wasDiscovered() {
        return this.discovered;
    }

    String getStart() {
        return this.start;
    }

    String getEnd() {
        return this.end;
    }

    HashSet<Vertex> getVertices() {
        return this.vertices;
    }

    void startFixing() {
        this.beingFixed = true;
    }

    boolean isBeingFixed() {
        return this.beingFixed;
    }
}
