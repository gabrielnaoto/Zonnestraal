package realworld;

import robocode.*;
import robocode.util.Utils;
import java.awt.geom.Point2D;
import java.awt.Color;

/**
 * Zonnestraal - a robot by Gabriel Naoto
 */
public class Zonnestraal extends Robot {

    boolean walls = false;
    boolean camped = false;
    boolean move = false;
    boolean walking = false;
    int amt = 30;
    int gear = 1;
    int miss = 0;
    double x = 0;
    double y = 0;

    public void run() {
        setBodyColor(Color.DARK_GRAY);
        setGunColor(Color.LIGHT_GRAY);
        setRadarColor(Color.GRAY);
        setBulletColor(Color.CYAN);
        setScanColor(Color.CYAN);

        goTo(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);
        while (true) {
            turnGunRight(360);
        }
    }

    public Point2D.Double getPosition(ScannedRobotEvent event) {
        double distance = event.getDistance();
        double angle = Math.toRadians(getHeading()) + event.getBearingRadians();
        double x = getX() + Math.sin(angle) * distance;
        double y = getY() + Math.cos(angle) * distance;
        return new Point2D.Double(x, y);
    }

    public void onScannedRobot(ScannedRobotEvent e) {
        Point2D.Double pos = getPosition(e);
        double bV = Rules.getBulletSpeed(3);
        double eX = pos.x;
        double eY = pos.y;
        double eV = e.getVelocity();
        double eH = e.getHeading();

        double A = (eX - getX()) / bV;
        double B = (eY - getY()) / bV;
        double C = eV / bV * Math.sin(eH);
        double D = eV / bV * Math.cos(eH);

        double a = A * A + B * B;
        double b = 2 * (A * C + B * D);
        double c = (C * C + D * D - 1);

        double discrim = b * b - 4 * a * c;
        if (discrim >= 0) {
            double t1 = 2 * a / (-b - Math.sqrt(discrim));
            double t2 = 2 * a / (-b + Math.sqrt(discrim));

            double t = Math.min(t1, t2) >= 0 ? Math.min(t1, t2) : Math.max(t1, t2);

            double targetX = eX + eV * t * Math.sin(eH);
            double targetY = eY + eV * t * Math.cos(eH);

            double minX = 18;
            double minY = 18;
            double maxX = getBattleFieldWidth() - 18;
            double maxY = getBattleFieldHeight() - 18;

            x = limit(targetX, minX, maxX);
            y = limit(targetY, minY, maxY);
        }
        if (x < 36) {
            if (e.getHeading() % 90 == 0) {
                if (miss > 5) {
                    walls = true;
                }
            }
        }
        if (y < 36) {
            if (e.getHeading() % 90 == 0) {
                if (miss > 5) {
                    walls = true;
                }
            }
        }
        if (x > getBattleFieldWidth() - 36) {
            if (e.getHeading() % 90 == 0) {
                if (miss > 5) {
                    walls = true;
                }
            }
        }
        if (y > getBattleFieldHeight() - 36) {
            if (e.getHeading() % 90 == 0) {
                if (miss > 5) {
                    walls = true;
                }
            }
        }
        if (!walls) {
            if (!walking) {
                double turnGunAmt = Utils.normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
                turnGunRight(turnGunAmt);
                smartFire(e.getDistance());
                turnRight(90 + e.getBearing());
                if (move) {
                    ahead(amt * gear);
                }
            }
        } else {
            if (!camped) {
                if (x < 50) { // left
                    if (distanceTo(0, getBattleFieldHeight()) < distanceBetween(0, getBattleFieldHeight(), x, y)) {
                        goTo(0, getBattleFieldHeight());
                        camped = true;
                    }
                }
                if (x > getBattleFieldWidth() - 50) { //right
                    if (distanceTo(getBattleFieldWidth(), 0) < distanceBetween(getBattleFieldWidth(), 0, x, y)) {
                        goTo(getBattleFieldWidth(), 0);
                        camped = true;
                    }
                }
                if (y < 50) { //bottom
                    if (distanceTo(0, 0) < distanceBetween(0, 0, x, y)) {
                        goTo(0, 0);
                        camped = true;
                    }
                }
                if (y > getBattleFieldHeight() - 50) { //top
                    if (distanceTo(getBattleFieldWidth(), getBattleFieldHeight()) < distanceBetween(getBattleFieldWidth(), getBattleFieldHeight(), x, y)) {
                        goTo(getBattleFieldWidth(), getBattleFieldHeight());
                        camped = true;
                    }
                }
            } else {
                double turnGunAmt = Utils.normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
                turnGunRight(turnGunAmt);
                smartFire(e.getDistance());

            }
        }
        scan();
    }

    public void onHitRobot(HitRobotEvent e) {
        if (walls) {
            camped = false;
            goTo(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);
        } else {
            double turnGunAmt = Utils.normalRelativeAngleDegrees(e.getBearing() + getHeading() - getGunHeading());
            turnGunRight(turnGunAmt);
            fire(3);
        }
    }

    public void onHitByBullet(HitByBulletEvent e) {
        if (walls) {
            if (camped) {
                camped = false;
                goTo(getBattleFieldWidth() / 2, getBattleFieldHeight() / 2);
            }
        } else {
            move = true;
        }
    }

    public void onBulletMissed(BulletMissedEvent event) {
        miss++;
    }

    public void onBulletHit(BulletHitEvent event) {
        miss = 0;
    }

    public void onHitWall(HitWallEvent e) {
        if (!walls) {
            back(amt * gear);
            gear *= -1;
        }
    }

    private double limit(double value, double min, double max) {
        return Math.min(max, Math.max(min, value));
    }

    private double angleTo(double x, double y) {
        return Math.atan2(x - getX(), y - getY());
    }

    private double distanceTo(double x, double y) {
        return Math.hypot(x - getX(), y - getY());
    }

    private double distanceBetween(double x1, double y1, double x2, double y2) {
        return Math.hypot(x1 - x2, y1 - y2);
    }

    private double bearingTo(double heading, double x, double y) {
        return Utils.normalRelativeAngle(angleTo(x, y) - heading);
    }

    private void goTo(double x, double y) {
        walking = true;
        double heading = Math.toRadians(getHeading());
        double bearing = bearingTo(heading, x, y);
        turnRight(Math.toDegrees(bearing));
        ahead(distanceTo(x, y));
        walking = false;
    }

    private void smartFire(double robotDistance) {
        if (robotDistance > 200 || getEnergy() < 15) {
            fire(1);
        } else if (robotDistance > 50) {
            fire(2);
        } else {
            fire(3);
        }
    }

    public void onWin(WinEvent e) {
        for (int i = 0; i < 50; i++) {
            turnRight(30);
            turnLeft(30);
        }
    }
}
