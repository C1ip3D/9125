package frc.robot.subsystems;

import java.io.Serial;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.utils.HubPose;

public class SwerveAim extends SubsystemBase {

    private static final double kP = 0.02;
    private static final double kI = 0.0;
    private static final double kD = 0.0;

    private static final double MAX_SPEED = 1;
    private static final double TOLERANCE = 5;

    private final SwerveDrivePoseEstimator poseEstimator;
    private final PIDController pidController = new PIDController(kP, kI, kD);

    public double targetAngle;
    public double rotationControl;

    Robot robot;

    public SwerveAim(Robot robot) {
        pidController.setTolerance(TOLERANCE);
        pidController.reset();

        pidController.setSetpoint(0);

        poseEstimator = robot.drivebase.swerveDrive.swerveDrivePoseEstimator;

        this.robot = robot;
    }

    public void periodic() {
        Pose2d botPose = robot.drivebase.getPose();

        Pose2d hubPose = HubPose.getHub();

        // System.out.println("Robot X: " + botPose.getX() + ", Robot Y: " +
        // botPose.getY()
        // + ", Hub X: " + hubPose.getX() + ", Hub Y: " + hubPose.getY());
        targetAngle = Math.toDegrees(
                Math.atan2(hubPose.getY() - botPose.getY(), hubPose.getX() - botPose.getX()));

        if (targetAngle > 180) {
            targetAngle = 180 - targetAngle;
        }
        System.out.println("Theta: " + targetAngle);

        pidController.setSetpoint(targetAngle);

        double swerveHeading = poseEstimator.getEstimatedPosition().getRotation().getDegrees();
        System.out.println("Heading: " + swerveHeading);
        rotationControl = pidController.calculate(swerveHeading);

        rotationControl = MathUtil.clamp(rotationControl, -MAX_SPEED, MAX_SPEED);

    }
}
