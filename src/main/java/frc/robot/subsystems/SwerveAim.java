package frc.robot.subsystems;

import java.io.Serial;

import com.ctre.phoenix6.hardware.Pigeon2;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Transform2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.Constants.DriveConstants;
import frc.robot.utils.HubPose;

public class SwerveAim extends SubsystemBase {

    private static final double kP = 0.002;
    private static final double kI = 0.0;
    private static final double kD = 0.0;

    private static final double MAX_SPEED = 1;
    private static final double TOLERANCE = 5;

    private final SwerveDrivePoseEstimator poseEstimator;
    public final PIDController pidController = new PIDController(kP, kI, kD);

    public double targetAngle;
    public double rotationControl;

    RobotContainer robot;

    public SwerveAim(RobotContainer robot) {
        pidController.setTolerance(TOLERANCE);
        pidController.reset();

        pidController.setSetpoint(0);

        poseEstimator = robot.drivebase.swerveDrive.swerveDrivePoseEstimator;

        pidController.enableContinuousInput(-180, 180);
        this.robot = robot;
    }

    public void periodic() {
        Pose2d botPose = robot.drivebase.getPose();

        Pose2d hubPose = HubPose.getHub();

        double time = 2; // for now flight time is constant
        ChassisSpeeds speeds = robot.drivebase.swerveDrive.getFieldVelocity();

        botPose.plus(new Transform2d(speeds.vxMetersPerSecond * time, speeds.vyMetersPerSecond * time, Rotation2d.kZero));

        targetAngle = Math.toDegrees(
                Math.atan2(hubPose.getY() - botPose.getY(), hubPose.getX() - botPose.getX()));

        if (targetAngle > 180) {
            targetAngle = -(360 - targetAngle);
        }
        System.out.println("Theta: " + targetAngle);

        pidController.setSetpoint(targetAngle);

        double swerveHeading = poseEstimator.getEstimatedPosition().getRotation().getDegrees();
        System.out.println("Heading: " + swerveHeading);
        rotationControl = pidController.calculate(swerveHeading);

        rotationControl = MathUtil.clamp(rotationControl, -MAX_SPEED, MAX_SPEED);

    }
}