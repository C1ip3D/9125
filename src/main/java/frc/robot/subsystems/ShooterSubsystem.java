package frc.robot.subsystems;

import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants;

public class ShooterSubsystem extends SubsystemBase {
    private final SparkMax shooterMotor1;
    private final SparkMax shooterMotor2;

    private static final double kP = 0.02;
    private static final double kI = 0.0;
    private static final double kD = 0.0;
    private static final double TOLERANCE = 5;

    public double rpm = 0;

    // still not sure about using 2 seperate PID controllers, maybe we can average
    // the velocity of both motors?
    private final PIDController pidController1 = new PIDController(kP, kI, kD);
    private final PIDController pidController2 = new PIDController(kP, kI, kD);

    public ShooterSubsystem() {
        shooterMotor1 = new SparkMax(Constants.ShooterConstants.SHOOTER_MOTOR_1_ID, MotorType.kBrushless);
        shooterMotor2 = new SparkMax(Constants.ShooterConstants.SHOOTER_MOTOR_2_ID, MotorType.kBrushless);

        SparkMaxConfig config_ = new SparkMaxConfig();
        config_.idleMode(IdleMode.kCoast); // maintain intertia between spin ups
        shooterMotor1.configure(config_, ResetMode.kResetSafeParameters,
                com.revrobotics.PersistMode.kPersistParameters);
        shooterMotor2.configure(config_, ResetMode.kResetSafeParameters,
                com.revrobotics.PersistMode.kPersistParameters);

        pidController1.setTolerance(TOLERANCE);
        pidController2.setTolerance(TOLERANCE);
    }

    public void setRPM(double rpm) {
        this.rpm = rpm;
        pidController1.setSetpoint(rpm);
        pidController2.setSetpoint(rpm);
    }

    public boolean atSpeed() {
        return pidController1.atSetpoint() && pidController2.atSetpoint();
    }

    @Override
    public void periodic() {
        shooterMotor1.set(pidController1.calculate(shooterMotor1.getEncoder().getVelocity()));
        shooterMotor2.set(pidController2.calculate(shooterMotor2.getEncoder().getVelocity()));
    }
}
