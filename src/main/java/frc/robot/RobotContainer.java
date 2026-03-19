// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.button.CommandPS4Controller;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.TurretConstants;
import frc.robot.commands.AutoAim;
import frc.robot.commands.Intake;
import frc.robot.commands.Shoot;
import frc.robot.commands.ShooterAim;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.subsystems.TransportSubsystem;
import frc.robot.subsystems.TurretSubsystem;

import java.io.File;

import org.littletonrobotics.junction.Logger;

import com.pathplanner.lib.auto.AutoBuilder;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
    // The robot's subsystems
    public final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve"));
    public final LimelightVision limelight = new LimelightVision(TurretConstants.LIMELIGHT_NAME);

    public TurretSubsystem turret = new TurretSubsystem(drivebase);
    public ShooterSubsystem shooter = new ShooterSubsystem();
    public TransportSubsystem transport = new TransportSubsystem();

    // The driver's controller
    private final CommandXboxController driverController = new CommandXboxController(
            OperatorConstants.DRIVER_CONTROLLER_PORT);
    private final CommandPS4Controller operatorController = new CommandPS4Controller(
            OperatorConstants.OPERATOR_CONTROLLER_PORT);

    AutoAim autoaim;
    ShooterAim shooterAim;

    // AdvantageScope Sim
    Pose3d turretMechanism;
    Pose2d simulatedFuel = Pose2d.kZero;
    double flightTime = 0;
    double exitAngle = 0;

    // private static final String autoname = "My Auto";
    private final SendableChooser<Command> chooser;

    private void configureBindings() {
        // Zero gyro on Y button press
        // driverController.y().onTrue(Commands.runOnce(drivebase::zeroGyro));

        // Lock wheels in X formation while A is held
        driverController.a().whileTrue(Commands.run(drivebase::lock, drivebase));

        driverController.x().whileTrue(new Shoot(transport));

        // Default drive command: field-relative teleop
        drivebase.setDefaultCommand(
                drivebase.driveCommand(
                        () -> MathUtil.applyDeadband(driverController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
                        () -> MathUtil.applyDeadband(driverController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
                        () -> MathUtil.applyDeadband(driverController.getRawAxis(2),
                                OperatorConstants.RIGHT_X_DEADBAND) * 0.05));

        driverController.a().whileTrue(new Shoot(transport)).onTrue(new InstantCommand(() -> {
            simulatedFuel = drivebase.getPose();
            flightTime = Timer.getTimestamp();
            exitAngle = autoaim.targetAngle;
        }));
        driverController.leftTrigger().whileTrue(new Intake(transport));
    }

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        autoaim = new AutoAim(turret, drivebase);
        turret.setDefaultCommand(autoaim);

        shooterAim = new ShooterAim(drivebase, shooter);
        shooter.setDefaultCommand(shooterAim);

        configureBindings();

        // Simulation/AdvantaeScope Turret Mechanism
        turretMechanism = new Pose3d(0, 0, 0, new Rotation3d(0, 0, 0));

        // Shooter tuning
        // CommandScheduler.getInstance().schedule(shooterAim);
        // driverController.leftBumper().onTrue(new InstantCommand(() -> {
        // shooter.setRPM(shooter.rpm - 50);
        // }));

        // driverController.rightBumper().onTrue(new InstantCommand(() -> {
        // shooter.setRPM(shooter.rpm + 50);
        // }));

        // chooser.setDefaultOption("Avaneesh set the default auto name here", "name of the auto here");
        // chooser.addOption("same here", "same thing here");

        chooser = AutoBuilder.buildAutoChooser();

        SmartDashboard.putData("Auto Chooser", chooser);
    }

    public void periodic() {
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
        turretMechanism = new Pose3d(robotPose.getX(), robotPose.getY(), 1.5,
                new Rotation3d(0, 0, Math.toRadians(autoaim.targetAngle)));
        Logger.recordOutput("Turret Mechanism", turretMechanism);
        Logger.recordOutput("Robot Pose", turretMechanism);

        if (flightTime != 0) {
            double velocity = shooter.rpm;
            double theta = Math.toRadians(45);
            double gravity = 9.81;
            double dt = Timer.getTimestamp() - flightTime;

            double x = velocity * Math.cos(theta) * dt;
            double y = velocity * Math.cos(theta) * dt - (0.5 * gravity * (dt * dt));

            double dx = Math.cos(Math.toRadians(exitAngle)) * x;
            double dy = Math.sin(Math.toRadians(exitAngle)) * x;

            Logger.recordOutput("Fuel", new Pose3d(simulatedFuel.getX() + (dx / 1), simulatedFuel.getY() + (dy / 1),
                    (y), Rotation3d.kZero));

            if (y < 0) {
                // despawn
                flightTime = 0;
            }
        }
    }

    public Command getAutonomousCommand() {
        return chooser.getSelected();
    }
}