package frc.robot.commands;


import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.TransportSubsystem;

public class Shoot extends Command {
    TransportSubsystem transport;

    public Shoot(TransportSubsystem transport) {
        this.transport = transport;
    }

    @Override
    public void initialize() {
        transport.setShooting(true);
    }

    @Override
    public void end(boolean interrupted) {
        transport.setShooting(false);
    }

    @Override
    public boolean isFinished() {
        // Keep shooting until interrupted
        return false;
    }
}
