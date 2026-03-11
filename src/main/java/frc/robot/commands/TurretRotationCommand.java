package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.LimelightVision;
import frc.robot.subsystems.TurretSubsystem;

public class TurretRotationCommand extends Command {

    private final LimelightVision limelight;
    private final TurretSubsystem turret;
    private final PIDController pidController;

    private static final double kP = 0.02;  // TODO: tune
    private static final double kI = 0.0;   // TODO: tune
    private static final double kD = 0.0;   // TODO: tune
    private static final double MAX_SPEED = 0.5;  // TODO: tune - hopefully it can be 1.0 but probably not
    private static final double TOLERANCE = 1.0;  // how close to target angle is acceptable ;)

    public TurretRotationCommand(LimelightVision limelight, TurretSubsystem turret) {
        this.limelight = limelight;
        this.turret = turret;
        this.pidController = new PIDController(kP, kI, kD);
        this.pidController.setSetpoint(0.0); // target: tag centered (tx = 0)
        this.pidController.setTolerance(TOLERANCE);
        addRequirements(turret); // tells the scheduler this command owns the turret
    }

    @Override
    public void initialize() {
        pidController.reset();
    }

    @Override
    public void execute() {
        if (limelight.hasTarget()) {
            double tx = limelight.getTX();
            double output = pidController.calculate(tx);
            output = MathUtil.clamp(output, -MAX_SPEED, MAX_SPEED);
            turret.set(output);
        } else {
            turret.stop();
        }
    }

    @Override
    public void end(boolean interrupted) {
        turret.stop();
    }

    @Override
    public boolean isFinished() {
        return false; // run until interrupted
    }
}
