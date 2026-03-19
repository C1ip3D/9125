package frc.robot.commands;


import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.ArmSubsystem;
import frc.robot.subsystems.TransportSubsystem;

public class Shoot extends Command {
    TransportSubsystem transport;
    ArmSubsystem arm;

    public Shoot(TransportSubsystem transport, ArmSubsystem arm) {
        this.transport = transport;
        this.arm = arm;

        addRequirements(transport, arm);
    }

    @Override
    public void initialize() {
        transport.setShooting(true);
        arm.setDescend(false);
    }

    @Override
    public void end(boolean interrupted) {
        transport.setShooting(false);
        arm.setDescend(true);

    }

    @Override
    public boolean isFinished() {
        // Keep shooting until interrupted
        return false;
    }
}
