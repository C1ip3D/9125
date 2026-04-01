package frc.robot.commands;


import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.ShooterSubsystem;
import frc.robot.subsystems.TransportSubsystem;

public class Shoot extends Command {
    TransportSubsystem transport;
    ArmSubsystem arm;
    ShooterSubsystem shooter;

    public Shoot(TransportSubsystem transport, ArmSubsystem arm, ShooterSubsystem shooter) {
        this.transport = transport;
        this.arm = arm;
        this.shooter = shooter;

        addRequirements(transport, arm);
    }

    @Override
    public void initialize() {
        transport.setShooting(true);
        shooter.setIdling(false);
        // arm.setDescend(false);
    }

    @Override
    public void end(boolean interrupted) {
        transport.setShooting(false);
        shooter.setIdling(true);
        // arm.setDescend(true);
    }

    @Override
    public boolean isFinished() {
        // Keep shooting until interrupted
        return false;
    }
}
