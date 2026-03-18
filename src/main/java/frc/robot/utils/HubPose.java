package frc.robot.utils;

import java.util.Optional;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.Constants.FieldConstants;

public class HubPose {
    public static Pose2d getHub() {
        Optional<Alliance> ally = DriverStation.getAlliance();
        if (ally.isPresent()) {
            if (ally.get() == Alliance.Red) {
                return new Pose2d(FieldConstants.RED_HUB_X, FieldConstants.HUB_Y, Rotation2d.kZero);
            }
            if (ally.get() == Alliance.Blue) {
                return new Pose2d(FieldConstants.BLUE_HUB_X, FieldConstants.HUB_Y, Rotation2d.kZero);
            }
        }
        System.out.println("Awaiting alliance color...");
        return Pose2d.kZero;

    }
}
