package sprint;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class DesignSchool implements Robot {
    enum UnitType {
        FORTIFY_HQ,
        PROTECT_DEPOSIT // (param1,param2) = deposit location

    }
    
    private class Unit {
        UnitType unitType;
        int param1,param2;
        
        public Unit(UnitType unitType,int param1,int param2) {
            this.unitType = unitType;
            this.param1 = param1;
            this.param2 = param2;
        }
    }
    private RobotController rc;
    private Queue<Unit> buildQueue;
    private MapLocation hqLocation;
    private int currentBlockChainRound;
    private List<MapLocation> refineryLocations;
    
    public DesignSchool(RobotController rc) throws GameActionException {
        this.rc = rc;
        buildQueue = new LinkedList<Unit>();
        hqLocation = utils.hqPosition(rc);
        refineryLocations = new ArrayList<MapLocation>();
        currentBlockChainRound = 1;
        buildQueue.add(new Unit(UnitType.PROTECT_DEPOSIT,hqLocation.x,hqLocation.y));

    }

    public void run() throws GameActionException {
        System.out.println("COOLDOWN: " + rc.getCooldownTurns() + "TEAM SOUP: " + rc.getTeamSoup());
        if(!buildQueue.isEmpty()) {
            switch (buildQueue.peek().unitType) {
                case FORTIFY_HQ:
                    buildFortifyHQ();
                    System.out.println("build fortify HQ unit");
                    break;
                
                case PROTECT_DEPOSIT:
                    buildProtectDeposit();
                    break;
                default:
                    break;
            }
        }
        readBlockChain();


    }

    private void readBlockChain() throws GameActionException {
        for (Transaction transaction : rc.getBlock(currentBlockChainRound)) {
            int[] msg = transaction.getMessage();
            if (msg.length != 7) continue;
            if (msg[0] == utils.BLOCKCHAIN_TAG) {
                if (msg[1] == utils.NEW_REFINERY_TAG) {
                    MapLocation loc = new MapLocation(msg[2],msg[3]);
                    if (!refineryLocations.contains(loc)) {
                        refineryLocations.add(loc);
                        buildQueue.add(new Unit(UnitType.PROTECT_DEPOSIT,loc.x,loc.y));
                    }
                }
            }
        }
        currentBlockChainRound++;
        if (Clock.getBytecodesLeft() > 500 && currentBlockChainRound < rc.getRoundNum()) {
            readBlockChain();
        }

    }

    private void buildFortifyHQ() throws GameActionException {
        if (rc.getTeamSoup() > 200 && utils.tryBuild(rc,RobotType.LANDSCAPER)) {
            System.out.println("Built fortify HQ Landscaper");
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.LANDSCAPER_FORTIFY_CASTLE_TAG,0,0,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();
        }
    }

    private void buildProtectDeposit() throws GameActionException {
        if (rc.getTeamSoup() > 200 && utils.tryBuild(rc,RobotType.LANDSCAPER)) {
            System.out.println("Built Protect Deposit Landscaper at: " + (new MapLocation(buildQueue.peek().param1,buildQueue.peek().param2)) );
            int[] msg = {utils.BLOCKCHAIN_TAG,rc.getRoundNum(),utils.LANDSCAPER_PROTECT_DEPOSIT_TAG,buildQueue.peek().param1,buildQueue.peek().param2,0,0};
            if (rc.canSubmitTransaction(msg,10)) {
                rc.submitTransaction(msg,10);
                System.out.println("SUBMITTED JOB TRANSACTION");
            }
            buildQueue.remove();

        }
    }
}
