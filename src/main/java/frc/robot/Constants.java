package frc.robot;

public final class Constants {

    public static final class DriveConstants {
        public static final double MAX_SPEED = 3.048;
    }

    public static final class TurretConstants {
        public static final int TURRET_MOTOR_ID = 20;
        public static final int CURRENT_LIMIT = 20; // amps — safe for a small turret NEO
        public static final String LIMELIGHT_NAME = "limelight";
    }

    public static final class OperatorConstants {
        public static final int DRIVER_CONTROLLER_PORT = 0;
        public static final double LEFT_X_DEADBAND = 0.1;
        public static final double LEFT_Y_DEADBAND = 0.1;
        public static final double RIGHT_X_DEADBAND = 0.1;
        public static final double TURN_CONSTANT = 6.0;
    }
}
