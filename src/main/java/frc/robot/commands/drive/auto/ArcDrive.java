package frc.robot.commands.drive.auto;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.TimedCommand;
import frc.robot.Robot;
import frc.robot.RobotMap;
import frc.robot.commands.drive.Drive;
import frc.robot.subsystems.Drivetrain;
import frc.robot.util.ArcConfig;
import frc.robot.util.ArcFinder;
import frc.robot.util.SynchronousPIDF;


public class ArcDrive extends TimedCommand {
    private double arcRadius;
    private double arcAngle;
    private double leftToRightRatio;
    private double leftSetpoint;
    private double rightSetpoint;
    private double leftInitialDist;
    private double rightInitialDist;

    private SynchronousPIDF pidControllerLeft, pidControllerRight, pidControllerPosition;

    private ArcConfig config;


    private Timer timer;
    private double lastTime = 0;



    public ArcDrive(double arcRadius) {
        super(RobotMap.ARC_DRIVE.TIMEOUT);

        requires(Drivetrain.getInstance());

        this.arcRadius = arcRadius;

        this.timer = new Timer();

        this.pidControllerLeft = new SynchronousPIDF(RobotMap.ARC_DRIVE.kP,
                                                RobotMap.ARC_DRIVE.kI,
                                                RobotMap.ARC_DRIVE.kD,
                                                RobotMap.ARC_DRIVE.kF);

        this.pidControllerRight = new SynchronousPIDF(RobotMap.ARC_DRIVE.kP,
                RobotMap.ARC_DRIVE.kI,
                RobotMap.ARC_DRIVE.kD,
                RobotMap.ARC_DRIVE.kF);

        //this.directionIntoTarget = new Vector2d(0, 0);
    }

    private void configureArc(){
        this.config = new ArcConfig();
        ArcFinder.getInstance().calculate(this.config);
        this.arcRadius = ArcFinder.getInstance().getArcRadius();
        this.arcAngle = ArcFinder.getInstance().getArcAngle();
        this.leftToRightRatio = ArcFinder.getInstance().getLeftToRightRatio();


    }

    private void setSetpoints(){
        if(!this.isRightOfTarget()){
            this.leftSetpoint = RobotMap.ARC_DRIVE.MAX_SPEED;
            this.rightSetpoint = RobotMap.ARC_DRIVE.MAX_SPEED * this.leftToRightRatio;
        }else {
            this.leftSetpoint = RobotMap.ARC_DRIVE.MAX_SPEED / this.leftToRightRatio;
            this.rightSetpoint = RobotMap.ARC_DRIVE.MAX_SPEED;
        }



        this.pidControllerLeft.setSetpoint(this.leftSetpoint);
        this.pidControllerRight.setSetpoint(this.rightSetpoint);
    }

    private boolean isRightOfTarget(){
        return Robot.getFrontLimelightInstance().isRightOfTarget();
    }

    /**
     * The initialize method is called just before the first time
     * this Command is run after being started.
     */
    @Override
    protected void initialize() {
        this.timer.start();
        this.configureArc();
        this.setSetpoints();

        Drivetrain.getInstance().resetEncoders();

        this.pidControllerLeft.setOutputRange(-1,1);
        this.pidControllerLeft.setDeadband(leftSetpoint / 10);

        this.pidControllerRight.setOutputRange(-1,1);
        this.pidControllerRight.setDeadband(rightSetpoint / 10);

        if(!isRightOfTarget()){//have Drason check this
            this.leftInitialDist = (this.arcRadius + RobotMap.ARC_DRIVE.WIDTH_BETWEEN_ROBOT_WHEELS_FEET / 2) *
                                    this.arcAngle;
            this.rightInitialDist = (this.arcRadius - RobotMap.ARC_DRIVE.WIDTH_BETWEEN_ROBOT_WHEELS_FEET / 2) *
                    this.arcAngle;
        }else{
            this.leftInitialDist = (this.arcRadius - RobotMap.ARC_DRIVE.WIDTH_BETWEEN_ROBOT_WHEELS_FEET / 2) *
                    this.arcAngle;
            this.rightInitialDist = (this.arcRadius + RobotMap.ARC_DRIVE.WIDTH_BETWEEN_ROBOT_WHEELS_FEET / 2) *
                    this.arcAngle;
        }

    }


    /**
     * The execute method is called repeatedly when this Command is
     * scheduled to run until this Command either finishes or is canceled.
     */
    @Override
    protected void execute() {
        double dt = this.timer.get() - lastTime;
        double left = this.pidControllerLeft.calculate(Drivetrain.getInstance().getLeftAverageVelocity(), dt);
        double right = this.pidControllerRight.calculate(Drivetrain.getInstance().getRightAverageVelocity(), dt);

        Drivetrain.getInstance().tankDrive(left, right);

        this.lastTime = timer.get();

    }

    @Override
    protected boolean isFinished() {
        // TODO: Make this return true when this Command no longer needs to run execute()
        return !isPossible && ;//add whether its on target
    }

    @Override
    protected void end() {

    }

    @Override
    protected void interrupted() {
        super.interrupted();
    }
}