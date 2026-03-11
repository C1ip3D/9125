package frc.robot.subsystems;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.SparkBase.PersistMode;
import com.revrobotics.spark.SparkBase.ResetMode;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.TurretConstants;

/**
 * Subsystem that owns and controls the turret rotation motor.
 * The motor is a SparkMax (NEO) with brake mode to hold position when stopped.
 */
public class TurretSubsystem extends SubsystemBase {

    private final SparkMax turretMotor;

    public TurretSubsystem() {
        turretMotor = new SparkMax(TurretConstants.TURRET_MOTOR_ID, MotorType.kBrushless);

        SparkMaxConfig config = new SparkMaxConfig();
        config.idleMode(IdleMode.kBrake);
        config.smartCurrentLimit(TurretConstants.CURRENT_LIMIT);

        turretMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    /**
     * Sets the turret motor output directly.
     * Positive = one direction, negative = other. Adjust sign in TurretRotationCommand if needed.
     */
    public void set(double speed) {
        turretMotor.set(speed);
    }

    /** Stops the turret motor. */
    public void stop() {
        turretMotor.set(0.0);
    }

    @Override
    public void periodic() {
        // Nothing needed here for now — tracking logic lives in TurretRotationCommand
    }
}
