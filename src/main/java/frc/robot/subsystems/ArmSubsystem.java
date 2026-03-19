package frc.robot.subsystems;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.units.measure.Angle;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Robot;
import frc.robot.Constants.TransportConstants;
import frc.robot.Constants.TurretConstants;

public class ArmSubsystem extends SubsystemBase {
    private static final double kP = 0.02;
    private static final double kI = 0.0;
    private static final double kD = 0.0;

    private static final double MAX_SPEED = 0.2;
    private static final double TOLERANCE = 5;

    private final SparkMax armMotor = new SparkMax(TransportConstants.ARM_MOTOR_ID, MotorType.kBrushless);
    private final PIDController pidController = new PIDController(kP, kI, kD);

    public boolean descended = false;

    public ArmSubsystem(Robot robot) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.idleMode(IdleMode.kBrake);
        config.smartCurrentLimit(TurretConstants.CURRENT_LIMIT);

        armMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        armMotor.set(MAX_SPEED);

        pidController.setTolerance(TOLERANCE);
        pidController.reset();

        pidController.setSetpoint(0); // hold at 0 for fixed turret
    }

    public void setDescend(boolean descended) {
        this.descended = descended;

        if (this.descended) {
            pidController.setSetpoint(TransportConstants.DESCEND_POINT);
        }
        else {
            pidController.setSetpoint(TransportConstants.LIFT_POINT);
        }
    }

    @Override
    public void periodic() {
        double armPosition = armMotor.getEncoder().getPosition();

        double output = pidController.calculate(armPosition);

        output = MathUtil.clamp(output * 0.2, -MAX_SPEED, MAX_SPEED);
        armMotor.set(output);
    }
}
