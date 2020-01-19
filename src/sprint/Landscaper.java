package sprint;

import battlecode.common.*;


import java.util.*;

public class Landscaper implements Robot {

    enum Mode {
        FORTIFY_HQ,
        GET_RID_OF_WATER,
        DEFEND_FROM_FLOOD, //  location to protect
        SAVE_WORKER, //(param1, param2) = worker location
        ATTACK, //(param1, param2) = enemy location
        PROTECT_DEPOSIT // (param1,param2) = deposit location

    }
    private class Job {
        Mode mode;
        int param1, param2;

        public Job(Mode mode) {
            this.mode = mode;


        }
        public void work() throws  GameActionException{

        }
    }
    private Queue<Job> jobQueue;
    private RobotController rc;
    private MapLocation HQ;
    private utils.Bug2Pathfinder pathfinder;
    private int currentBlockChainRound;
    private int roundBuilt;
    private boolean startFortification;
    private boolean gettingRushed;


    public Landscaper(RobotController rc) throws GameActionException {
        this.rc = rc;
        HQ = utils.hqPosition(rc);
        jobQueue = new LinkedList<>();
        currentBlockChainRound = rc.getRoundNum()-1;
        roundBuilt = rc.getRoundNum()-1;
        startFortification  = false;
        System.out.println("Round Built: " + roundBuilt);
//        jobQueue.add(new GetRidOfWaterJob(Mode.GET_RID_OF_WATER,HQ,4,18));
//        jobQueue.add(new ProtectDepositJob(Mode.PROTECT_DEPOSIT,HQ));
        gettingRushed = false;
        pathfinder = null;


    }

    public void run() throws GameActionException {



        if(!jobQueue.isEmpty()) {
            switch (jobQueue.peek().mode) {
                case FORTIFY_HQ:

                    System.out.println("fortify");
                    jobQueue.peek().work();
                    break;
                case GET_RID_OF_WATER:
                    System.out.println("DEFENDING FROM FLOOD");
                    jobQueue.peek().work();
                    break;
                case SAVE_WORKER:
                    break;
                case ATTACK:
                    break;
                case PROTECT_DEPOSIT:
                    jobQueue.peek().work();
                    break;
                default:
                    break;
            }
        }
        readBlockChain();

        //System.out.println("BYTECODE: " + Clock.getBytecodeNum());

    }
    private void readBlockChain() throws GameActionException {
        //System.out.println("READING BLOCK AT ROUND " + currentBlockChainRound);
        for (Transaction transaction : rc.getBlock(currentBlockChainRound)) {
            int[] msg = transaction.getMessage();
            if (msg.length != 7) continue;
            for (int i = 0;i < msg.length;i++) {
                System.out.print(msg[i] + ", ");
            }
            System.out.println("msg ");
            if (msg[0] == utils.BLOCKCHAIN_TAG) {
                if (msg[1] == roundBuilt) {
                    //Transaction tags to look for
                    if (msg[2] == utils.LANDSCAPER_FORTIFY_CASTLE_TAG) {
                        System.out.println("Recieved fortify HQ Job at: " + (new MapLocation(msg[3],msg[4])));
                        jobQueue.add(new FortifyHQJob(Mode.FORTIFY_HQ,new MapLocation(msg[3],msg[4])));
                    } else if (msg[2] == utils.LANDSCAPER_PROTECT_DEPOSIT_TAG) {
                        System.out.println("Recieved Protect Deposit Job at:" + (new MapLocation(msg[3],msg[4])));
                        jobQueue.add(new GetRidOfWaterJob(Mode.GET_RID_OF_WATER,new MapLocation(msg[3],msg[4]),3,13));
                        jobQueue.add(new ProtectDepositJob(Mode.PROTECT_DEPOSIT,new MapLocation(msg[3],msg[4])));
                    }
                }
                if (msg[2] == utils.START_FORTIFICATION) {
                    startFortification = true;
                }
                if (msg[2] == utils.GOT_RID_OF_WATER && jobQueue.peek().mode == Mode.GET_RID_OF_WATER) {
                    if (((GetRidOfWaterJob) jobQueue.peek()).center.equals(new MapLocation(msg[3],msg[4]))) {
                        jobQueue.remove();
                    }
                }
            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }

    }


//    public void fortifyHQInit() {
//        pathfinder = new utils.Bug2Pathfinder(rc,HQ);
//    }
//    public void fortifyHQ() throws GameActionException {
//        if (pathfinder == null) {
//            fortifyHQInit();
//        }
//        if (!rc.isReady()) return;
//        if (!atFortifyHQLocation()) {
//            System.out.println("MOVING");
//            pathfinder.moveTowards();
//        } else if (rc.getDirtCarrying() < 1) {
//            Direction dir = rc.getLocation().directionTo(HQ).opposite();
//            rc.digDirt(dir);
//
//
//        } else {
//            Direction dir = rc.getLocation().directionTo(HQ);
//            rc.depositDirt(dir);
//        }
//    }
//    public boolean atFortifyHQLocation() {
//        switch(rc.getLocation().distanceSquaredTo(HQ)) {
//            case 9:
//            case 10:
//            case 13:
//            case 18:
//                return true;
//        }
//        return false;
//
//    }

    public class FortifyHQJob extends  Job {

        MapLocation location;
        public FortifyHQJob(Mode mode, MapLocation location) {
            super(mode);
            this.location = location;
        }

        public void work() throws GameActionException {
            if (!rc.isReady()) return;
            if (!rc.getLocation().equals(location)) {
                utils.moveTowardsLandscaperNoWater(rc,location);
            } else {
                System.out.println("AT LOCATION");
                if (rc.getDirtCarrying() != 25) {
                    System.out.println("looking to dig dirt");
                    if (rc.getLocation().distanceSquaredTo(HQ) <= 2) {
                        Direction dir = HQ.directionTo(rc.getLocation());
                        if (rc.canDigDirt(dir)) rc.digDirt(dir);
                        else if (rc.canDigDirt(dir.rotateLeft())) rc.digDirt(dir.rotateLeft());
                        else if (rc.canDigDirt(dir.rotateRight())) rc.digDirt(dir.rotateRight());
                    } else {
                        rc.digDirt(Direction.CENTER);
                    }
                } else  if (rc.getLocation().distanceSquaredTo(HQ) <= 2) {
                    depositLowestWall();


                } else if (rc.getLocation().distanceSquaredTo(HQ) <= 8) {
                    depositLowestWall();

                }
            }
        }

        public void depositLowestWall() throws GameActionException {
            Direction bestDir = null;
            int bestHeight = Integer.MAX_VALUE;
            for (Direction dir: Direction.allDirections()) {
                if (rc.canDepositDirt(dir)) {
                    MapLocation tempLoc = rc.adjacentLocation(dir);
                    if (tempLoc.distanceSquaredTo(HQ) <= 2 && !tempLoc.equals(HQ) && rc.senseRobotAtLocation(tempLoc) != null) {
                        if (rc.senseElevation(tempLoc) < bestHeight) {
                            bestDir = dir;
                            bestHeight = rc.senseElevation(rc.adjacentLocation(dir));
                        }
                    }
                }
            }
            if (bestDir != null) {
                rc.depositDirt(bestDir);
            }
        }

    }

    public class ProtectDepositJob extends Job {


        private MapLocation locationToProtect;
        private int heightIncrease;
        private boolean goLeft;
        public ProtectDepositJob(Mode mode, MapLocation locationToProtect) {
            super(mode);
            this.locationToProtect = locationToProtect;
            heightIncrease = 0;
            goLeft = true;

        }



        public void work() throws GameActionException {
            if (!rc.isReady()) return;
            if (locationToProtect != null) {
                System.out.println("Info: " + rc.senseElevation(rc.getLocation()) +
                        " " + rc.getDirtCarrying() + " " + locationToProtect.distanceSquaredTo(rc.getLocation()));

            }

             if (atProtectDistance(rc.getLocation())) {
                int waterLevel = utils.getWaterLevel(rc.getRoundNum());
                if (rc.getDirtCarrying() == 0 && rc.canDigDirt(locationToProtect.directionTo(rc.getLocation()))) {
                    System.out.println("Low on dirt, digging");
                    rc.digDirt(locationToProtect.directionTo(rc.getLocation()));
                } else if (waterLevel  + 2 >= rc.senseElevation(rc.getLocation())) {
                    System.out.println("Water level: " + waterLevel + "Increasing elevation of flood wall");
                    rc.depositDirt(Direction.CENTER);
                } else {
                    Direction dir = rc.getLocation().directionTo(locationToProtect);
                    System.out.println("Finish current flood wall, looking for next wall segment to build");

                    for (int i = 0;i < 5;i++) {
                        if (goLeft) {
                            dir = dir.rotateLeft();
                        } else {
                            dir = dir.rotateRight();
                        }
                        MapLocation tempLoc = rc.getLocation().add(dir);
                        if (atProtectDistance(tempLoc)) {
                            if (rc.isReady() && (!rc.onTheMap(tempLoc) || rc.senseRobotAtLocation(tempLoc) != null)) {
                                goLeft = !goLeft;
                                break;
                            }
                            System.out.println("Next wall segment: " + tempLoc);

                            if (rc.senseElevation(tempLoc) < rc.senseElevation(rc.getLocation())+ heightIncrease) {
                                System.out.println("Water level: " + waterLevel + "Increasing elevation of flood wall");
                                rc.depositDirt(dir);
                                break;
                            } else if (rc.senseElevation(tempLoc) > rc.senseElevation(rc.getLocation()) + 3){
                                if (rc.getDirtCarrying() == 25) {
                                    rc.depositDirt(Direction.CENTER);
                                    break;
                                } else {
                                    System.out.println("Too high, lowering  elevation of flood wall");
                                    rc.digDirt(dir);
                                    break;
                                }
                            }
                        }
                    }
                    dir = rc.getLocation().directionTo(locationToProtect);

                    for (int i = 0;i < 5;i++) {
                        if (goLeft) {
                            dir = dir.rotateLeft();
                        } else {
                            dir = dir.rotateRight();
                        }
                        MapLocation tempLoc = rc.getLocation().add(dir);
                        if (atProtectDistance(tempLoc)) {
                            if (rc.isReady() && (!rc.onTheMap(tempLoc) || rc.senseRobotAtLocation(tempLoc) != null)) {
                                goLeft = !goLeft;
                                break;
                            }
                            if (rc.canMove(dir)) {
                                System.out.println("Moving to next segment");
                                rc.move(dir);

                                if (increaseHeight()) {
                                    heightIncrease = 2;
                                } else {
                                    heightIncrease = 0;
                                }
                                break;
                            }

                        }
                    }
                }
            } else if (rc.getLocation().distanceSquaredTo(locationToProtect) < 9){
                 utils.moveAwayLandscaper(rc,locationToProtect);
             } else {
                 utils.moveTowardsLandscaper(rc,locationToProtect);
             }



        }

        private boolean increaseHeight() throws GameActionException {
            Direction dir = rc.getLocation().directionTo(locationToProtect);
            System.out.println("Finish current flood wall, looking for next wall segment to build");
            int heightIncrease = 0;
            for (int i = 0;i < 7;i++) {
                dir = dir.rotateLeft();
                MapLocation tempLoc = rc.getLocation().add(dir);
                if (atProtectDistance(tempLoc)) {

                    if (rc.senseElevation(tempLoc) != rc.senseElevation(rc.getLocation()) || rc.senseRobotAtLocation(tempLoc) != null) {
                        return false;
                    }
                }
            }
            return true;
        }
        private boolean atProtectDistance(MapLocation loc) {
            switch(locationToProtect.distanceSquaredTo(loc)) {
                case 9:
                case 10:
                case 13:
                case 18:
                    return true;
            }
            return false;
        }

    }

    public class GetRidOfWaterJob extends Job {

        int radius;
        MapLocation center;
        int radiusSquared;

        boolean[][] waterMap;
        boolean needDirt;

        public GetRidOfWaterJob(Mode mode,MapLocation center, int radius,int radiusSquared) {

            super(mode);
            this.center = center;
            this.radius = radius;
            this.radiusSquared = radiusSquared;
            this.waterMap = new boolean[radius*2 + 1][radius * 2 + 1];

            needDirt = rc.getDirtCarrying() < 25;

            System.out.println("GET RID OF WATER JOB ACQUIRED");



        }

        public void work() throws GameActionException {
            int waterLevel = utils.getWaterLevel(rc.getRoundNum())+2;
            if (rc.getDirtCarrying() == 0) {
                System.out.println("Getting dirt");
                needDirt = true;
            } else if (rc.getDirtCarrying() == 25) {
                needDirt = false;
            }
            if (needDirt) {
                if (rc.canDepositDirt(Direction.CENTER) && rc.senseElevation(rc.getLocation()) < waterLevel) {
                    rc.depositDirt(Direction.CENTER);
                } else {
                    Direction dir = center.directionTo(rc.getLocation());
                    if (rc.getLocation().distanceSquaredTo(center) > radiusSquared) {
                        if (rc.canDigDirt(dir)) {
                            rc.digDirt(dir);
                            rc.setIndicatorDot(rc.adjacentLocation(dir), 255, 255, 255);
                        } else if (rc.canDigDirt(dir.rotateRight())) {
                            rc.digDirt(dir.rotateRight());
                            rc.setIndicatorDot(rc.adjacentLocation(dir.rotateRight()), 255, 255, 255);
                        } else if (rc.canDigDirt(dir.rotateLeft())) {
                            rc.digDirt(dir.rotateLeft());
                            rc.setIndicatorDot(rc.adjacentLocation(dir.rotateLeft()), 255, 255, 255);
                        }
                    } else {
                        utils.moveTowardsLandscaper(rc, rc.adjacentLocation(dir).add(dir));
                    }
                }
            }

            if (!rc.isReady()) return;
            if (rc.getLocation().distanceSquaredTo(center) > radiusSquared) {
                System.out.println("Moving towards center");
                utils.moveTowardsLandscaper(rc,center);
                rc.setIndicatorLine(rc.getLocation(),center,0,255,0);
            } else if (!updateWaterMap()) {
                System.out.println("Still flooded");

                    boolean dig = false;
                    for (Direction dir: Direction.allDirections()) {
                        if (rc.adjacentLocation(dir).distanceSquaredTo(center) <= radiusSquared && rc.canDepositDirt(dir)
                                && rc.senseElevation(rc.adjacentLocation(dir)) < waterLevel && rc.senseElevation(rc.adjacentLocation(dir)) > -200 &&
                                utils.notAlliedBuilding(rc,rc.adjacentLocation(dir))) {
                            rc.depositDirt(dir);
                            rc.setIndicatorDot(rc.adjacentLocation(dir),0,0,0);
                            System.out.println("filling up water" );
                            rc.setIndicatorLine(rc.getLocation(),rc.adjacentLocation(dir),0,255,0);
                            dig = true;
                            break;
                        }
                    }
                    if (!dig) {

                        moveTowardsWater();
                    }
            } else {
                int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.GOT_RID_OF_WATER,center.x,center.y,0,0};
                if (rc.canSubmitTransaction(msg,10)) {
                    rc.submitTransaction(msg, 10);
                    System.out.println("SUBMITTED JOB TRANSACTION");

                    System.out.println("NO MORE WATER, DONE!!!!!!!!!!!!!!!!");
                    jobQueue.remove();
                }


            }
        }

        private void moveTowardsWater() throws GameActionException {
            for (int i = 0; i < waterMap.length; i++) {
                for (int j = 0; j < waterMap.length; j++) {

                    if (waterMap[i][j]) {

                        MapLocation location = new MapLocation(center.x - radius + i, center.y - radius + j);
                        utils.moveTowardsLandscaper(rc,location);
                        rc.setIndicatorLine(rc.getLocation(),location,0,255,0);
                        return;
                    }
                }
            }
        }

        //update water map,
        private boolean updateWaterMap() throws GameActionException {
            int waterLevel = utils.getWaterLevel(rc.getRoundNum())+2;
            boolean noWater = true;
            for (int i = 0;i < waterMap.length;i++) {
                for (int j = 0;j < waterMap.length;j++) {
                    //System.out.println("updating water map");

                        //System.out.println("There is water or unknown");

                        MapLocation location = new MapLocation(center.x - radius + i,center.y - radius + j);
                        if (location.distanceSquaredTo(center) > radiusSquared) waterMap[i][j] = false;
                        else if (rc.canSenseLocation(location) && (!(rc.senseElevation(location) < waterLevel) || rc.senseElevation(location) < -200 || !utils.notAlliedBuilding(rc,location))) {
                            waterMap[i][j] = false;
                            rc.setIndicatorDot(location,0,0,255);
                           // System.out.println("There is no water or too deep");

                        } else if (rc.canSenseLocation(location) && (rc.senseElevation(location) < waterLevel) && rc.senseElevation(location) > -200){
                            rc.setIndicatorDot(location,255,0,0);
                            waterMap[i][j] = true;
                            //System.out.println("There is shallow water");

                            noWater = false;
                        } else if (waterMap[i][j]){
                            rc.setIndicatorDot(location,255,0,0);
                            noWater = false;
                        } else if (!waterMap[i][j]) {
                            rc.setIndicatorDot(location,0,0,255);

                        }


                }
            }
            System.out.println("Is there watter?? " + noWater);
            return noWater;
        }


    }


    public boolean antiRushDefense() throws GameActionException {
        RobotInfo[] enemyRobots = rc.senseNearbyRobots(-1,rc.getTeam().opponent());
        RobotInfo HQInfo = rc.canSenseLocation(HQ) ? rc.senseRobotAtLocation(HQ) : null;
        MapLocation enemySchool = null;
        MapLocation enemyNetGun = null;
        MapLocation enemyCenter = null;
        MapLocation enemyMiner = null;
        List<MapLocation> enemyLandscapers = new ArrayList<MapLocation>();

        if (HQInfo == null) {
            utils.moveTowardsLandscaper(rc,HQ);
            return false;
        }
        if (enemyRobots.length == 0) {
            return true;
        }
        for (RobotInfo robot : enemyRobots) {
            if (robot.type == RobotType.DESIGN_SCHOOL) {
                enemySchool = robot.location;
            } else if (robot.type == RobotType.NET_GUN) {
                enemyNetGun = robot.location;
            } else if (robot.type == RobotType.FULFILLMENT_CENTER) {
                enemyCenter = robot.location;
            } else if (robot.type == RobotType.MINER) {
                enemyMiner = robot.location;
            } else if (robot.type == RobotType.LANDSCAPER) {
                enemyLandscapers.add(robot.location);
            }
        }

        if (enemySchool != null && rc.getLocation().isAdjacentTo(enemySchool) && rc.getDirtCarrying() > 0) {
            rc.depositDirt(rc.getLocation().directionTo(enemySchool));
        } else if (enemyNetGun != null && rc.getLocation().isAdjacentTo(enemyNetGun) && rc.getDirtCarrying() > 0) {
            rc.depositDirt(rc.getLocation().directionTo(enemyNetGun));
        } else if (enemyCenter != null && rc.getLocation().isAdjacentTo(enemyCenter) && rc.getDirtCarrying() > 0) {
            rc.depositDirt(rc.getLocation().directionTo(enemyCenter));
        } else if (rc.getLocation().isAdjacentTo(HQ) && rc.getDirtCarrying() < 25 && HQInfo.dirtCarrying > 0) {
            rc.digDirt(rc.getLocation().directionTo(HQ));
        } else {
            if (rc.getDirtCarrying() > 15) {
                if (enemySchool != null) {
                    utils.moveTowardsLandscaperNoWater(rc,enemySchool);
                } else if (enemyNetGun != null) {
                    utils.moveTowardsLandscaperNoWater(rc,enemyNetGun);
                } else if (enemySchool != null) {
                    utils.moveTowardsLandscaperNoWater(rc,enemySchool);
                } else if (!rc.getLocation().isAdjacentTo(HQ)) {
                    utils.moveTowardsLandscaperNoWater(rc,HQ);
                } else {
                    utils.tryDepositLowest(rc);
                }
            } else {
                if (HQInfo.dirtCarrying > 15) {
                    utils.moveTowardsLandscaperNoWater(rc,HQ);
                } else  {
                    utils.tryDigLowest(rc);
                }
            }

        }
        return false;



    }




}
