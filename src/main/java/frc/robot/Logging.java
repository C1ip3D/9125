package frc.robot;

import org.littletonrobotics.junction.Logger;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.wpilibj.Timer;

public class Logging {
    RobotContainer robot;

    // AdvantageScope Sim
    Pose3d turretMechanism;
    Pose2d simulatedFuel = Pose2d.kZero;
    double flightTime = 0;
    double exitAngle = 0;

    public Logging(RobotContainer robot) {
        this.robot = robot;

        turretMechanism = new Pose3d(0, 0, 0, new Rotation3d(0, 0, 0));

    }

    public void launchBall() {
        simulatedFuel = robot.drivebase.getPose();
        flightTime = Timer.getTimestamp();
        exitAngle = robot.autoaim.targetAngle;
    }

    public void updateLogging() {
        // Advantage Scope logging
        Pose2d robotPose = robot.drivebase.getPose();
        turretMechanism = new Pose3d(robotPose.getX(), robotPose.getY(), 1.5,
                new Rotation3d(0, 0, Math.toRadians(robot.autoaim.targetAngle)));
        Logger.recordOutput("Turret Mechanism", turretMechanism);
        Logger.recordOutput("Robot Pose", turretMechanism);

        if (flightTime != 0) {
            double velocity = robot.shooter.rpm;
            double theta = Math.toRadians(45);
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
