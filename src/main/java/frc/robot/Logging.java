package frc.robot;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.Timer;

public class Logging {
    RobotContainer robot;

    // AdvantageScope Sim
    Pose2d simulatedFuel = Pose2d.kZero;
    double flightTime = 0;
    double exitAngle = 0;

    public Logging(RobotContainer robot) {
        this.robot = robot;
    }

    public void launchBall() {
        simulatedFuel = robot.drivebase.getPose();
        flightTime = Timer.getTimestamp();
        exitAngle = robot.swerveAim.targetAngle;
    }

    public void updateLogging() {
        // Advantage Scope logging
        Pose2d robotPose = robot.drivebase.getPose();
        Logger.recordOutput("Robot Pose", robotPose);

        if (flightTime != 0) {
            double velocity = robot.shooter.rpm;
            // double velocity = 50;
            double theta = Math.toRadians(35);
            double gravity = 9.81;
            double dt = Timer.getTimestamp() - flightTime;

            double x = velocity * Math.cos(theta) * dt;
            double y = velocity * Math.cos(theta) * dt - (0.5 * gravity * (dt * dt));

            double dx = Math.cos(Math.toRadians(exitAngle)) * x;
            double dy = Math.sin(Math.toRadians(exitAngle)) * x;

            Logger.recordOutput("Fuel", new Pose3d(simulatedFuel.getX() + (dx / 1), simulatedFuel.getY() + (dy / 1),
                    (y), Rotation3d.kZero));

            if (y < 0) {
                // despawn
                flightTime = 0;
            }
        }
    }
}
