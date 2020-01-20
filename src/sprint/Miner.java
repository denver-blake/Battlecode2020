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

    private utils.Bug2Pathfinder pathfinder;

    private LinkedList<Job> jobQueue;
    private RobotController rc;

    private MapLocation initialLocation;
    private MapLocation hqLocation;

    private MapLocation refineryLocation;
    private int turn;
    private int roundBuilt;
    private MapLocation schoolLocation;
    private boolean built = false;
    public Miner(RobotController rc) throws GameActionException {
        this.rc = rc;
        turn = 0;
        roundBuilt = rc.getRoundNum();
        jobQueue = new LinkedList<>();

        if(roundBuilt > 250) {
            System.out.println("BUILD SCHOOL JOB");
            schoolLocation = new MapLocation(rc.getLocation().x + 8, rc.getLocation().y);
            System.out.println("SCHOOL LOCATION: " + schoolLocation);

            jobQueue.add(new Job(Mode.BUILD_SCHOOL, schoolLocation.x, schoolLocation.y, 0, 0));
        } else {
            MapLocation lastRefineryLocation = utils.lastRefineryLocation(rc);
            System.out.println(lastRefineryLocation + " @ " + roundBuilt);
            if (lastRefineryLocation == null) {
                jobQueue.add(new Job(Mode.SCOUT_DEPOSIT, (int) (Math.random() * 8), 0, 0, 0));
            } else {
                jobQueue.add(new Job(Mode.MINE_DEPOSIT, lastRefineryLocation.x + 1, lastRefineryLocation.y, 0, 0));
                pathfinder = new utils.Bug2Pathfinder(rc, new MapLocation(lastRefineryLocation.x + 1, lastRefineryLocation.y));
            }
        }

        initialLocation = rc.getLocation();
        hqLocation = utils.hqPosition(rc);
    }

    public void run() throws GameActionException {
        if (rc.getRoundNum() > 500 && rc.getLocation().distanceSquaredTo(hqLocation) < 9) rc.disintegrate();
        if (built) rc.disintegrate();
        if (roundBuilt > 250) {
            if (rc.canBuildRobot(RobotType.DESIGN_SCHOOL,Direction.NORTH) )  {
                rc.buildRobot(RobotType.DESIGN_SCHOOL,Direction.NORTH);
                built = true;
            }
            return;
        }

//        turn++;
        //System.out.println("running");
        if(!jobQueue.isEmpty()) {
            switch (jobQueue.peek().mode) {
                case BUILD_REFINERY:
                    buildRefinery();
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

    private void buildRefinery() throws GameActionException {
        MapLocation refineryLocation = new MapLocation(jobQueue.peek().param1, jobQueue.peek().param2);

        if(rc.getLocation().distanceSquaredTo(refineryLocation) <= 2
                && rc.canBuildRobot(RobotType.REFINERY, rc.getLocation().directionTo(refineryLocation))) {
            rc.buildRobot(RobotType.REFINERY, rc.getLocation().directionTo(refineryLocation));
            jobQueue.remove();
            return;
        }

        utils.moveTowardsSimple(rc, refineryLocation);
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
            utils.moveTowardsSimple(rc, initialLocation);
        } else {
            jobQueue.peek().param3 = 1;
        }
    }

    private void buildSchool() throws GameActionException {
        if(jobQueue.peek().param3 == 1) {
            MapLocation schoolLocation = new MapLocation(jobQueue.peek().param1, jobQueue.peek().param2);
            System.out.println("Looking for build school at " + schoolLocation + " dir: " + rc.getLocation().directionTo(schoolLocation));
            if(rc.getLocation().distanceSquaredTo(schoolLocation) <= 2
                    && rc.canBuildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(schoolLocation))) {
                rc.buildRobot(RobotType.DESIGN_SCHOOL, rc.getLocation().directionTo(schoolLocation));
                jobQueue.remove();
                return;
            }
        }

        if(rc.getLocation().distanceSquaredTo(schoolLocation) > 2) {
            System.out.println("MOVING TO SCHOOL LOCATION");
            utils.moveTowardsSimple(rc, schoolLocation);
        } else {
            System.out.println("STARTING BUILDING");

            jobQueue.peek().param3 = 1;
        }
    }

    private void scoutDeposit() throws GameActionException {
        //first check if we're currently near any soup
        MapLocation nearbySoup = utils.findNearbySoup(rc);
        if(nearbySoup != null && utils.shouldCopSoup(rc)) {
            System.out.println("copping soup @ " + nearbySoup.x + " " + nearbySoup.y);
            jobQueue.add(new Job(Mode.MINE_DEPOSIT, nearbySoup.x, nearbySoup.y, 0, 0));
            pathfinder = new utils.Bug2Pathfinder(rc, nearbySoup);
            jobQueue.remove();
        } else {
            if(rc.canMove(utils.intToDirection(jobQueue.peek().param1)))
                rc.move(utils.intToDirection(jobQueue.peek().param1));
            else
                jobQueue.peek().param1 = (int) (Math.random() * 8);
            }
    }

    private void mineDeposit() throws GameActionException {
        if(rc.getSoupCarrying() < 95) {
            if (rc.getLocation().x == jobQueue.peek().param1 && rc.getLocation().y == jobQueue.peek().param2) {

                if(refineryLocation == null) {
                    MapLocation existingRefinery = utils.searchForRefinery(rc);

                    if(existingRefinery != null) {
                        refineryLocation = existingRefinery;
                    } else if(rc.getTeamSoup() > 200) {
                        //need to make a new refinery
                        MapLocation newRefineryLocation = utils.newRefineryLocation(rc);

                        if(newRefineryLocation != null) {
                            refineryLocation = newRefineryLocation;
                            jobQueue.addFirst(new Job(Mode.BUILD_REFINERY, newRefineryLocation.x, newRefineryLocation.y, 0, 0));
                        }
                    }
                }

                if(rc.canMineSoup(Direction.CENTER)) {
                    rc.mineSoup(Direction.CENTER);
                    pathfinder = new utils.Bug2Pathfinder(rc, initialLocation);
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
            if(refineryLocation == null) {
                if (rc.getLocation().equals(initialLocation)) {
                    for (Direction dir : Direction.allDirections()) {
                        if (rc.canDepositSoup(dir)) {
                            rc.depositSoup(dir, rc.getSoupCarrying());
                            pathfinder = new utils.Bug2Pathfinder(rc, new MapLocation(jobQueue.peek().param1, jobQueue.peek().param2));
                            break;
                        }
                    }
                } else {
                    utils.moveTowardsSimple(rc, initialLocation);
                }
            } else {
                if(rc.getLocation().distanceSquaredTo(refineryLocation) <= 2) {
                    for (Direction dir : Direction.allDirections()) {
                        if (rc.canDepositSoup(dir)) {
                            rc.depositSoup(dir, rc.getSoupCarrying());
                            break;
                        }
                    }
                } else {
                    utils.moveTowardsSimple(rc, refineryLocation);
                }
            }
        }
    }
}