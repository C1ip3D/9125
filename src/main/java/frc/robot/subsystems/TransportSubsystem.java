package frc.robot.subsystems;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.TransportConstants;

public class TransportSubsystem extends SubsystemBase {
    final SparkMax intakeMotor = new SparkMax(TransportConstants.INTAKE_MOTOR_ID, MotorType.kBrushless);
    final SparkMax rollerMotor = new SparkMax(TransportConstants.ROLLER_MOTOR_ID, MotorType.kBrushless);
    final SparkMax transferMotor = new SparkMax(TransportConstants.TRANSFER_MOTOR_ID, MotorType.kBrushless);

    private boolean shooting = false;
    private boolean intaking = false;
    private boolean reversing = false;

    public TransportSubsystem() {
        // Init motors
        SparkMaxConfig transportConfig_ = new SparkMaxConfig();
        transportConfig_.idleMode(IdleMode.kCoast);

        intakeMotor.configure(transportConfig_, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        rollerMotor.configure(transportConfig_, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        transferMotor.configure(transportConfig_, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    private void updateMotors() {
        double rollerPower = 0;

        if (reversing) {
            intakeMotor.set(-TransportConstants.INTAKE_POWER);
            rollerMotor.set(TransportConstants.ROLLER_INTAKE_POWER);
            transferMotor.set(TransportConstants.TRANSFER_POWER);

        } else {

            if (intaking) {
                intakeMotor.set(TransportConstants.INTAKE_POWER);
                rollerPower = TransportConstants.ROLLER_INTAKE_POWER;
            } else {
                intakeMotor.set(0);
            }

            // IMPORTANT: shooting power has to be set after intake power
            if (shooting) {
                transferMotor.set(TransportConstants.TRANSFER_POWER);
                rollerPower = TransportConstants.ROLLER_SHOOT_POWER;
            } else {
                transferMotor.set(0);
            }

            rollerMotor.set(rollerPower);
        }
    }

    public void setShooting(boolean shooting) {
        this.shooting = shooting;
        updateMotors();
    }

    public void setIntaking(boolean intaking) {
        this.intaking = intaking;
        updateMotors();
    }

    public void setReversing(boolean reversing) {
        this.reversing = true;
        updateMotors();
    }

}
