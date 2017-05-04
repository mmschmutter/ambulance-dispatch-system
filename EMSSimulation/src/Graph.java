import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
/**
 * Graph implementation
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class Graph {
    private HashMap<Integer, Vertex> vertices;
    private HashMap<String, Edge> edges;
    private LinkedBlockingQueue<EMSUnit> EMSUnits;
    private HashSet<Integer> hospitals;

    Graph(HashMap<String, Integer> addressMap, HashMap<String, TreeSet<Integer>> addresses) {
        this.vertices = new HashMap<Integer, Vertex>();
        this.edges = new HashMap<String, Edge>();
        this.EMSUnits = new LinkedBlockingQueue<EMSUnit>();
        this.hospitals = new HashSet<Integer>();
        for (int ID : addressMap.values()) {
            this.vertices.put(ID, new Vertex(ID));
        }
        for (String street : addresses.keySet()) {
            TreeSet<Integer> houseNums = addresses.get(street);
            boolean first = true;
            int prev = 0;
            for (int houseNum : houseNums) {
                if (first == true) {
                    prev = houseNum;
                    first = false;
                } else {
                    Vertex a = this.vertices.get(addressMap.get(prev + " " + street));
                    Vertex b = this.vertices.get(addressMap.get(houseNum + " " + street));
                    addEdge(a, b);
                    prev = houseNum;
                }
            }
        }
    }

    Edge addEdge(Vertex a, Vertex b) {
        if (a.equals(b)) {
            throw new IllegalArgumentException("Cannot add Edge from Vertex to itself");
        }
        Edge e = new Edge(a, b);
        if (this.edges.values().contains(e) || a.containsEdge(e) || b.containsEdge(e)) {
            return this.getEdge(e.getID());
        }
        System.out.println("Adding Edge " + e.getID() + " between Vertex " + e.getA() + " and Vertex " + e.getB());
        this.edges.put(e.getID(), e);
        a.addEdge(e);
        b.addEdge(e);
        return e;
    }

    Edge getEdge(String edgeID) {
        return this.edges.get(edgeID);
    }

    Vertex getVertex(int ID) {
        return this.vertices.get(ID);
    }

    HashMap<Integer, Vertex> getVertices() {
        return this.vertices;
    }

    void addHospital(int location) {
        this.hospitals.add(location);
    }

    HashSet<Integer> getHospitals() {
        return this.hospitals;
    }

    void queueEMSUnit(EMSUnit unit) {
        this.EMSUnits.offer(unit);
    }

    EMSUnit dequeueEMSUnit() {
        return this.EMSUnits.poll();
    }

    int numberOfUnits() {
        return this.EMSUnits.size();
    }

    EMSUnit currentEMSUnit() {
        return this.EMSUnits.peek();
    }

    EMSUnit nextEMSUnit() {
        Object[] EMSArray = this.EMSUnits.toArray();
        return (EMSUnit) EMSArray[1];
    }

    LinkedBlockingQueue<EMSUnit> getEMSUnits() {
        return this.EMSUnits;
    }

    public String toString() {
        StringBuilder s = new StringBuilder();
        for (Vertex v : this.vertices.values()) {
            s.append(v + ": ");
            for (Edge e : v.getEdges()) {
                s.append(e.getAdjacent(v) + " ");
            }
            for (Edge e : v.getEdges()) {
                if (e.isBroken()) {
                    s.append("(" + e + ") ");
                }
            }
            s.append("\n");
        }
        return s.toString();
    }
}

