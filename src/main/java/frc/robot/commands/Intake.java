package frc.robot.commands;


import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.TransportSubsystem;

public class Intake extends Command {
    TransportSubsystem transport;

    public Intake(TransportSubsystem transport) {
        this.transport = transport;
    }

    @Override
    public void initialize() {
        transport.setIntaking(true);
    }

    @Override
    public void end(boolean interrupted) {
        transport.setIntaking(false);
    }

    @Override
    public boolean isFinished() {
        // Keep intaking until interrupted
        return false;
    }
}
