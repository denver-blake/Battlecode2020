package sprint;

import battlecode.common.*;

public class RobotPlayer {

    private static RobotController rc;
    static int turnCount;
    private static Robot r;

    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;

        turnCount = 0;


        System.out.println("I'm a " + rc.getType() + " and I just got created!");
        try {
            // Here, we've separated the controls into a different method for each RobotType.
            // You can add the missing ones or rewrite this into your own control structure.
            System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
            switch (rc.getType()) {
                case HQ:                 r = new HQ(rc);                break;
                case MINER:              r = new Miner(rc);             break;
                case REFINERY:           r = new Refinery();          break;
                case VAPORATOR:          r = new Vaporator();         break;
                case DESIGN_SCHOOL:      r = new DesignSchool(rc);      break;
                case FULFILLMENT_CENTER: r = new FulfillmentCenter(); break;
                case LANDSCAPER:         r = new Landscaper(rc);        break;
                case DELIVERY_DRONE:     r = new Drone();               break;
                case NET_GUN:            r = new NetGun();            break;
            }

            // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again


        } catch (Exception e) {
            System.out.println(rc.getType() + " Exception");
            e.printStackTrace();
        }
        while (true) {
            turnCount += 1;
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                // Here, we've separated the controls into a different method for each RobotType.
                // You can add the missing ones or rewrite this into your own control structure.
                System.out.println("I'm a " + rc.getType() + "! Location " + rc.getLocation());
                r.run();

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println(rc.getType() + " Exception");
                e.printStackTrace();
            }
        }
    }
}
