import java.util.LinkedList;
/**
 * Vertex implementation
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class Vertex {
    private LinkedList<Edge> edges;
    private Integer ID;

    Vertex(int ID) {
        this.ID = ID;
        this.edges = new LinkedList<Edge>();
    }

    void addEdge(Edge edge) {
        if (!this.edges.contains(edge)) {
            this.edges.add(edge);
        }
    }

    void addEdges(LinkedList<Edge> edges) {
        for (Edge edge : edges) {
            if (!this.edges.contains(edge)) {
                this.edges.add(edge);
            }
        }
    }

    boolean containsEdge(Edge edge) {
        return this.edges.contains(edge);
    }

    Integer getID() {
        return this.ID;
    }

    LinkedList<Edge> getEdges() {
        return this.edges;
    }

    public String toString() {
        return "" + ID;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Vertex)) {
            return false;
        }
        Vertex v = (Vertex) other;
        return this.ID.equals(v.ID);
    }
}