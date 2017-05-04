/**
 * EMS Unit object
 *
 * @author Mordechai Schmutter
 * @version 1.0
 */
class EMSUnit {
    private int ID;
    private int location;

    EMSUnit(int ID, int location) {
        this.ID = ID;
        this.location = location;
    }

    int getID() {
        return this.ID;
    }

    int getLocation() {
        return this.location;
    }

    void setLocation(int location) {
        this.location = location;
    }
}
