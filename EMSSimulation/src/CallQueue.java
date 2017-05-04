import java.util.*;
/**
 * Custom Queue for Calls
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class CallQueue {
    private PriorityQueue<Call> calls;

    CallQueue() {
        this.calls = new PriorityQueue<Call>();
    }

    void addCall(Call call) {
        this.calls.offer(call);
    }

    LinkedList<Call> peekAt(int amount) {
        LinkedList<Call> result = new LinkedList<Call>();
        PriorityQueue<Call> copy = new PriorityQueue<Call>(this.calls);
        for (int i = 0; i < amount; i++) {
            Call call = copy.poll();
            if (call != null) {
                result.add(call);
            }
        }
        return result;
    }

    void removeCall(Call call) {
        this.calls.remove(call);
    }

    Call popCall() {
        return this.calls.poll();
    }

    boolean isEmpty() {
        return this.calls.isEmpty();
    }

    int size() {
        return this.calls.size();
    }
}
