package sprint;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Refinery implements Robot {

    private boolean sufficientMiners;
    private RobotController rc;

    public Refinery(RobotController rc) throws GameActionException {
        int[] transaction = {utils.BLOCKCHAIN_TAG, utils.NEW_REFINERY_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0};
        rc.submitTransaction(transaction, 10);
        sufficientMiners = false;

        this.rc = rc;
        
    }

    public void run() throws GameActionException {
        if(!sufficientMiners && utils.countNearbyMiners(rc) >= 4) {
            sufficientMiners = true;

            int[] transaction = {utils.BLOCKCHAIN_TAG, utils.FINISHED_REFINERY_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0};
            rc.submitTransaction(transaction, 10);
        }
    }
}
