import java.io.*;
import java.util.*;
/**
 * Parses input file, builds Graph, and executes events
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
public class Simulator {
    private HashMap<Integer, String> IDMap;
    private HashMap<String, Integer> addressMap;
    private HashMap<String, TreeSet<Integer>> addresses;
    private Graph city;
    private int unitNum;
    private CallQueue calls;
    private CallQueue unreachableCalls;
    private HashSet<BrokenRoad> brokenRoads;
    private HashMap<Integer, Integer> repairTeams;
    private HashMap<Integer, BrokenRoad> fixing;
    private int repairNum;


    public Simulator() {
        this.IDMap = new HashMap<Integer, String>();
        this.addressMap = new HashMap<String, Integer>();
        this.addresses = new HashMap<String, TreeSet<Integer>>();
        this.calls = new CallQueue();
        this.unreachableCalls = new CallQueue();
        this.unitNum = 0;
        this.brokenRoads = new HashSet<BrokenRoad>();
        this.repairTeams = new HashMap<Integer, Integer>();
        this.fixing = new HashMap<Integer, BrokenRoad>();
        this.repairNum = 0;
    }

    public void parse(String file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            for (String line; (line = br.readLine()) != null; ) {
                if (!line.contains(",")) {
                    String[] input = line.split("\\s+");
                    if (isNum(input[0]) && isNum(input[1])) { // new address input
                        int ID = Integer.parseInt(input[0]);
                        int houseNum = Integer.parseInt(input[1]);
                        String street = "";
                        for (int i = 2; i < input.length; i++) {
                            street += input[i] + " ";
                        }
                        street = street.substring(0, street.length() - 1);
                        System.out.println("Adding '" + houseNum + " " + street + "' as Vertex " + ID);
                        this.IDMap.put(ID, houseNum + " " + street);
                        this.addressMap.put(houseNum + " " + street, ID);
                        if (this.addresses.containsKey(street)) {
                            TreeSet<Integer> houseNums = this.addresses.get(street);
                            houseNums.add(houseNum);
                            this.addresses.put(street, houseNums);
                        } else {
                            TreeSet<Integer> houseNums = new TreeSet<Integer>();
                            houseNums.add(houseNum);
                            this.addresses.put(street, houseNums);
                        }
                        br.mark(1000);
                        String next = br.readLine();
                        if (next.contains(",")) { // check if address input is finished
                            this.city = new Graph(this.addressMap, this.addresses);
                        }
                        br.reset();
                    } else if (input[0].equals("hospital")) { // new hospital input
                        System.out.println("Adding hospital at " + this.IDMap.get(Integer.parseInt(input[1])));
                        this.city.addHospital(Integer.parseInt(input[1]));
                    } else if (input[0].equals("EMS")) {
                        this.unitNum++;
                        System.out.println("Adding EMS Unit " + this.unitNum + " at " + this.IDMap.get(Integer.parseInt(input[1])));
                        this.city.queueEMSUnit(new EMSUnit(this.unitNum, Integer.parseInt(input[1])));
                    } else if (input[0].equals("Begin911CallGroup")) { // new call group
                        for (String ln; !(ln = br.readLine()).equals("End911CallGroup"); ) {
                            String[] inpt = ln.split("\\s+");
                            String address = "";
                            for (int i = 0; i < inpt.length - 1; i++) {
                                address += inpt[i] + " ";
                            }
                            address = address.substring(0, address.length() - 1);
                            int addressID = this.addressMap.get(address);
                            Call call = new Call(addressID, Integer.parseInt(inpt[inpt.length - 1]));
                            this.calls.addCall(call);
                        }
                        this.handleCalls();
                    } else if (isNum(input[0]) && isNum(input[input.length - 1])) { // new single call
                        String address = "";
                        for (int i = 0; i < input.length - 1; i++) {
                            address += input[i] + " ";
                        }
                        address = address.substring(0, address.length() - 1);
                        int addressID = this.addressMap.get(address);
                        System.out.println("911 call received from " + address);
                        Call call = new Call(addressID, Integer.parseInt(input[input.length - 1]));
                        this.handleCall(call);
                    } else if (input[0].equals("brokenRoad")) { // new broken road input - assuming input includes "brokenRoad" label
                        int start = Integer.parseInt(input[1]);
                        int end = Integer.parseInt(input[2]);
                        String street = "";
                        for (int i = 3; i < input.length; i++) {
                            street += input[i] + " ";
                        }
                        street = street.substring(0, street.length() - 1);
                        NavigableSet<Integer> houseNums = this.addresses.get(street).subSet(start, true, end, true);
                        HashSet<Edge> edges = new HashSet<Edge>();
                        boolean first = true;
                        int prev = 0;
                        for (int houseNum : houseNums) {
                            if (first) {
                                prev = houseNum;
                                first = false;
                            } else {
                                int a = this.addressMap.get(prev + " " + street);
                                int b = this.addressMap.get(houseNum + " " + street);
                                String edgeID;
                                if (a < b) {
                                    edgeID = a + "-" + b;
                                } else {
                                    edgeID = b + "-" + a;
                                }
                                edges.add(this.city.getEdge(edgeID));
                                prev = houseNum;
                            }
                        }
                        BrokenRoad road = new BrokenRoad(start + " " + street, end + " " + street, edges);
                        this.brokenRoads.add(road);
                        System.out.println("Road broken between " + start + " and " + end + " " + street);
                    } else if (input[0].equals("repair")) { // new repair team input
                        this.repairNum++;
                        System.out.println("Repair Team " + this.repairNum + " arrived");
                        this.assignRepairs(this.repairNum);
                    }
                } else if (line.contains(",")) { // new road connections input
                    String[] input = line.split(",");
                    LinkedList<Edge> edges = new LinkedList<Edge>();
                    Vertex a;
                    Vertex b;
                    for (int i = 0; i < input.length; i++) {
                        a = this.city.getVertex(Integer.parseInt(input[i]));
                        for (int j = i + 1; j < input.length; j++) {
                            b = this.city.getVertex(Integer.parseInt(input[j]));
                            edges.add(this.city.addEdge(a, b));
                        }
                    }
                    if (input.length == 4) { // if this is an intersection
                        for (Edge edge : edges) {
                            edge.addIntersection(edges);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleCalls() {
        LinkedList<Call> allCalls = this.calls.peekAt(this.calls.size());
        String list = "\n911 Call Group:\n";
        for (Call call : allCalls) {
            list += "(" + call.getSeverity() + ") " + this.IDMap.get(call.getAddressID()) + "\n";
        }
        System.out.println(list); // print original call list
        int firstUnit = -1;
        while (!this.calls.isEmpty()) { // while there are calls left in the queue
            LinkedList<Call> topCalls = this.calls.peekAt(this.city.numberOfUnits()); // look at the same number of calls as there are units
            EMSUnit unit = this.city.currentEMSUnit();
            BreadthFirstSearch bfs = new BreadthFirstSearch(this.city, unit.getLocation());
            Call closest = null;
            for (Call call : topCalls) {
                if (bfs.hasPathTo(call.getAddressID())) {
                    if (closest == null || bfs.distanceTo(call.getAddressID()) < bfs.distanceTo(closest.getAddressID())) { // get call closest to current unit
                        closest = call;
                    }
                }
            }
            outer:
            if (closest != null) {
                System.out.println("EMS Unit " + unit.getID() + " dispatched from " + this.IDMap.get(unit.getLocation()) + " to " + this.IDMap.get(closest.getAddressID()));
                for (Edge edge : bfs.roadsTo(closest.getAddressID())) { // check if unit encounters any broken roads
                    for (BrokenRoad road : this.brokenRoads) {
                        if (road.includes(edge)) {
                            int stopped = bfs.visitedFirst(edge.getA(), edge.getB(), closest.getAddressID());
                            System.out.println("EMS Unit " + unit.getID() + " stopped at " + this.IDMap.get(stopped));
                            System.out.println("Broken road discovered between " + road.getStart() + " and " + road.getEnd());
                            road.breakRoad();
                            road.setDiscovered();
                            unit.setLocation(stopped);
                            this.checkForRepairTeams();
                            break outer;
                        }
                    }
                }
                unit.setLocation(closest.getAddressID()); // unit successfully reached call
                this.calls.removeCall(closest); // remove the call from the queue
                this.printCalls(); // print remaining calls
                this.city.queueEMSUnit(this.city.dequeueEMSUnit()); // rotate to the next unit
                firstUnit = -1;
                this.incrementTime(); // time it takes for repair teams to repair roads
            } else if (this.city.nextEMSUnit().getID() != firstUnit) { // if some units haven't tried to reach the call
                if (topCalls.size() == 1) {
                    System.out.println("EMS Unit " + unit.getID() + " cannot currently reach the top call");
                } else {
                    System.out.println("EMS Unit " + unit.getID() + " cannot currently reach any of the top " + topCalls.size() + " calls");
                }
                if (firstUnit == -1) {
                    firstUnit = this.city.currentEMSUnit().getID(); // mark unit as first to try and fail
                }
                this.city.queueEMSUnit(this.city.dequeueEMSUnit()); // rotate to the next unit
            } else { // all units exhausted and call not reached
                if (topCalls.size() == 1) {
                    System.out.println("EMS Unit " + unit.getID() + " cannot currently reach the top call");
                    System.out.println("Top call put on hold since it is unreachable");
                } else {
                    System.out.println("EMS Unit " + unit.getID() + " cannot currently reach any of the top " + topCalls.size() + " calls");
                    System.out.println("Top " + topCalls.size() + " calls put on hold since they are unreachable");
                }
                for (int i = 0; i < topCalls.size(); i++) {
                    this.unreachableCalls.addCall(this.calls.popCall()); // will try all top calls again after fixing roads
                }
                this.city.queueEMSUnit(this.city.dequeueEMSUnit()); // rotate to the next unit
                firstUnit = -1;
                this.printCalls(); // print remaining calls
            }
        }
    }

    private void handleCall(Call call) {
        boolean resolved = false;
        int firstUnit = -1;
        while (this.city.currentEMSUnit().getID() != firstUnit) { // while some units haven't tried to reach the call
            EMSUnit unit = this.city.currentEMSUnit();
            BreadthFirstSearch bfs = new BreadthFirstSearch(this.city, unit.getLocation());
            outer:
            if (bfs.hasPathTo(call.getAddressID())) {
                System.out.println("EMS Unit " + unit.getID() + " dispatched from " + this.IDMap.get(unit.getLocation()) + " to " + this.IDMap.get(call.getAddressID()));
                for (Edge edge : bfs.roadsTo(call.getAddressID())) { // check if unit encounters any broken roads
                    for (BrokenRoad road : this.brokenRoads) {
                        if (road.includes(edge)) {
                            int stopped = bfs.visitedFirst(edge.getA(), edge.getB(), call.getAddressID());
                            System.out.println("EMS Unit " + unit.getID() + " stopped at " + this.IDMap.get(stopped));
                            System.out.println("Broken road discovered between " + road.getStart() + " and " + road.getEnd());
                            road.breakRoad();
                            road.setDiscovered();
                            unit.setLocation(stopped);
                            this.checkForRepairTeams();
                            break outer;
                        }
                    }
                }
                unit.setLocation(call.getAddressID()); // unit successfully reached call
                this.city.queueEMSUnit(this.city.dequeueEMSUnit()); // rotate to the next unit
                resolved = true;
                this.incrementTime(); // time it takes for repair teams to repair roads
                break;
            } else { // unit has no path to call
                System.out.println("EMS Unit " + unit.getID() + " cannot reach " + this.IDMap.get(call.getAddressID()) + " from " + this.IDMap.get(unit.getLocation()));
                if (firstUnit == -1) {
                    firstUnit = unit.getID(); // mark unit as first to try and fail
                }
                this.city.queueEMSUnit(this.city.dequeueEMSUnit()); //rotate to the next unit
            }
        }
        if (!resolved) { // all units exhausted and call not reached
            System.out.println("911 call put on hold since it is unreachable");
            this.unreachableCalls.addCall(call); // will try again after fixing roads
        }
    }

    private void printCalls() {
        if (!this.calls.isEmpty()) {
            LinkedList<Call> remainingCalls = this.calls.peekAt(this.calls.size());
            String list = "\nRemaining 911 Calls:\n";
            for (Call call : remainingCalls) {
                list += "(" + call.getSeverity() + ") " + this.IDMap.get(call.getAddressID()) + "\n";
            }
            System.out.println(list);
        }
    }

    private void incrementTime() {
        if (!this.repairTeams.isEmpty()) { // if there are repair teams on the map
            for (int repairTeam : this.repairTeams.keySet()) {
                if (this.fixing.get(repairTeam) != null) { // if the repair team is working on a road
                    int time = this.repairTeams.get(repairTeam);
                    time++; // increment time working on that road by one
                    this.repairTeams.put(repairTeam, time);
                }
            }
            for (int repairTeam : this.repairTeams.keySet()) {
                if (this.repairTeams.get(repairTeam) == (2 * this.city.numberOfUnits())) { // if the repair team has worked on the road for 2 * number of EMS units amount of time
                    BrokenRoad finishedRoad = this.fixing.get(repairTeam);
                    System.out.println("Repair Team " + repairTeam + " fixed road between " + finishedRoad.getStart() + " and " + finishedRoad.getEnd());
                    finishedRoad.fixRoad();
                    this.brokenRoads.remove(finishedRoad);
                    this.checkForRepairTeams(); // allow standby repair teams to step up if they can
                    this.assignRepairs(repairTeam); // assign the next road for the unit to repair
                }
            }
        }
    }

    private void assignRepairs(int repairTeam) {
        int brokenCount = 0;
        for (BrokenRoad road : this.brokenRoads) {
            if (road.wasDiscovered() && !road.isBeingFixed()) {
                brokenCount++;
            }
        }
        if (brokenCount == 0) { // if there are no discovered broken roads that aren't being fixed
            System.out.println("City does not have any known broken roads");
            this.repairTeams.put(repairTeam, 0); // initialize time count to zero
            this.fixing.put(repairTeam, null); // put repair team on standby
            this.retryCalls(); // retry previously unreachable calls
            return;
        }
        ConnectedComponents cc = new ConnectedComponents(this.city);
        if (cc.numberOfComponents() == 1) { // if there are no isolated components
            System.out.println("City does not have any areas isolated by broken roads");
            BrokenRoad closestRoad = null;
            Integer roadDistance = -1;
            for (int hospital : this.city.getHospitals()) {
                BreadthFirstSearch bfs = new BreadthFirstSearch(this.city, hospital);
                for (BrokenRoad road : this.brokenRoads) {
                    if (road.wasDiscovered() && !road.isBeingFixed()) {
                        if (roadDistance == -1 || bfs.distanceTo(road.getVertices()) < roadDistance) { // find road closest to a hospital
                            closestRoad = road;
                            roadDistance = bfs.distanceTo(road.getVertices());
                        }
                    }
                }
            }
            System.out.println("Repair Team " + repairTeam + " working on road between " + closestRoad.getStart() + " and " + closestRoad.getEnd());
            closestRoad.startFixing(); // mark road as being worked on
            this.repairTeams.put(repairTeam, 0); // initialize time count to zero
            this.fixing.put(repairTeam, closestRoad); // assign repair team to road
            this.retryCalls(); // retry previously unreachable calls
        } else { // there are isolated components
            System.out.println("City has " + cc.numberOfComponents() + " areas isolated by broken roads");
            int emptiestComponent = -1;
            int leastHelp = -1;
            for (int i = 1; i < cc.numberOfComponents() + 1; i++) {
                int help = cc.getUnitAmount(i) + cc.getHospitalAmount(i);
                if (leastHelp == -1 || help < leastHelp) { // find component with the least amount of hospitals and EMS units
                    emptiestComponent = i;
                    leastHelp = help;
                }
            }
            HashSet<Integer> component = new HashSet<Integer>(); // collect all vertices in component into a set
            for (int vertexID : this.city.getVertices().keySet()) {
                if (cc.getComponentID(vertexID) == emptiestComponent) {
                    component.add(vertexID);
                }
            }
            BreadthFirstSearch bfs = new BreadthFirstSearch(this.city, component); // perform BFS from entire component
            int easiestHelp = -1;
            int leastBroken = -1;
            for (int hospital : this.city.getHospitals()) {
                if (cc.getComponentID(hospital) != emptiestComponent) {
                    int brokenOnPath = 0;
                    for (BrokenRoad brokenRoad : this.brokenRoads) {
                        if (brokenRoad.wasDiscovered() && !brokenRoad.isBeingFixed()) {
                            for (Edge edge : bfs.roadsTo(hospital)) {
                                if (brokenRoad.includes(edge)) { // check if broken road must be traversed to reach hospital
                                    brokenOnPath++;
                                    break;
                                }
                            }
                        }
                    }
                    if (leastBroken == -1 || brokenOnPath < leastBroken) { // find hospital that requires least traversal of broken roads
                        easiestHelp = hospital;
                        leastBroken = brokenOnPath;
                    }
                }
            }
            for (EMSUnit unit : this.city.getEMSUnits()) {
                if (cc.getComponentID(unit.getLocation()) != emptiestComponent) {
                    int brokenOnPath = 0;
                    for (BrokenRoad brokenRoad : this.brokenRoads) {
                        if (brokenRoad.wasDiscovered() && !brokenRoad.isBeingFixed()) {
                            for (Edge edge : bfs.roadsTo(unit.getLocation())) {
                                if (brokenRoad.includes(edge)) { // check if broken road must be traversed to reach EMS unit
                                    brokenOnPath++;
                                    break;
                                }
                            }
                        }
                    }
                    if (leastBroken == -1 || brokenOnPath < leastBroken) { // find EMS unit that requires less traversal of broken roads than least hospital
                        easiestHelp = unit.getLocation();
                        leastBroken = brokenOnPath;
                    }
                }
            }
            BrokenRoad roadToFix = null;
            outer:
            for (Edge edge : bfs.roadsTo(easiestHelp)) {
                for (BrokenRoad brokenRoad : this.brokenRoads) {
                    if (brokenRoad.wasDiscovered() && !brokenRoad.isBeingFixed() && brokenRoad.includes(edge)) {
                        roadToFix = brokenRoad; // find first broken road that must be traversed to reach hospital/EMS unit
                        break outer;
                    }
                }
            }
            if (roadToFix == null) { // component with the least hospitals and EMS units is already being helped to the fullest
                System.out.println("Most helpless area is already being maximally repaired");
                this.repairTeams.put(repairTeam, 0); // initialize time count to zero
                this.fixing.put(repairTeam, null); // put repair team on standby
            } else { // component with the least hospitals and EMS units needs the help of this repair team
                System.out.println("Repair Team " + repairTeam + " working on road between " + roadToFix.getStart() + " and " + roadToFix.getEnd());
                roadToFix.startFixing(); // mark road as being worked on
                this.repairTeams.put(repairTeam, 0); // initialize time count to zero
                this.fixing.put(repairTeam, roadToFix); // assign repair team to road
            }
        }
    }

    private void retryCalls() {
        if (!this.unreachableCalls.isEmpty()) { // if there are calls that still have not been reached
            if (!this.calls.isEmpty()) { // if there is an active call queue
                System.out.println("Adding previously unreachable calls to current call group");
                while (!this.unreachableCalls.isEmpty()) {
                    this.calls.addCall(this.unreachableCalls.popCall()); // add unreached calls to active call queue
                }
                this.printCalls(); // print remaining calls in call group
            } else { // there is no active call group
                System.out.println("Retrying previously unreachable calls");
                while (!this.unreachableCalls.isEmpty()) {
                    this.calls.addCall(this.unreachableCalls.popCall()); // add unreached calls to call queue
                }
                this.handleCalls(); // activate call queue
            }
        }
    }

    private void checkForRepairTeams() {
        for (int repairTeam : this.repairTeams.keySet()) {
            if (this.fixing.get(repairTeam) == null) { // if repair team is on standby
                this.assignRepairs(repairTeam); // assign repair team to broken road
            }
        }
    }

    private boolean isNum(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }
}