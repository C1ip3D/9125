package frc.robot;

import edu.wpi.first.wpilibj.RobotBase;
import frc.robot.commands.TurretRotationCommand;


public final class Main {
    private Main() {}

    public static void main(String... args) {
        RobotBase.startRobot(Robot::new);
        TurretRotationCommand turretRotationCommand = new TurretRotationCommand(new LimelightVision(limelight));
        

    }
}
