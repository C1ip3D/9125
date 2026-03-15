package frc.robot;

import java.io.File;

import com.ctre.phoenix6.hardware.Pigeon2;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OperatorConstants;
import frc.robot.Constants.TurretConstants;
import frc.robot.commands.AutoAim;
import frc.robot.commands.ShooterAim;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.subsystems.TurretSubsystem;

public class Robot extends TimedRobot {
    public final SwerveSubsystem drivebase = new SwerveSubsystem(new File(Filesystem.getDeployDirectory(), "swerve"));
    public final LimelightVision limelight = new LimelightVision(TurretConstants.LIMELIGHT_NAME);
    public final Pigeon2 imu = new Pigeon2(13);
    private final CommandXboxController driverController = new CommandXboxController(
            OperatorConstants.DRIVER_CONTROLLER_PORT);

    public Command autonomousCommand;
    public TurretSubsystem turret;
    public AutoAim autoaim;
    public ShooterAim shooterAim;

    public ShooterSubsystem shooter;

    private void configureBindings() {
        // Zero gyro on Y button press
        // driverController.y().onTrue(Commands.runOnce(drivebase::zeroGyro));

        // Lock wheels in X formation while A is held
        driverController.a().whileTrue(Commands.run(drivebase::lock, drivebase));
    }

    public Command getAutonomousCommand() {
        return Commands.print("No autonomous command configured");
    }

    @Override
    public void robotInit() {
        configureBindings();

        // Default drive command: field-relative teleop
        drivebase.setDefaultCommand(
                drivebase.driveCommand(
                        () -> MathUtil.applyDeadband(driverController.getLeftY(), OperatorConstants.LEFT_Y_DEADBAND),
                        () -> MathUtil.applyDeadband(driverController.getLeftX(), OperatorConstants.LEFT_X_DEADBAND),
                        () -> MathUtil.applyDeadband(driverController.getRightX(),
                                OperatorConstants.RIGHT_X_DEADBAND)));

        turret = new TurretSubsystem(this);
        shooter = new ShooterSubsystem(this);

        autoaim = new AutoAim(turret, limelight);
        shooterAim = new ShooterAim(shooter, limelight);
    }

    @Override
    public void robotPeriodic() {
        CommandScheduler.getInstance().run();

        // Update Odometry to Limelight
        // if (limelight.hasTarget()) {
        //     Pose2d pose = LimelightHelpers.getBotPose2d(TurretConstants.LIMELIGHT_NAME);
        //     drivebase.resetOdometry(pose);
        // }
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
