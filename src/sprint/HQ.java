package sprint;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HQ implements Robot {

    private RobotController rc;
    private boolean builder = false;

    private boolean builtSchool = false;

    public void run() throws GameActionException {
        if(rc.getRoundNum() < 250 && rc.getRoundNum() % 10 == 0 && utils.tryBuild(rc, RobotType.MINER)) {

        }

        if(rc.getRoundNum() > 250 && !builtSchool && utils.tryBuild(rc, RobotType.MINER)) {
            builtSchool = true;
            //make the school here?
        }
    }

    public HQ (RobotController rc) throws GameActionException {
        this.rc = rc;

        int[] hqPosBlock = {utils.BLOCKCHAIN_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0, 0};
        rc.submitTransaction(hqPosBlock, 5);
    }
}
