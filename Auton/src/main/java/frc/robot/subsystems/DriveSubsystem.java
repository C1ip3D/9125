package frc.robot.subsystems;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveDrivePoseEstimator;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.RobotBase;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;
import java.util.Optional;
import java.util.function.Supplier;

public class DriveSubsystem extends SubsystemBase {
  private final VisionSubsystem visionSubsystem;

  private final SwerveDriveKinematics kinematics =
      new SwerveDriveKinematics(
          Constants.Drive.FRONT_LEFT_LOCATION,
          Constants.Drive.FRONT_RIGHT_LOCATION,
          Constants.Drive.REAR_LEFT_LOCATION,
          Constants.Drive.REAR_RIGHT_LOCATION);

  private final SwerveDrivePoseEstimator poseEstimator;
  private Pose2d simulatedPose = new Pose2d();
  private ChassisSpeeds commandedSpeeds = new ChassisSpeeds();

  private final SwerveModulePosition[] modulePositions =
      new SwerveModulePosition[] {
        new SwerveModulePosition(),
        new SwerveModulePosition(),
        new SwerveModulePosition(),
        new SwerveModulePosition()
      };

  public DriveSubsystem(VisionSubsystem visionSubsystem) {
    this.visionSubsystem = visionSubsystem;

    poseEstimator =
        new SwerveDrivePoseEstimator(
            kinematics,
            getHeading(),
            modulePositions,
            new Pose2d(),
            Constants.Vision.STATE_STD_DEVS,
            Constants.Vision.VISION_STD_DEVS);
  }

  @Override
  public void periodic() {
    if (RobotBase.isSimulation()) {
      integrateSimulationPose(0.02);
    }

    poseEstimator.update(getHeading(), modulePositions);

    Optional<VisionSubsystem.VisionEstimate> maybeEstimate = visionSubsystem.getLatestEstimate();
    maybeEstimate.ifPresent(
        estimate -> poseEstimator.addVisionMeasurement(estimate.pose(), estimate.timestampSeconds()));
  }

  public void driveFieldRelative(double vxMetersPerSec, double vyMetersPerSec, double omegaRadPerSec) {
    ChassisSpeeds targetSpeeds =
        ChassisSpeeds.fromFieldRelativeSpeeds(vxMetersPerSec, vyMetersPerSec, omegaRadPerSec, getHeading());
    driveRobotRelative(targetSpeeds);
  }

  public void driveRobotRelative(ChassisSpeeds chassisSpeeds) {
    commandedSpeeds = chassisSpeeds;
    SwerveModuleState[] targetStates = kinematics.toSwerveModuleStates(chassisSpeeds);
    setModuleStates(targetStates);
  }

  public Pose2d getPose() {
    return poseEstimator.getEstimatedPosition();
  }

  public void resetPose(Pose2d pose) {
    simulatedPose = pose;
    poseEstimator.resetPosition(getHeading(), modulePositions, pose);
  }

  public ChassisSpeeds getRobotRelativeSpeeds() {
    return commandedSpeeds;
  }

  public Rotation2d getHeading() {
    if (RobotBase.isSimulation()) {
      return simulatedPose.getRotation();
    }
    return new Rotation2d();
  }

  public void setModuleStates(SwerveModuleState[] states) {
  }

  private void integrateSimulationPose(double dtSeconds) {
    ChassisSpeeds fieldRelativeSpeeds =
        ChassisSpeeds.fromRobotRelativeSpeeds(commandedSpeeds, simulatedPose.getRotation());

    Pose2d nextPose =
        new Pose2d(
            simulatedPose.getX() + fieldRelativeSpeeds.vxMetersPerSecond * dtSeconds,
            simulatedPose.getY() + fieldRelativeSpeeds.vyMetersPerSecond * dtSeconds,
            simulatedPose
                .getRotation()
                .plus(new Rotation2d(fieldRelativeSpeeds.omegaRadiansPerSecond * dtSeconds)));

    simulatedPose = nextPose;
    poseEstimator.resetPosition(simulatedPose.getRotation(), modulePositions, simulatedPose);
  }

  public void configureAutoBuilder() {
    RobotConfig config;
    try {
      config = RobotConfig.fromGUISettings();
    } catch (Exception ex) {
      DriverStation.reportError("PathPlanner RobotConfig missing from GUI settings", ex.getStackTrace());
      return;
    }

    Supplier<Boolean> shouldFlip =
        () -> DriverStation.getAlliance().isPresent() && DriverStation.getAlliance().get() == DriverStation.Alliance.Red;

    AutoBuilder.configure(
        this::getPose,
        this::resetPose,
        this::getRobotRelativeSpeeds,
        this::driveRobotRelative,
        new PPHolonomicDriveController(new PIDConstants(4.0, 0.0, 0.0), new PIDConstants(3.0, 0.0, 0.0)),
        config,
        shouldFlip,
        this);
  }
}
