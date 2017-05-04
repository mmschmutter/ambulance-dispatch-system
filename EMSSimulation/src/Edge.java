import java.util.LinkedList;
/**
 * Edge implementation
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class Edge {
    private Vertex a, b;
    private boolean broken;
    private String ID;
    private LinkedList<Edge> intersection;

    Edge(Vertex a, Vertex b) {
        this.a = (a.getID().compareTo(b.getID()) <= 0) ? a : b;
        this.b = (this.a == a) ? b : a;
        this.ID = this.a.getID() + "-" + this.b.getID();
        this.broken = false;
        this.intersection = new LinkedList<Edge>();
    }

    Vertex getAdjacent(Vertex current) {
        if (!(current.equals(a) || current.equals(b))) {
            return null;
        }
        return (current.equals(a)) ? b : a;
    }

    String getID() {
        return this.ID;
    }

    Vertex getA() {
        return this.a;
    }

    Vertex getB() {
        return this.b;
    }

    boolean isBroken() {
        return this.broken;
    }

    void breakEdge() {
        this.broken = true;
        for (Edge edge : this.intersection) {
            edge.isolatedBreak();
        }
    }

    private void isolatedBreak() {
        this.broken = true;
    }

    void fixEdge() {
        this.broken = false;
        for (Edge edge : this.intersection) {
            edge.isolatedFix();
        }
    }

    void isolatedFix() {
        this.broken = false;
    }

    void addIntersection(LinkedList<Edge> edges) {
        for (Edge edge : edges) {
            if (!edge.equals(this)) {
                this.intersection.add(edge);
            }
        }
    }

    LinkedList<Edge> getIntersection() {
        return this.intersection;
    }

    public String toString() {
        String string = "" + ID;
        if (broken) {
            string += ": BROKEN";
        }
        return string;
    }

    public boolean equals(Object other) {
        if (!(other instanceof Edge)) {
            return false;
        }
        Edge e = (Edge) other;
        return e.a.equals(this.a) && e.b.equals(this.b);
    }
}