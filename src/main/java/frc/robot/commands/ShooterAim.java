package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.LimelightVision;
import frc.robot.subsystems.ShooterSubsystem;

public class ShooterAim extends Command {

    private final LimelightVision limelight;
    private final ShooterSubsystem shooter;

    double targetHeight = 44.25; // aprilTag distance from center to ground in inches
    double cameraHeight = 24.0; // height of the limelight from the ground in inches
    double cameraAngle = 0; // angle of the limelight from horizontal in degrees
    double CurrdistanceFromTarget;
    double MaxDistanceFromTarget = 181.56; // maximum possible shooting distance
    double MinDistanceFromTarget = 5.0; // setting min distance ( because we aren't crashing into the wall )
    double shootPower = 0.0;

    public ShooterAim(ShooterSubsystem shooter, LimelightVision limelight) {
        this.limelight = limelight;
        this.shooter = shooter;
    }

    @Override
    public void initialize() {

    }

    @Override
    public void execute() {
        if (limelight.hasTarget()) {
            double hubTY = limelight.getTY();

            CurrdistanceFromTarget = (targetHeight - cameraHeight) / Math.tan(Math.toRadians(cameraAngle + hubTY));
            System.out.println("Distance from target: " + CurrdistanceFromTarget); // Generate distance from AprilTag

            // Based on testing a power of 0.5 is enough for max distance ( 181.56 in ), so
            // lets take that as our max power for max distance
            // Lets take 0.2 as min power(for now - needs to be tested)
            // power range: [0.2 - 0.5]
            // establish a basic linear relationship between the currDistance and power

            double multiplier = CurrdistanceFromTarget / MaxDistanceFromTarget;
            shootPower = 0.2 + (0.3 * multiplier); // scale power between 0.2 and 0.5 based on distance

            shooter.setRPM(shootPower);
        } else {
        }
    }

    @Override
    public void end(boolean interrupted) {
    }

    @Override
    public boolean isFinished() {
        return false; // run until interrupted
    }
}