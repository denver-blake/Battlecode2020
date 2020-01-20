package sprint;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Refinery implements Robot {

    private boolean sufficientMiners;
    private RobotController rc;
    private MapLocation hqLocation;

    public Refinery(RobotController rc) throws GameActionException {
        int[] transaction = {utils.BLOCKCHAIN_TAG, utils.NEW_REFINERY_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0};
        rc.submitTransaction(transaction, 10);
        sufficientMiners = false;
        hqLocation = utils.hqPosition(rc);
        this.rc = rc;
        
    }

    public void run() throws GameActionException {
        if (rc.getRoundNum() > 500 && rc.getLocation().distanceSquaredTo(hqLocation) < 9) rc.disintegrate();
        if(!sufficientMiners && utils.countNearbyMiners(rc) >= 4) {
            sufficientMiners = true;

            int[] transaction = {utils.BLOCKCHAIN_TAG, utils.FINISHED_REFINERY_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0};
            rc.submitTransaction(transaction, 10);
        }
    }
}
