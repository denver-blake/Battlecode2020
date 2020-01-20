package sprint;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Refinery implements Robot {

    private RobotController rc;
    private MapLocation hqLocation;

    public Refinery(RobotController rc) throws GameActionException {

        hqLocation = utils.hqPosition(rc);
        this.rc = rc;
        
    }

    public void run() throws GameActionException {
        if (rc.getRoundNum() > 500 && rc.getLocation().distanceSquaredTo(hqLocation) < 20) rc.disintegrate();

    }
}
