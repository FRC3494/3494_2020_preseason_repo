package frc.robot;

import edu.wpi.cscore.UsbCamera;
import edu.wpi.cscore.VideoMode;
import edu.wpi.first.cameraserver.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.CommandGroup;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.commands.auto.arm.RunToHome;
import frc.robot.sensors.Limelight;
import frc.robot.sensors.PressureSensor;
import frc.robot.subsystems.CargoManipulatorArm;
import frc.robot.subsystems.Climber;
import frc.robot.subsystems.ComboManipulator;
import frc.robot.subsystems.Drivetrain;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
    Command autonomousCommand;
    SendableChooser<Command> chooser = new SendableChooser<>();
    private static Limelight limelight;

    /**
     * This function is run when the robot is first started up and should be
     * used for any initialization code.
     */
    @Override
    public void robotInit() {
        // HACK: Singletons don't like working unless they're grabbed before use.
        OI.getInstance();
        Drivetrain.getInstance();
        Climber.getInstance();
        CargoManipulatorArm.getInstance();
        ComboManipulator.getInstance();
        // Start compressor
        new Compressor().start();
        // init limelight
        limelight = new Limelight();
        UsbCamera c = CameraServer.getInstance().startAutomaticCapture("Emergency USB camera", 0);
        c.setVideoMode(VideoMode.PixelFormat.kMJPEG, 265, 144, 15);
        // chooser.setDefaultOption("Default Auto", new ExampleCommand());
        // chooser.addOption("My Auto", new MyAutoCommand());
        SmartDashboard.putData("Auto mode", chooser);

        String[] displays = new String[]{"Display Drivetrain data?", "Display navX data?", "Display pressure?"};
        for (String display : displays) {
            if (!SmartDashboard.containsKey(display)) {
                SmartDashboard.putBoolean(display, false);
                SmartDashboard.setPersistent(display);
            }
        }
    }

    /**
     * This function is called every robot packet, no matter the mode. Use
     * this for items like diagnostics that you want ran during disabled,
     * autonomous, teleoperated and test.
     *
     * <p>This runs after the mode specific periodic functions, but before
     * LiveWindow and SmartDashboard integrated updating.
     */
    @Override
    public void robotPeriodic() {
        if (SmartDashboard.getBoolean("Display pressure?", false)) {
            SmartDashboard.putNumber("Pnuematic pressure", PressureSensor.getInstance().getPressure());
        }
    }

    /**
     * This function is called once each time the robot enters Disabled mode.
     * You can use it to reset any subsystem information you want to clear when
     * the robot is disabled.
     */
    @Override
    public void disabledInit() {
    }

    @Override
    public void disabledPeriodic() {
        Scheduler.getInstance().run();
    }

    /**
     * This autonomous (along with the chooser code above) shows how to select
     * between different autonomous modes using the dashboard. The sendable
     * chooser code works with the Java SmartDashboard.
     *
     * <p>You can add additional auto modes by adding additional commands to the
     * chooser code above (like the commented example) or additional comparisons
     * to the switch structure below with additional strings & commands.
     */
    @Override
    public void autonomousInit() {
        autonomousCommand = chooser.getSelected();

        if (autonomousCommand != null) {
            if (!(autonomousCommand instanceof CommandGroup)) {
                // truly singular command -> add RunToHome in what I hope will be parallel
                // If both commands require use of the arm strange things will probably happen
                // to avoid this, use a single command wrapped in a group
                CommandGroup group = new CommandGroup();
                group.addParallel(autonomousCommand);
                group.addParallel(new RunToHome());
                group.start();
            } else {
                autonomousCommand.start();
            }
        }
    }

    /**
     * This function is called periodically during autonomous.
     */
    @Override
    public void autonomousPeriodic() {
        Scheduler.getInstance().run();
    }

    @Override
    public void teleopInit() {
        // This makes sure that the autonomous stops running when
        // teleop starts running. If you want the autonomous to
        // continue until interrupted by another command, remove
        // this line or comment it out.
        if (autonomousCommand != null) {
            autonomousCommand.cancel();
        }
        limelight.setLEDs(Limelight.LIMELIGHT_LED_OFF);
    }

    /**
     * This function is called periodically during operator control.
     */
    @Override
    public void teleopPeriodic() {
        Scheduler.getInstance().run();
    }

    /**
     * This function is called periodically during test mode.
     */
    @Override
    public void testPeriodic() {
    }

    public static Limelight getLimelight() {
        return limelight;
    }
}
