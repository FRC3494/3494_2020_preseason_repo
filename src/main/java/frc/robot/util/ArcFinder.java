package frc.robot.util;

import edu.wpi.first.wpilibj.drive.Vector2d;
import frc.robot.Robot;
import frc.robot.RobotMap;

public class ArcFinder {
    private Vector2d directionIntoTarget;
    private double arcRadius;
    private double initialAngleToTargetCenter;//not set yet
    private double angleBetweenCenterAndEdge;
    private double initialDistToTargetCenter;//not set yet
    private double initialDistToTargetEdge;//not set yet
    private double initialAngleToTargetEdge;//not set yet
    private double kiteLegLength;
    private double cornerToTargetDist;
    private double initialDrivingDist;
    private double initialDistToCorner;
    private double targetSkewAngle;//not set yet
    private double distToTargetAfterArc;
    private double arcAngle;

     private boolean isPossible = false;

    private static ArcFinder INSTANCE;
    public static ArcFinder getInstance(){
        return INSTANCE;
    }

    public void compute(){
        this.setDirectionIntoTarget();
        this.setTargetSkewAngle();

        this.setInitialDistanceToTargetCenter();
        this.setInitialAngleToTargetCenter();

        this.setInitialDistToCorner();

        this.setInitialAngleToTargetEdge();
        this.setAngleBetweenCenterAndEdge();
        this.setInitialDistToTargetEdge();

        this.setInitialDrivingDistance();

        this.setKiteLegLength();
        this.setCornerToTargetDist();
        this.setDistToTargetAfterArc();

        this.setArcRadius();
        this.setArcAngle();
    }

    private void setDirectionIntoTarget(){
        this.directionIntoTarget = new Vector2d(Math.cos(Robot.getFrontLimelightInstance().getSkewAngle()),
                Math.sin(Robot.getFrontLimelightInstance().getSkewAngle()));
    }

    private boolean isRightOfTarget(){
        return Robot.getFrontLimelightInstance().isRightOfTarget();
    }

    private void setAngleBetweenCenterAndEdge(){
        this.angleBetweenCenterAndEdge = this.initialAngleToTargetEdge - this.initialAngleToTargetCenter;
    }

    private void setArcAngle(){
        this.arcAngle = Math.acos(1
                - (Math.pow(this.initialDistToTargetCenter*Math.cos(this.initialAngleToTargetCenter)
                + this.directionIntoTarget.x * this.distToTargetAfterArc - this.initialDrivingDist, 2)
                + Math.pow(this.initialDistToTargetCenter * Math.sin(this.initialAngleToTargetCenter)
                + this.directionIntoTarget.y * this.distToTargetAfterArc, 2))
                / (2 * Math.pow(this.arcRadius, 2)));
    }

    private void setTargetSkewAngle(){
        this.targetSkewAngle = Robot.getFrontLimelightInstance().getSkewAngle();
    }

    private void setPipelineToCenter(){
        if(this.isRightOfTarget()){
            Robot.getFrontLimelightInstance().setPipeline(RobotMap.LIMELIGHT.PIPELINE.CENTER_RIGHT.num());
        }else{
            Robot.getFrontLimelightInstance().setPipeline(RobotMap.LIMELIGHT.PIPELINE.CENTER_LEFT.num());
        }
    }

    private void setPipelineToEdge(){
        if(this.isRightOfTarget()){
            Robot.getFrontLimelightInstance().setPipeline(RobotMap.LIMELIGHT.PIPELINE.EDGE_RIGHT.num());
        }else{
            Robot.getFrontLimelightInstance().setPipeline(RobotMap.LIMELIGHT.PIPELINE.EDGE_LEFT.num());
        }
    }

    private void setInitialDistToTargetCenter(){
        this.setPipelineToCenter();
        this.initialDistToTargetCenter = Robot.getFrontLimelightInstance().getDistance();
    }

    private void setInitialAngleToTargetCenter(){
        this.setPipelineToCenter();
        this.initialAngleToTargetCenter =  Robot.getFrontLimelightInstance().getTargetXAngleRad();
    }

    private void setInitialAngleToTargetEdge(){
        this.setPipelineToEdge();
        this.initialAngleToTargetEdge =  Robot.getFrontLimelightInstance().getTargetXAngleRad();

    }

    //this.directionIntoTarget.rotate() takes degrees and rotates counterclockwise
    private Vector2d getRotatedDirectionIntoTarget(){
        Vector2d rotatedDirection = new Vector2d(this.directionIntoTarget.x, this.directionIntoTarget.y);
        rotatedDirection.rotate(90);
        return rotatedDirection;
    }

    private void setDistToTargetAfterArc(){
        this.distToTargetAfterArc = this.cornerToTargetDist - this.kiteLegLength;
    }

    private void setInitialDrivingDistance(){
        this.initialDrivingDist = (this.initialDistToTargetEdge * Math.cos(RobotMap.LIMELIGHT.FOV_RAD / 2)
                - this.initialDistToTargetEdge * Math.sin(RobotMap.LIMELIGHT.FOV_RAD / 2)) / Math.tan(RobotMap.LIMELIGHT.FOV_RAD / 2);
    }

    //equation: sqrt((d_ix - L_x)^2 + (d_iy)^2))
    private void setCornerToTargetDist(){
        this.cornerToTargetDist = Math.sqrt(Math.pow(
                this.initialDistToTargetCenter*Math.cos(this.initialAngleToTargetCenter) - this.initialDistToCorner, 2))
                + (Math.pow(this.initialDistToTargetCenter*Math.sin(this.initialAngleToTargetCenter),2));
    }

    private void setKiteLegLength(){
        this.kiteLegLength = this.initialDistToCorner - this.initialDrivingDist;
    }
    private void setInitialDistToCorner(){
        this.initialDistToCorner = -(this.initialDistToTargetCenter*Math.sin(this.initialAngleToTargetCenter)) / Math.tan(this.targetSkewAngle)
                + (this.initialDistToTargetCenter*Math.cos(this.initialAngleToTargetCenter));
    }
    private boolean isPathPossible(){
        return (this.cornerToTargetDist > this.kiteLegLength);
    }


    private double setInitialDistanceToTargetCenter(){
        return 0;
    }

    private void setInitialDistToTargetEdge(){
        this.initialDistToTargetEdge = this.initialDistToTargetCenter * Math.cos(this.angleBetweenCenterAndEdge)
                + Math.sqrt(Math.pow(RobotMap.ARC_DRIVE.CARGO_HATCH_TAPE_WIDTH_FEET, 2) / 4
                - Math.pow(this.initialDistToTargetCenter, 2)
                + Math.pow(this.initialDistToTargetCenter * Math.cos(this.angleBetweenCenterAndEdge), 2));
    }

    private void setArcRadius(){
        Vector2d rotatedDirection = getRotatedDirectionIntoTarget();
        this.arcRadius = rotatedDirection.y/rotatedDirection.x * (this.initialDistToTargetCenter*Math.cos(this.initialAngleToTargetCenter)
                + this.directionIntoTarget.x * this.distToTargetAfterArc + this.initialDrivingDist)
                + this.initialDistToTargetCenter*Math.sin(this.initialAngleToTargetCenter)
                + this.directionIntoTarget.y * this.distToTargetAfterArc;
    }
}