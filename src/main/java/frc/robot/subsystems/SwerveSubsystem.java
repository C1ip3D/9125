package frc.robot.subsystems;

import java.io.File;
import java.io.IOException;
import java.util.function.DoubleSupplier;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.DriveConstants;
import swervelib.SwerveDrive;
import swervelib.math.SwerveMath;
import swervelib.parser.SwerveParser;
import swervelib.telemetry.SwerveDriveTelemetry;
import swervelib.telemetry.SwerveDriveTelemetry.TelemetryVerbosity;

public class SwerveSubsystem extends SubsystemBase {

    public final SwerveDrive swerveDrive;

    public SwerveSubsystem(File directory) {
        SwerveDriveTelemetry.verbosity = TelemetryVerbosity.HIGH;

        try {
            swerveDrive = new SwerveParser(directory).createSwerveDrive(DriveConstants.MAX_SPEED);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create swerve drive from config directory: " + directory, e);
        }

        swerveDrive.setHeadingCorrection(false);
        swerveDrive.setCosineCompensator(!SwerveDriveTelemetry.isSimulation);

        // PathPlanner
        RobotConfig config;
        try {
            config = RobotConfig.fromGUISettings();
        } catch (Exception e) {
            // Handle exception as needed
            e.printStackTrace();
            return;
        }
        AutoBuilder.configure(
                this::getPose, // Robot pose supplier
                this::resetOdometry, // Method to reset odometry (will be called if your auto has a starting pose)
                this::getRobotRelativeSpeeds, // ChassisSpeeds supplier. MUST BE ROBOT RELATIVE
                (speeds, feedforwards) -> driveRobotRelative(speeds), // Method that will drive the robot given ROBOT
                                                                      // RELATIVE ChassisSpeeds. Also optionally outputs
                                                                      // individual module feedforwards
                new PPHolonomicDriveController( // PPHolonomicController is the built in path following controller for
                                                // holonomic drive trains
                        new PIDConstants(5.0, 0.0, 0.0), // Translation PID constants
                        new PIDConstants(5.0, 0.0, 0.0) // Rotation PID constants
                ),
                config, // The robot configuration
                () -> {
                    // Boolean supplier that controls when the path will be mirrored for the red
                    // alliance
                    // This will flip the path being followed to the red side of the field.
                    // THE ORIGIN WILL REMAIN ON THE BLUE SIDE

                    var alliance = DriverStation.getAlliance();
                    if (alliance.isPresent()) {
                        return alliance.get() == DriverStation.Alliance.Red;
                    }
                    return false;
                },
                this // Reference to this subsystem to set requirements
        );
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
