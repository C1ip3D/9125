package frc.robot.commands;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.path.PathPlannerPath;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.DriveSubsystem;
import frc.robot.subsystems.VisionSubsystem;

public class FollowPathToTagCommand extends Command {
  private final Command wrapped;

  public FollowPathToTagCommand(
      DriveSubsystem driveSubsystem,
      VisionSubsystem visionSubsystem,
      PathPlannerPath path) {
    wrapped =
        Commands.sequence(
            Commands.runOnce(
                () -> visionSubsystem.getLatestEstimate().ifPresent(e -> driveSubsystem.resetPose(e.pose()))),
            AutoBuilder.followPath(path));
  }

  @Override
  public void initialize() {
    wrapped.initialize();
  }

  @Override
  public void execute() {
    wrapped.execute();
  }

  @Override
  public void end(boolean interrupted) {
    wrapped.end(interrupted);
  }

  @Override
  public boolean isFinished() {
    return wrapped.isFinished();
  }
}
