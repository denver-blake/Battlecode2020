package sprint;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class DesignSchool implements Robot {
    private RobotController rc;
    public DesignSchool(RobotController rc) {
        this.rc = rc;
    }
    public void run() throws GameActionException {
        if (rc.canBuildRobot(RobotType.LANDSCAPER, Direction.SOUTH)) {
            rc.buildRobot(RobotType.LANDSCAPER,Direction.SOUTH);
        }


    }
}
