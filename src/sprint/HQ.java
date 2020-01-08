package sprint;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class HQ implements Robot {

    private RobotController rc;
    private boolean builder = false;

    public void run() throws GameActionException {
        if(rc.getRoundNum() == 100) {
            rc.buildRobot(RobotType.MINER, Direction.NORTH);
        }
        if(rc.getRoundNum() == 200) {
            rc.buildRobot(RobotType.MINER, Direction.NORTH);
        }

    }

    public HQ (RobotController rc) throws GameActionException {
        this.rc = rc;

        int[] hqPosBlock = {utils.BLOCKCHAIN_TAG, rc.getLocation().x, rc.getLocation().y, 0, 0, 0, 0};
        rc.submitTransaction(hqPosBlock, 5);
    }
}
