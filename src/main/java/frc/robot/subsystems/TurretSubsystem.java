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
import frc.robot.Constants.TurretConstants;

public class TurretSubsystem extends SubsystemBase {

    private static final double kP = 0.02;
    private static final double kI = 0.0;
    private static final double kD = 0.0;

    private static final double MAX_SPEED = 0.2;
    private static final double TOLERANCE = 5;

    private static final double GEAR_RATIO = (double) (1.0 / 40.0); // 1:4 gearbox * 20:200 turret

    private final SwerveDrivePoseEstimator poseEstimator;
    private final SparkMax turretMotor = new SparkMax(TurretConstants.TURRET_MOTOR_ID, MotorType.kBrushless);
    private final PIDController pidController = new PIDController(kP, kI, kD);
    public boolean absoluteMode = true; // If true, turret will automatically compensate for robot yaw

    public TurretSubsystem(Robot robot) {
        SparkMaxConfig config = new SparkMaxConfig();
        config.idleMode(IdleMode.kBrake);
        config.smartCurrentLimit(TurretConstants.CURRENT_LIMIT);

        turretMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        turretMotor.set(MAX_SPEED);

        pidController.setTolerance(TOLERANCE);
        pidController.reset();

        pidController.setSetpoint(0); // hold at 0 for fixed turret

        // imu = robot.imu;
        poseEstimator = robot.drivebase.swerveDrive.swerveDrivePoseEstimator;
    }

    public void set(Angle position) {
        // TODO: angle wrapping

        // pidController.setSetpoint(position.in(Units.Degrees));
    }

    @Override
    public void periodic() {
        double turretPosition = turretMotor.getEncoder().getPosition() * GEAR_RATIO * 360;

        // Fixed turret
        // if (absoluteMode) {
        //     // double robotYaw = imu.getYaw().getValueAsDouble();
        //     double robotYaw = poseEstimator.getEstimatedPosition().getRotation().getDegrees();
        //     System.out.println("Robot Yaw" + robotYaw);
        //     turretPosition -= robotYaw;
        // }
        double output = pidController.calculate(turretPosition);

        output = MathUtil.clamp(output * 0.2, -MAX_SPEED, MAX_SPEED);
        turretMotor.set(output);
    }
}
