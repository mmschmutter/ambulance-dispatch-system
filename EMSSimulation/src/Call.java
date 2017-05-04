/**
 * Call object
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class Call implements Comparable<Call> {
    private int addressID;
    private int severity;

    Call(int addressID, int severity) {
        this.addressID = addressID;
        this.severity = severity;
    }

    int getAddressID() {
        return this.addressID;
    }

    int getSeverity() {
        return this.severity;
    }

    public String toString() {
        return this.addressID + " (" + this.severity + ")";
    }

    public int compareTo(Call otherCall) {
        if (this.severity > otherCall.getSeverity()) {
            return -1;
        } else if (this.severity < otherCall.getSeverity()) {
            return 1;
        }
        return 0;
    }
}