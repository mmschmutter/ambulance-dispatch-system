import java.util.HashMap;
/**
 * Finds Connected Components using Depth First Search
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class ConnectedComponents {
    private HashMap<Integer, Boolean> visited;
    private HashMap<Integer, Integer> componentIDs;
    private HashMap<Integer, Integer> hospitalAmounts;
    private HashMap<Integer, Integer> unitAmounts;
    private Graph graph;
    private int componentCount;

    ConnectedComponents(Graph graph) {
        this.componentCount = 0;
        this.graph = graph;
        this.componentIDs = new HashMap<Integer, Integer>();
        this.visited = new HashMap<Integer, Boolean>();
        this.hospitalAmounts = new HashMap<Integer, Integer>();
        this.unitAmounts = new HashMap<Integer, Integer>();
        for (Integer ID : this.graph.getVertices().keySet()) {
            this.visited.put(ID, false);
        }
        for (Integer ID : this.graph.getVertices().keySet()) {
            if (!this.visited.get(ID)) {
                this.componentCount++;
                dfs(ID);
            }
        }
        for (int i = 1; i < this.componentCount + 1; i++) {
            this.hospitalAmounts.put(i, 0);
            this.unitAmounts.put(i, 0);
        }
        for (int hospital : this.graph.getHospitals()) {
            int component = this.componentIDs.get(hospital);
            int hospitalCount = this.hospitalAmounts.get(component) + 1;
            this.hospitalAmounts.put(component, hospitalCount);
        }
        for (EMSUnit unit : this.graph.getEMSUnits()) {
            int unitLocation = unit.getLocation();
            int component = this.componentIDs.get(unitLocation);
            int unitCount = this.unitAmounts.get(component) + 1;
            this.unitAmounts.put(component, unitCount);
        }
    }

    private void dfs(int vertexID) {
        this.visited.put(vertexID, true);
        this.componentIDs.put(vertexID, this.componentCount);
        Vertex vertex = this.graph.getVertices().get(vertexID);
        for (Edge e : vertex.getEdges()) {
            if (e.isBroken()) {
                continue;
            }
            int otherVertex = e.getAdjacent(vertex).getID();
            if (!this.visited.get(otherVertex)) {
                this.dfs(otherVertex);
            }
        }
    }

    boolean areConnected(int vertexID, int otherVertexID) {
        return this.componentIDs.get(vertexID).equals(this.componentIDs.get(otherVertexID));
    }

    int getComponentID(int vertexID) {
        return this.componentIDs.get(vertexID);
    }

    int numberOfComponents() {
        return this.componentCount;
    }

    int getHospitalAmount(int componentID) {
        return this.hospitalAmounts.get(componentID);
    }

    int getUnitAmount(int componentID) {
        return this.unitAmounts.get(componentID);
    }
}
