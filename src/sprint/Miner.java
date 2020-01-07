package sprint;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

import java.util.LinkedList;
import java.util.Queue;

public class Miner implements Robot {

    enum Mode {
        BUILD_REFINERY, //(param1, param2) = refinery location
        SCOUT_DEPOSIT, //param1 specifies direction to scout
        MINE_DEPOSIT, //(param1, param2) = deposit centroid
        BUILD_SCHOOL, //(param1, param2) = desired location
        BUILD_CENTER, //(param1, param2) = desired location
        BUILD_DEFENSIVE_GUN, //(param1, param2) = building location
        BUILD_VAPORATOR //(param1, param2) = desired location
    }

    private class Job {
        Mode mode;
        int param1, param2, param3, param4;

        public Job(Mode mode, int param1, int param2, int param3, int param4) {
            this.mode = mode;
            this.param1 = param1;
            this.param2 = param2;
            this.param3 = param3;
            this.param4 = param4;
        }
    }

    private Queue<Job> jobQueue;
    private RobotController rc;

    private MapLocation initialLocation;

    public Miner(RobotController rc) {
        this.rc = rc;

        jobQueue = new LinkedList<>();
        jobQueue.add(new Job(Mode.SCOUT_DEPOSIT, (int) (Math.random() * 8), 0, 0, 0));

        initialLocation = rc.getLocation();
    }

    public void run() throws GameActionException {
        if(!jobQueue.isEmpty()) {
            switch (jobQueue.peek().mode) {
                case BUILD_REFINERY:
                    break;
                case SCOUT_DEPOSIT:
                    scoutDeposit();
                    break;
                case MINE_DEPOSIT:
                    mineDeposit();
                    break;
                case BUILD_SCHOOL:
                    break;
                case BUILD_CENTER:
                    break;
                case BUILD_DEFENSIVE_GUN:
                    break;
                case BUILD_VAPORATOR:
                    break;
                default:
                    break;
            }
        }
    }

    private void scoutDeposit() throws GameActionException {
        //first check if we're currently ontop of soup
        if(rc.senseSoup(rc.getLocation()) > 0) {
            jobQueue.add(new Job(Mode.MINE_DEPOSIT, rc.getLocation().x, rc.getLocation().y, 0, 0));
            jobQueue.remove();
            rc.mineSoup(Direction.CENTER);
        } else {
            rc.move(utils.intToDirection(jobQueue.peek().param1));
        }
    }

    private void mineDeposit() throws GameActionException {
        if(rc.getSoupCarrying() > 0) {
            if (rc.getLocation().x == jobQueue.peek().param1 && rc.getLocation().y == jobQueue.peek().param2) {
                if(rc.canMineSoup(Direction.CENTER)) rc.mineSoup(Direction.CENTER);
            } else {
                utils.moveTowardsSimple(rc, new MapLocation(jobQueue.peek().param1, jobQueue.peek().param2));
            }
        } else {
            if(rc.getLocation().equals(initialLocation)) {
                if(rc.canDepositSoup(rc.getLocation().directionTo(utils.hqPosition(rc)))) {
                    rc.depositSoup(rc.getLocation().directionTo(utils.hqPosition(rc)), rc.getSoupCarrying());
                }
            } else {
                utils.moveTowardsSimple(rc, initialLocation);
            }
        }
    }
}
