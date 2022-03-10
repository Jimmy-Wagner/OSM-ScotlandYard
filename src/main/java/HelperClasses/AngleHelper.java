package HelperClasses;

public class AngleHelper {

    public static double angleToFirstQuadrant(double angle){
        // first quadrant
        if (angle >= 0 && angle <= 90){
            return angle;
        }
        // Second quadrant
        else if (angle > 90 && angle <= 180){
            return 180-angle;
        }
        // third quadrant
        else if(angle <= -90 && angle >= -180){
            return angle+180;
        }
        // Fourth quadrant
        else if(angle > -90 && angle < 0){
            return -angle;
        }
        System.out.println("Severe error!");
        return 0;
    }
}
