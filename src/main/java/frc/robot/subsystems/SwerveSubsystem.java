package frc.robot.subsystems;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DriveConstants;
import swervelib.SwerveDrive;
import swervelib.math.SwerveMath;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;

import java.io.File;
import java.io.IOException;
import java.util.function.DoubleSupplier;

public class SwerveSubsystem extends SubsystemBase {

    private final SwerveDrive swerveDrive;

    public SwerveSubsystem(File directory) {
        SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH;

        try {
            swerveDrive = new SwerveParser(directory).createSwerveDrive(DriveConstants.MAX_SPEED);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create swerve drive from config directory: " + directory, e);
        }

        swerveDrive.setHeadingCorrection(false);
        swerveDrive.setCosineCompensator(!SwerveDriveTelemetry.isSimulation);
    }

    // --- Drive Commands ---

    /**
     * Field-relative drive command using left stick for translation, right stick
     * for rotation.
     */
    public Command driveCommand(DoubleSupplier translationX, DoubleSupplier translationY,
            DoubleSupplier angularRotation) {
        return run(() -> {
            swerveDrive.drive(
                    SwerveMath.scaleTranslation(
                            new edu.wpi.first.math.geometry.Translation2d(
                                    translationX.getAsDouble() * swerveDrive.getMaximumChassisVelocity(),
                                    translationY.getAsDouble() * swerveDrive.getMaximumChassisVelocity()),
                            1.0),
                    angularRotation.getAsDouble() * swerveDrive.getMaximumChassisAngularVelocity() * 8.0,
                    true, // field-relative
                    false // open loop
            );
        });
    }

    /**
     * Robot-relative drive with explicit ChassisSpeeds (used by path followers).
     */
    public void driveRobotRelative(ChassisSpeeds speeds) {
        swerveDrive.drive(speeds);
    }

    /** Lock wheels in X formation to resist being pushed. */
    public void lock() {
        swerveDrive.lockPose();
    }

    // --- Gyro / Pose ---

    /** Zero the gyro heading. */
    public void zeroGyro() {
        swerveDrive.zeroGyro();
    }

    /** Get the current robot pose from the odometry. */
    public Pose2d getPose() {
        return swerveDrive.getPose();
    }

    /** Reset odometry to a given pose. */
    public void resetOdometry(Pose2d pose) {
        swerveDrive.resetOdometry(pose);
    }

    /** Get the current robot heading as a Rotation2d. */
    public Rotation2d getHeading() {
        return getPose().getRotation();
    }

    // --- Kinematics ---

    public SwerveDriveKinematics getKinematics() {
        return swerveDrive.kinematics;
    }

    public ChassisSpeeds getRobotRelativeSpeeds() {
        return swerveDrive.getRobotVelocity();
    }

    public ChassisSpeeds getFieldRelativeSpeeds() {
        return swerveDrive.getFieldVelocity();
    }

    // --- Periodic ---

    @Override
    public void periodic() {
        swerveDrive.updateOdometry();
    }

    @Override
    public void simulationPeriodic() {
    }
}
