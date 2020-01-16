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


    public Landscaper(RobotController rc) throws GameActionException {
        this.rc = rc;
        HQ = utils.hqPosition(rc);
        jobQueue = new LinkedList<>();
        currentBlockChainRound = rc.getRoundNum()-1;
        roundBuilt = rc.getRoundNum()-1;

        System.out.println("Round Built: " + roundBuilt);
        jobQueue.add(new GetRidOfWaterJob(Mode.GET_RID_OF_WATER,HQ,4));
        jobQueue.add(new ProtectDepositJob(Mode.PROTECT_DEPOSIT,HQ));

        pathfinder = null;


    }

    public void run() throws GameActionException {


        if(!jobQueue.isEmpty()) {
            switch (jobQueue.peek().mode) {
                case FORTIFY_HQ:

                    System.out.println("fortify");
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

        System.out.println("BYTECODE: " + Clock.getBytecodeNum());

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
                        System.out.println("Recieved fortify HQ Job");
//                        jobQueue.add(new Job(Mode.FORTIFY_HQ,0,0));
                    } else if (msg[2] == utils.LANDSCAPER_PROTECT_DEPOSIT_TAG) {
                        System.out.println("Recieved Protect Deposit Job at:" + (new MapLocation(msg[3],msg[4])));
                        jobQueue.add(new ProtectDepositJob(Mode.PROTECT_DEPOSIT,new MapLocation(msg[3],msg[4])));
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

    public class ProtectDepositJob extends Job {

        private utils.Bug2Pathfinder pathfinder;
        private MapLocation locationToProtect;
        public ProtectDepositJob(Mode mode, MapLocation locationToProtect) {
            super(mode);
            this.locationToProtect = locationToProtect;
            pathfinder = new utils.Bug2Pathfinder(rc,locationToProtect);
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
                    for (int i = 0;i < 7;i++) {
                        dir = dir.rotateLeft();
                        MapLocation tempLoc = rc.getLocation().add(dir);
                        if (atProtectDistance(tempLoc)) {
                            System.out.println("Next wall segment: " + tempLoc);
                            if (rc.senseElevation(tempLoc) < rc.senseElevation(rc.getLocation())) {
                                System.out.println("Water level: " + waterLevel + "Increasing elevation of flood wall");
                                rc.depositDirt(dir);
                                break;
                            } else if (rc.senseElevation(tempLoc) > rc.senseElevation(rc.getLocation()) + 3){
                                System.out.println("Too high, lowering  elevation of flood wall");
                                rc.digDirt(dir);
                                break;
                            }
                        }
                    }
                    dir = rc.getLocation().directionTo(locationToProtect);

                    for (int i = 0;i < 7;i++) {
                        dir = dir.rotateLeft();
                        MapLocation tempLoc = rc.getLocation().add(dir);
                        if (atProtectDistance(tempLoc)) {

                            if (rc.canMove(dir)) {
                                System.out.println("Moving to next segment");
                                rc.move(dir);
                                break;
                            }

                        }
                    }
                }
            } else {
                 pathfinder.moveTowards();
             }



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

        public GetRidOfWaterJob(Mode mode,MapLocation center, int radius) {

            super(mode);
            this.center = center;
            this.radius = radius;
            this.radiusSquared = radius * radius;
            this.waterMap = new boolean[radius*2 + 1][radius * 2 + 1];

            needDirt = rc.getDirtCarrying() < 25;

            System.out.println("GET RID OF WATER JOB ACQUIRED");



        }

        public void work() throws GameActionException {
            if (rc.getDirtCarrying() == 0) {
                System.out.println("Getting dirt");
                needDirt = true;
            } else if (rc.getDirtCarrying() == 25) {
                needDirt = false;
            }
            if (needDirt) {
                Direction dir = center.directionTo(rc.getLocation());
                if (rc.getLocation().distanceSquaredTo(center) > radiusSquared) {
                    if (rc.canDigDirt(dir)) {
                        rc.digDirt(dir);
                        rc.setIndicatorDot(rc.adjacentLocation(dir),255,255,255);
                    } else if (rc.canDigDirt(dir.rotateRight())){
                        rc.digDirt(dir.rotateRight());
                        rc.setIndicatorDot(rc.adjacentLocation(dir.rotateRight()),255,255,255);
                    } else if (rc.canDigDirt(dir.rotateLeft())){
                        rc.digDirt(dir.rotateLeft());
                        rc.setIndicatorDot(rc.adjacentLocation(dir.rotateLeft()),255,255,255);
                    }
                } else {
                   utils.moveTowardsLandscaper(rc,rc.adjacentLocation(dir).add(dir));
                }
            }
            System.out.println("WORKING");
            if (!rc.isReady()) return;
            if (rc.getLocation().distanceSquaredTo(center) > radiusSquared) {
                System.out.println("Moving towards center");
                utils.moveTowardsLandscaper(rc,center);
                rc.setIndicatorLine(rc.getLocation(),center,0,255,0);
            } else if (!updateWaterMap()) {
                System.out.println("Still flooded");

                    boolean dig = false;
                    for (Direction dir: Direction.allDirections()) {
                        if (rc.adjacentLocation(dir).distanceSquaredTo(center) <= radiusSquared && rc.canDepositDirt(dir) && rc.senseFlooding(rc.adjacentLocation(dir)) && rc.senseElevation(rc.adjacentLocation(dir)) > -200) {
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
                jobQueue.remove();
                System.out.println("NO MORE WATER, DONE!!!!!!!!!!!!!!!!");
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
            boolean noWater = true;
            for (int i = 0;i < waterMap.length;i++) {
                for (int j = 0;j < waterMap.length;j++) {
                    //System.out.println("updating water map");

                        //System.out.println("There is water or unknown");

                        MapLocation location = new MapLocation(center.x - radius + i,center.y - radius + j);
                        if (location.distanceSquaredTo(center) > radiusSquared) waterMap[i][j] = false;
                        else if (rc.canSenseLocation(location) && (!rc.senseFlooding(location) || rc.senseElevation(location) < -200)) {
                            waterMap[i][j] = false;
                            rc.setIndicatorDot(location,0,0,255);
                           // System.out.println("There is no water or too deep");

                        } else if (rc.canSenseLocation(location) && rc.senseFlooding(location)&& rc.senseElevation(location) > -200){
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


    public void attack() {
        for (RobotInfo robotInfo : rc.senseNearbyRobots(-1,rc.getTeam().opponent())) {


        }
    }




}
