package frc.robot;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import frc.robot.Constants.OperatorConstants;
import frc.robot.subsystems.SwerveSubsystem;

import java.io.File;

public class RobotContainer {

    private final SwerveSubsystem drivebase = new SwerveSubsystem(
        new File(Filesystem.getDeployDirectory(), "swerve")
    );

    private final CommandXboxController driverController =
        new CommandXboxController(OperatorConstants.DRIVER_CONTROLLER_PORT);

    public RobotContainer() {
        configureBindings();

        // Default drive command: field-relative teleop
        drivebase.setDefaultCommand(
            drivebase.driveCommand(
                () -> MathUtil.applyDeadband(-driverController.getLeftY(),  OperatorConstants.LEFT_Y_DEADBAND),
                () -> MathUtil.applyDeadband(driverController.getLeftX(),  OperatorConstants.LEFT_X_DEADBAND),
                () -> MathUtil.applyDeadband(-driverController.getRightX(), OperatorConstants.RIGHT_X_DEADBAND)
            )
        );
    }

    private void configureBindings() {
        // Zero gyro on Y button press
        driverController.y().onTrue(Commands.runOnce(drivebase::zeroGyro));

        // Lock wheels in X formation while A is held
        driverController.a().whileTrue(Commands.run(drivebase::lock, drivebase));
    }

    public Command getAutonomousCommand() {
        return Commands.print("No autonomous command configured");
    }
}
