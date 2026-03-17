package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import java.util.Optional;

public class VisionSubsystem extends SubsystemBase {
  public record VisionEstimate(Pose2d pose, double timestampSeconds) {}

  private final NetworkTable limelightTable;
  private final NetworkTableEntry botposeBlue;

  public VisionSubsystem() {
    limelightTable = NetworkTableInstance.getDefault().getTable(Constants.Vision.LIMELIGHT_TABLE);
    botposeBlue = limelightTable.getEntry("botpose_wpiblue");
  }

  public Optional<VisionEstimate> getLatestEstimate() {
    double[] poseArray = botposeBlue.getDoubleArray(new double[0]);
    if (poseArray.length < 7) {
      return Optional.empty();
    }

    double xMeters = poseArray[0];
    double yMeters = poseArray[1];
    double yawDegrees = poseArray[5];

    Pose2d estimate = new Pose2d(xMeters, yMeters, Rotation2d.fromDegrees(yawDegrees));

    double latencyMs = poseArray[6];
    double timestamp = Timer.getFPGATimestamp() - (latencyMs / 1000.0);

    return Optional.of(new VisionEstimate(estimate, timestamp));
  }
}
