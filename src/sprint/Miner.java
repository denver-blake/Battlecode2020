package sprint;

import battlecode.common.*;

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

        public Job(Mode mode) {
            this(mode, 0, 0, 0, 0);
        }
    }

    private Queue<Job> jobQueue;
    private RobotController rc;

    private MapLocation initialLocation;
    private MapLocation hqLocation;

    public Miner(RobotController rc) throws GameActionException {
        this.rc = rc;

        jobQueue = new LinkedList<>();
        jobQueue.add(new Job(Mode.SCOUT_DEPOSIT, (int) (Math.random() * 8), 0, 0, 0));

        initialLocation = rc.getLocation();
        for(Direction dir : Direction.allDirections()) {
            if(rc.senseRobotAtLocation(rc.getLocation().add(dir)).type == RobotType.HQ) {
                hqLocation = rc.getLocation().add(dir);
            }
        }
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
                    buildSchool();
                    break;
                case BUILD_CENTER:
                    buildCenter();
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

    private void buildCenter() throws GameActionException {
        if(jobQueue.peek().param3 == 1) {
            MapLocation centerLocation = new MapLocation(jobQueue.peek().param1, jobQueue.peek().param2);

            if(rc.getLocation().distanceSquaredTo(centerLocation) <= 2
                && rc.canBuildRobot(RobotType.FULFILLMENT_CENTER, rc.getLocation().directionTo(centerLocation))) {
                rc.buildRobot(RobotType.FULFILLMENT_CENTER, rc.getLocation().directionTo(centerLocation));
                jobQueue.remove();
                return;
            }
        }

        if(rc.getLocation().distanceSquaredTo(hqLocation) > 2) {
            utils.moveTowardsSimple(rc, hqLocation);
        } else {
            MapLocation centerLocation = utils.findHighGround(rc);
            jobQueue.peek().param1 = centerLocation.x;
            jobQueue.peek().param2 = centerLocation.y;
            jobQueue.peek().param3 = 1;
        }
    }

    private void buildSchool() throws GameActionException {
        if(jobQueue.peek().param3 == 1) {
            MapLocation schoolLocation = new MapLocation(jobQueue.peek().param1, jobQueue.peek().param2);

            if(rc.getLocation().distanceSquaredTo(schoolLocation) <= 2
                    && rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(schoolLocation))) {
                rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(schoolLocation));
                jobQueue.remove();
                return;
            }
        }

        if(rc.getLocation().distanceSquaredTo(hqLocation) > 2) {
            utils.moveTowardsSimple(rc, hqLocation);
        } else {
            MapLocation schoolLocation = utils.findHighGround(rc);
            jobQueue.peek().param1 = schoolLocation.x;
            jobQueue.peek().param2 = schoolLocation.y;
            jobQueue.peek().param3 = 1;
        }
    }

    private void scoutDeposit() throws GameActionException {
        //first check if we're currently near any soup
        MapLocation nearbySoup = utils.findNearbySoup(rc);
        if(nearbySoup != null) {
            jobQueue.add(new Job(Mode.MINE_DEPOSIT, nearbySoup.x, nearbySoup.y, 0, 0));
            jobQueue.remove();
            rc.mineSoup(Direction.CENTER);
        } else {
            if(rc.canMove(utils.intToDirection(jobQueue.peek().param1)))
                rc.move(utils.intToDirection(jobQueue.peek().param1));
            else
                jobQueue.peek().param1 = (int) (Math.random() * 8);
        }
    }

    private void mineDeposit() throws GameActionException {
        if(rc.getSoupCarrying() == 0) {
            if (rc.getLocation().x == jobQueue.peek().param1 && rc.getLocation().y == jobQueue.peek().param2) {
                if(rc.canMineSoup(Direction.CENTER)) {
                    rc.mineSoup(Direction.CENTER);
                } else {
                    MapLocation nextSoup = utils.findNearbySoup(rc);

                    if(nextSoup != null) {
                        jobQueue.peek().param1 = nextSoup.x;
                        jobQueue.peek().param2 = nextSoup.y;
                    } else {
                        jobQueue.add(new Job(Mode.SCOUT_DEPOSIT, (int) (Math.random() * 8), 0, 0, 0));
                        jobQueue.remove();
                    }
                }
            } else {
                utils.moveTowardsSimple(rc, new MapLocation(jobQueue.peek().param1, jobQueue.peek().param2));
            }
        } else {
            if(rc.getLocation().equals(initialLocation)) {
                for(Direction dir : Direction.allDirections()) {
                    if(rc.canDepositSoup(dir)) {
                        rc.depositSoup(dir, rc.getSoupCarrying());
                        break;
                    }
                }
            } else {
                utils.moveTowardsSimple(rc, initialLocation);
            }
        }
    }
}
