package frc.robot;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.commands.FollowPathToTagCommand;
import frc.robot.commands.RunWpilibMockAutoCommand;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.VisionSubsystem;

public class RobotContainer {
  private final VisionSubsystem visionSubsystem = new VisionSubsystem();
  private final DriveSubsystem driveSubsystem = new DriveSubsystem(visionSubsystem);

  public RobotContainer() {
    configureAutoBuilder();
    configureBindings();
  }

  private void configureBindings() {
    driveSubsystem.setDefaultCommand(
        Commands.run(
            () -> driveSubsystem.driveFieldRelative(0.0, 0.0, 0.0),
            driveSubsystem));
  }

  private void configureAutoBuilder() {
    driveSubsystem.configureAutoBuilder();
  }

  public Command getAutonomousCommand() {
    if (Constants.Mock.ENABLE_WPILIB_MOCK_AUTONOMOUS) {
      return new RunWpilibMockAutoCommand(driveSubsystem, Constants.Mock.DEPLOY_LAYOUT_RELATIVE_PATH);
    }

    try {
      PathPlannerPath path = PathPlannerPath.fromPathFile("ToCenterTag");
      return new FollowPathToTagCommand(driveSubsystem, visionSubsystem, path);
    } catch (Exception ignored) {
      return Commands.none();
    }
  }
}
