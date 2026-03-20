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
// import frc.robot.commands.AutoAim;
import frc.robot.commands.Intake;
import frc.robot.commands.Shoot;
import frc.robot.commands.ShooterAim;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.SwerveAim;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.subsystems.TransportSubsystem;
import frc.robot.subsystems.TurretSubsystem;

import java.io.File;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;

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
    public ArmSubsystem arm = new ArmSubsystem();

    // The driver's controller
    private final CommandXboxController driverController = new CommandXboxController(
            OperatorConstants.DRIVER_CONTROLLER_PORT);
    private final CommandPS4Controller operatorController = new CommandPS4Controller(
            OperatorConstants.OPERATOR_CONTROLLER_PORT);

    Logging logging;
    // AutoAim autoaim;
    SwerveAim swerveAim;
    ShooterAim shooterAim;

    // private static final String autoname = "My Auto";
    private final SendableChooser<Command> chooser;

    private void configureBindings() {
        // ---- Driver controls

        // Zero gyro on Y button press
        // driverController.y().onTrue(Commands.runOnce(drivebase::zeroGyro));

        // Lock wheels in X formation while A is held
        driverController.x().whileTrue(Commands.run(drivebase::lock, drivebase));

        // Default drive command: field-relative teleop
        drivebase.setDefaultCommand(
                drivebase.driveCommand(
                        () -> MathUtil.applyDeadband(driverController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
                        () -> MathUtil.applyDeadband(driverController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
                        () -> {
                            if (driverController.b().getAsBoolean()) {
                                return swerveAim.rotationControl;
                            } else {
                                return MathUtil.applyDeadband(driverController.getRightX(),
                                        OperatorConstants.RIGHT_X_DEADBAND);
                            }
                        }
                // () -> MathUtil.applyDeadband(driverController.getRawAxis(2),
                // OperatorConstants.RIGHT_X_DEADBAND) * 0.05));
                ));

        // ---- Operator Controls

        operatorController.cross().whileTrue(new Intake(transport));

        operatorController.triangle().whileTrue(new Shoot(transport, arm));

        // Variable arm control unless already overrided
        if (arm.getCurrentCommand() == null) {
            arm.setPosition(operatorController.getL2Axis());
        }

        // driverController.leftTrigger().whileTrue(new Intake(transport));
    }

    /**
     * The container for the robot. Contains subsystems, OI devices, and commands.
     */
    public RobotContainer() {
        logging = new Logging(this);

        // autoaim = new AutoAim(turret, drivebase);
        // turret.setDefaultCommand(autoaim);

        swerveAim = new SwerveAim(this);
        shooterAim = new ShooterAim(drivebase, shooter);
        shooter.setDefaultCommand(shooterAim);

        // Auton commands
        NamedCommands.registerCommand("Shoot", new Shoot(transport, arm));
        NamedCommands.registerCommand("Intake", new Intake(transport));

        configureBindings();

        // Simulation/AdvantaeScope Turret Mechanism

        // Shooter tuning
        // CommandScheduler.getInstance().schedule(shooterAim);
        // driverController.leftBumper().onTrue(new InstantCommand(() -> {
        // shooter.setRPM(shooter.rpm - 50);
        // }));

        // driverController.rightBumper().onTrue(new InstantCommand(() -> {
        // shooter.setRPM(shooter.rpm + 50);
        // }));

        // chooser.setDefaultOption("Avaneesh set the default auto name here", "name of
        // the auto here");
        // chooser.addOption("same here", "same thing here");

        chooser = AutoBuilder.buildAutoChooser();

        SmartDashboard.putData("Auto Chooser", chooser);
    }

    public Command getAutonomousCommand() {
        return chooser.getSelected();
    }
}