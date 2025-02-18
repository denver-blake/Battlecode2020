package sprint;
import battlecode.common.*;

import java.util.*;

public class utils {

    public static void moveTowardsSimple(RobotController rc,MapLocation destination) throws GameActionException {
        Direction dir = rc.getLocation().directionTo(destination);
        if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
            System.out.println("Next Location" + rc.getLocation().add(dir));
            rc.move(dir);
            return;
        }
        for (int i = 0;i < 7;i++) {
            dir = dir.rotateLeft();
            if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                rc.move(dir);
                return;
            }
        }
    }

    public static class Bug2Pathfinder {
        private RobotController rc;
        private MapLocation destination;
        private List<MapLocation> line;
        private Direction prevDirection;
        public Bug2Pathfinder(RobotController rc,MapLocation destination) {
            this.rc = rc;
            this.destination = destination;
            line = findLine(rc,rc.getLocation(),destination);
        }

        public void moveTowards() throws GameActionException {
            if (rc.getCooldownTurns() >= 1) return;
            Direction dir;

            for (int i = 0;i < line.size()-1;i++) {

                if (line.get(i).equals(rc.getLocation())) {
                    dir = rc.getLocation().directionTo(line.get(i+1));
                    System.out.println(dir + " Towards goal");
                    if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                        System.out.println(dir + " Actual");
                        prevDirection = dir;
                        rc.move(dir);
                        return;
                    }
                    for (int j = 0;j < 8;j++) {
                        dir = dir.rotateLeft();
                        if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                            prevDirection = dir;
                            System.out.println(dir + " Actual");
                            rc.move(dir);
                            return;
                        }
                    }
                }
            }
            dir = prevDirection.opposite().rotateLeft();
            for (int j = 0;j < 8;j++) {
                dir = dir.rotateLeft();
                if (rc.canMove(dir) && !rc.senseFlooding(rc.getLocation().add(dir))) {
                    prevDirection = dir;
                    rc.move(dir);
                    return;
                }
            }

        }
    }



    public static List<MapLocation> findLine(RobotController rc, MapLocation initial,MapLocation end) {
        List<MapLocation> line = new ArrayList<MapLocation>();
        int x0 = initial.x;
        int y0 = initial.y;
        int x1 = end.x;
        int y1 = end.y;

        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);

        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;

        int err = dx-dy;
        int e2;

        while (true)
        {
            line.add(new MapLocation(x0,y0));

            if (x0 == x1 && y0 == y1)
                break;

            e2 = 2 * err;
            if (e2 > -dy)
            {
                err = err - dy;
                x0 = x0 + sx;
            }

            if (e2 < dx)
            {
                err = err + dx;
                y0 = y0 + sy;
            }
        }
        System.out.println(line);
        return line;
    }


}
