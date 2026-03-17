package frc.robot.commands;

import java.util.Optional;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.LimelightHelpers;
import frc.robot.LimelightVision;
import frc.robot.Constants.FieldConstants;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.subsystems.TurretSubsystem;

public class AutoAim extends Command {

    private TurretSubsystem turret;
    private SwerveSubsystem drivebase;

    public double targetAngle;

    public AutoAim(TurretSubsystem turret, SwerveSubsystem drivebase) {
        this.turret = turret;
        this.drivebase = drivebase;
    }

    @Override
    public void execute() {
        // if (drivebase.hasTarget()) {

        // turret.set(Units.Degrees.of(targetAngle));
        // } else {
        // System.out.println("No AprilTag");
        // turret.set(Units.Degree.of(0));
        // }

        // Pose3d hubPose =
        // LimelightHelpers.getTargetPose3d_RobotSpace(Constants.TurretConstants.LIMELIGHT_NAME);
        // double targetAngle = Math.toDegrees(Math.atan2(-hubPose.getZ(),
        // hubPose.getX()));

        // System.out.println("Theta: "+ targetAngle);
        
        Pose2d botPose = drivebase.getPose();

        Pose2d hubPose = Pose2d.kZero;

        // Should this be run every cycle or in init?
        Optional<Alliance> ally = DriverStation.getAlliance();
        if (ally.isPresent()) {
            if (ally.get() == Alliance.Red) {
                hubPose = new Pose2d(FieldConstants.RED_HUB_X, FieldConstants.HUB_Y, Rotation2d.kZero);
            }
            if (ally.get() == Alliance.Blue) {
                hubPose = new Pose2d(FieldConstants.BLUE_HUB_X, FieldConstants.HUB_Y, Rotation2d.kZero);
            }
        } else {
            System.out.println("Awaiting alliance color...");
            return;
        }

        System.out.println("Robot X: " + botPose.getX() + ", Robot Y: " + botPose.getY() + ", Hub X: " + hubPose.getX() + ", Hub Y: " + hubPose.getY());
        targetAngle = Math.toDegrees(Math.atan2(hubPose.getY() - botPose.getY(), hubPose.getX() - botPose.getX()));
        System.out.println("Theta: " + targetAngle);
        turret.set(Units.Degrees.of(targetAngle));
    }

    @Override
    public void end(boolean interrupted) {
        turret.set(Units.Degree.of(0));
    }

    @Override
    public boolean isFinished() {
        return false; // run until interrupted
    }
}