package frc.robot;

import edu.wpi.first.math.Matrix;
import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.numbers.N1;
import edu.wpi.first.math.numbers.N3;

public final class Constants {
  private Constants() {}

  public static final class Drive {
    public static final double MAX_LINEAR_SPEED_MPS = 4.0;
    public static final double MAX_ANGULAR_SPEED_RAD_PER_SEC = Math.PI;

    public static final Translation2d FRONT_LEFT_LOCATION = new Translation2d(0.35, 0.35);
    public static final Translation2d FRONT_RIGHT_LOCATION = new Translation2d(0.35, -0.35);
    public static final Translation2d REAR_LEFT_LOCATION = new Translation2d(-0.35, 0.35);
    public static final Translation2d REAR_RIGHT_LOCATION = new Translation2d(-0.35, -0.35);
  }

  public static final class Vision {
    public static final String LIMELIGHT_TABLE = "limelight";

    public static final Transform3d CAMERA_TO_ROBOT =
        new Transform3d(0.20, 0.0, 0.45, new Rotation3d(0.0, 0.0, 0.0));

    public static final Matrix<N3, N1> STATE_STD_DEVS = VecBuilder.fill(0.05, 0.05, 0.03);
    public static final Matrix<N3, N1> VISION_STD_DEVS = VecBuilder.fill(0.7, 0.7, 0.9);
  }

  public static final class Mock {
    public static final boolean ENABLE_WPILIB_MOCK_AUTONOMOUS = false;
    public static final String DEPLOY_LAYOUT_RELATIVE_PATH = "layouts/ui-current-layout.json";
    public static final double POSITION_TOLERANCE_METERS = 0.12;
    public static final double HEADING_TOLERANCE_DEG = 8.0;
  }
}
