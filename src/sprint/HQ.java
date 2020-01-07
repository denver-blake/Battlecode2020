package sprint;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class HQ implements Robot {

    private RobotController rc;

    public void run() throws GameActionException {

    }

    public HQ (RobotController rc) throws GameActionException {
        this.rc = rc;

        int[] hqPosBlock = {utils.BLOCKCHAIN_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0, 0}
        rc.submitTransaction(hqPosBlock, 5);
    }
}
