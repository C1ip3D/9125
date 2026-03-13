package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.Command; //add comment
import frc.robot.LimelightVision;
import frc.robot.Robot;

public class TurretRotationCommand extends Command {

    private final LimelightVision limelight;
    private final PIDController pidController;

    private static final double kP = 0.02;  // TODO: tune
    private static final double kI = 0.0;   // TODO: tune
    private static final double kD = 0.0;   // TODO: tune
    private static final double MAX_SPEED = 0.5;  // TODO: tune - hopefully iy can be 1.0 but probably not 
    private static final double TOLERANCE = 1.0;  // how close ot target angle is acceptable ;)

    public TurretRotationCommand(LimelightVision limelight) {
        this.limelight = limelight;
        this.pidController = new PIDController(kP, kI, kD);
        this.pidController.setSetpoint(0.0); // target: tag centered (tx = 0)
        this.pidController.setTolerance(TOLERANCE);
    }

    @Override
    public void initialize() {
        pidController.reset();
    }

    @Override
    public void execute() {
        if (limelight.hasTarget()) {
            double tx = limelight.getTX();
            System.out.println("Limelight tx: " + tx); // Debugging output
            // double output = pidController.calculate(tx);
            // output = MathUtil.clamp(output, -MAX_SPEED, MAX_SPEED);
            // Robot.turretMotor.set(output);
            if(tx > 0.5){
                Robot.turretMotor.set(0.4);
            }else if (tx < -0.5){
                Robot.turretMotor.set(-0.4);

            }else{Robot.turretMotor.set(0.0);};
        } else {
            Robot.turretMotor.set(0.0);
        }
    }

    @Override
    public void end(boolean interrupted) {
        Robot.turretMotor.set(0.0);
    }

    @Override
    public boolean isFinished() {
        return false; // run until interrupted
    }
}