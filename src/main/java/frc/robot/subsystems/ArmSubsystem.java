package frc.robot.subsystems;

import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.mechanism.LoggedMechanism2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismLigament2d;
import org.littletonrobotics.junction.mechanism.LoggedMechanismRoot2d;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.Constants.TransportConstants;

public class ArmSubsystem extends SubsystemBase {
    private static final double kP = 0.02;
    private static final double kI = 0.0;
    private static final double kD = 0.0;

    private static final double MAX_SPEED = 0.2;
    private static final double TOLERANCE = 5;

    private final SparkMax armMotor1 = new SparkMax(TransportConstants.ARM_MOTOR_1_ID, MotorType.kBrushless);
    private final SparkMax armMotor2 = new SparkMax(TransportConstants.ARM_MOTOR_2_ID, MotorType.kBrushless);
    private final PIDController pidController1 = new PIDController(kP, kI, kD);
    private final PIDController pidController2 = new PIDController(kP, kI, kD);


    public boolean descended = false;

    LoggedMechanism2d armMechanism = new LoggedMechanism2d(2, 2);
    LoggedMechanismRoot2d root;
    LoggedMechanismLigament2d pivot;


    public ArmSubsystem() {
        SparkMaxConfig config = new SparkMaxConfig();
        config.idleMode(IdleMode.kBrake);
        // config.smartCurrentLimit(10);

        armMotor1.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        armMotor1.set(MAX_SPEED);


        armMotor2.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
        armMotor2.set(MAX_SPEED);

        pidController1.setTolerance(TOLERANCE);
        pidController1.reset();

        pidController2.setTolerance(TOLERANCE);
        pidController2.reset();

        pidController1.setSetpoint(0);
        pidController2.setSetpoint(0);

        root = armMechanism.getRoot("arm", 2, 0);
        pivot = root.append(new LoggedMechanismLigament2d("elevator", 0.5, 90));
    }

    public void setDescend(boolean descended) {
        this.descended = descended;

        if (this.descended) {
            pivot.setAngle(TransportConstants.DESCEND_POINT * 90);
            pidController1.setSetpoint(TransportConstants.DESCEND_POINT);
            pidController2.setSetpoint(TransportConstants.DESCEND_POINT);
        } else {
            pivot.setAngle(TransportConstants.LIFT_POINT * 90);
            pidController1.setSetpoint(TransportConstants.LIFT_POINT);
            pidController2.setSetpoint(TransportConstants.LIFT_POINT);
        }


    }

    public void setPosition(double pos) {
        // TODO: change if decended is higher than lift
        double interpolated = pos * (TransportConstants.LIFT_POINT - TransportConstants.DESCEND_POINT)
                + TransportConstants.DESCEND_POINT;
        pidController1.setSetpoint(interpolated);
        pidController2.setSetpoint(interpolated);
        
        System.out.println("Arm position " + pos);
        pivot.setAngle(pos * 90);
    }

    @Override
    public void periodic() {
        double armPosition1 = armMotor1.getEncoder().getPosition();
        double output1 = pidController1.calculate(armPosition1);

        output1 = MathUtil.clamp(output1 * 0.2, -MAX_SPEED, MAX_SPEED);
        armMotor1.set(output1);


        double armPosition2 = armMotor2.getEncoder().getPosition();
        double output2 = pidController2.calculate(armPosition2);

        output2 = MathUtil.clamp(output2 * 0.2, -MAX_SPEED, MAX_SPEED);
        armMotor2.set(output2);

        Logger.recordOutput("Arm Lifted", !descended);
        Logger.recordOutput("Intake Arm", armMechanism);

    }
}
