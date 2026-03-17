package frc.robot;

public final class Constants {
    public static final class FieldConstants {
        public static final double BLUE_HUB_X = 4.63;
        public static final double RED_HUB_X = 16.54 - BLUE_HUB_X;
        public static final double HUB_Y = 4.035;

    }
    
    public static final class TransportConstants {
        public static final int INTAKE_MOTOR_ID = 20;
        public static final int ROLLER_MOTOR_ID = 19;
        public static final int TRANSFER_MOTOR_ID = 18;

        public static final double INTAKE_POWER = 0.5;
        public static final double ROLLER_INTAKE_POWER = 0.5;
        public static final double ROLLER_SHOOT_POWER = 0.5;
        public static final double TRANSFER_POWER = 0.5;
    }

    public static final class DriveConstants {
        public static final double MAX_SPEED = 3.048;
    }

    public static final class ShooterConstants {
        public static final int SHOOTER_MOTOR_1_ID = 21;
        public static final int SHOOTER_MOTOR_2_ID = 22;
    }

    public static final class TurretConstants {
        public static final int TURRET_MOTOR_ID = 23;
        public static final int CURRENT_LIMIT = 20; // amps — safe for a small turret NEO
        public static final String LIMELIGHT_NAME = "limelight";
    }

    public static final class OperatorConstants {
        public static final int DRIVER_CONTROLLER_PORT = 0;
        public static final int OPERATOR_CONTROLLER_PORT = 1;
        public static final double LEFT_X_DEADBAND = 0.1;
        public static final double LEFT_Y_DEADBAND = 0.1;
        public static final double RIGHT_X_DEADBAND = 0.1;
        public static final double TURN_CONSTANT = 6.0;
    }
}
