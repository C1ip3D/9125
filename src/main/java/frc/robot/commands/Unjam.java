package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.TransportSubsystem;

public class Unjam extends Command {
        TransportSubsystem transport;

    public Unjam(TransportSubsystem transport) {
        this.transport = transport;

        addRequirements(transport);
    }

    @Override
    public void initialize() {
        transport.setReversing(true);
    }

    @Override
    public void end(boolean interrupted) {
        transport.setReversing(false);
    }

    @Override
    public boolean isFinished() {
        // Keep intaking until interrupted
        return false;
    }
}
