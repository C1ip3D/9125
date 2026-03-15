package frc.robot.commands;

import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.LimelightHelpers;
import frc.robot.LimelightVision;
import edu.wpi.first.units.Units;
import frc.robot.subsystems.TurretSubsystem;

public class AutoAim extends Command {

    private TurretSubsystem turret;
    private LimelightVision limelight;

    public AutoAim(TurretSubsystem turret, LimelightVision limelight) {
        addRequirements(getRequirements());
    }

    @Override
    public void execute() {
        if (limelight.hasTarget()) {
            Pose3d hubPose = LimelightHelpers.getTargetPose3d_RobotSpace(Constants.TurretConstants.LIMELIGHT_NAME);
            double targetAngle = Math.toDegrees(Math.atan2(-hubPose.getZ(), hubPose.getX()));

            System.out.println("Theta: "+ targetAngle);

            turret.set(Units.Degrees.of(targetAngle));
        } else {
            System.out.println("No AprilTag");
            turret.set(Units.Degree.of(0));
        }
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