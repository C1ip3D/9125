package frc.robot;

import java.io.File;

import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismRoot2d;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

import com.ctre.phoenix6.hardware.Pigeon2;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.CommandPS4Controller;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.TurretConstants;
import frc.robot.commands.AutoAim;
import frc.robot.commands.ShooterAim;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.subsystems.TurretSubsystem;

public class Robot extends LoggedRobot {
    public final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve"));
    public final LimelightVision limelight = new LimelightVision(TurretConstants.LIMELIGHT_NAME);
    public final Pigeon2 imu = new Pigeon2(13);
    private final CommandXboxController driverController = new CommandXboxController(
            OperatorConstants.DRIVER_CONTROLLER_PORT);
    private final CommandPS4Controller operatorController = new CommandPS4Controller(
            OperatorConstants.OPERATOR_CONTROLLER_PORT);

    public TurretSubsystem turret;
    public ShooterSubsystem shooter;

    public Command autonomousCommand;
    public AutoAim autoaim;
    public ShooterAim shooterAim;

    Pose3d turretMechanism;

    private void configureBindings() {

        // Zero gyro on Y button press
        // driverController.y().onTrue(Commands.runOnce(drivebase::zeroGyro));

        // Lock wheels in X formation while A is held
        // driverController.a().whileTrue(Commands.run(drivebase::lock, drivebase));

        // Default drive command: field-relative teleop
        drivebase.setDefaultCommand(
                drivebase.driveCommand(
                        () -> MathUtil.applyDeadband(driverController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
                        () -> MathUtil.applyDeadband(driverController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
                        () -> MathUtil.applyDeadband(driverController.getRawAxis(2),
                                OperatorConstants.RIGHT_X_DEADBAND) * 0.1));
    }

    public Command getAutonomousCommand() {
        return Commands.print("No autonomous command configured");
    }

    @Override
    public void robotInit() {
        turret = new TurretSubsystem(this);
        shooter = new ShooterSubsystem(this);

        autoaim = new AutoAim(turret, drivebase);
        shooterAim = new ShooterAim(shooter, limelight);

        configureBindings();

        // AdvantageKit
        Logger.addDataReceiver(new NT4Publisher()); // Publish data to NetworkTables

        if (isReal()) {
            Logger.addDataReceiver(new WPILOGWriter()); // Log to a USB stick ("/U/logs")
        } else {
        }


        Logger.start();

        // Simulation/AdvantaeScope Turret Mechanism
        turretMechanism = new Pose3d(0, 0, 0, new Rotation3d(0, 0, 0));
    }

    @Override
    public void robotPeriodic() {
        CommandScheduler.getInstance().run();

        LimelightHelpers.SetRobotOrientation(TurretConstants.LIMELIGHT_NAME,
                drivebase.swerveDrive.getOdometryHeading().getDegrees(), 0, 0,
                0, 0, 0);
        LimelightHelpers.PoseEstimate mt2 = LimelightHelpers
                .getBotPoseEstimate_wpiBlue_MegaTag2(TurretConstants.LIMELIGHT_NAME);

        boolean doRejectUpdate = false;
        // if our angular velocity is greater than 360 degrees per second, ignore vision
        if (drivebase.swerveDrive.getGyro().getYawAngularVelocity().abs(Units.DegreesPerSecond) > 360) {
            doRejectUpdate = true;
        }
        if (mt2.tagCount == 0) {
            doRejectUpdate = true;
        }
        if (!doRejectUpdate) {
            drivebase.swerveDrive.setVisionMeasurementStdDevs(VecBuilder.fill(.7, .7, 9999999));
            drivebase.swerveDrive.addVisionMeasurement(
                    mt2.pose,
                    mt2.timestampSeconds);
        }

        // Advantage Scope logging
        Pose2d robotPose = drivebase.getPose();
        turretMechanism = new Pose3d(robotPose.getX(), robotPose.getY(), 1.5, new Rotation3d(0, 0, Math.toRadians(autoaim.targetAngle)));
        Logger.recordOutput("Turret Mechanism", turretMechanism);
        Logger.recordOutput("Robot Pose", turretMechanism);
    }

    @Override
    public void disabledInit() {
    }

    @Override
    public void disabledPeriodic() {
    }

    @Override
    public void autonomousInit() {
        autonomousCommand = Commands.print("we should probably make an auton");
        if (autonomousCommand != null) {
            CommandScheduler.getInstance().schedule(autonomousCommand);
        }
    }

    @Override
    public void autonomousPeriodic() {
    }

    @Override
    public void teleopInit() {
        if (autonomousCommand != null) {
            CommandScheduler.getInstance().cancel(autonomousCommand);
        }

        // start autoaim
        CommandScheduler.getInstance().schedule(autoaim);

    }

    @Override
    public void teleopPeriodic() {
    }

    @Override
    public void testInit() {
        CommandScheduler.getInstance().cancelAll();
    }

    @Override
    public void testPeriodic() {
    }

    @Override
    public void simulationInit() {
    }

    @Override
    public void simulationPeriodic() {
    }
}
