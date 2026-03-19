package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.subsystems.ArmSubsystem;

public class RaiseArm extends Command {
    ArmSubsystem arm;

    public RaiseArm(ArmSubsystem arm) {
        this.arm = arm;

        addRequirements(arm);
    }

    @Override
    public void initialize() {
        arm.setDescend(false);
    }

    @Override
    public void end(boolean interrupted) {
        arm.setDescend(true);
    }

    @Override
    public boolean isFinished() {
        // Keep intaking until interrupted
        return false;
    }
}
