package frc.robot.commands;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.LimelightVision;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.SwerveSubsystem;
import frc.robot.utils.HubPose;

public class ShooterAim extends Command {

    private final SwerveSubsystem swerve;
    private final ShooterSubsystem shooter;

    public ShooterAim(SwerveSubsystem swerve, ShooterSubsystem shooter) {
        this.swerve = swerve;
        this.shooter = shooter;
    }

    @Override
    public void execute() {
        Pose2d botPose = swerve.getPose();

        Pose2d hubPose = HubPose.getHub();

        double dist = Math
                .sqrt(Math.pow(botPose.getX() - hubPose.getX(), 2) + Math.pow(botPose.getY() - hubPose.getY(), 2));
        
                // System.out.println("dist: " + dist);
        
        // temporary for sim
        double velocity = 1.5 * dist;

        shooter.setRPM(velocity);
    }


    @Override
    public void end(boolean interrupted) {
    }

    @Override
    public boolean isFinished() {
        return false; // run until interrupted
    }
}