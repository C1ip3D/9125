package frc.robot;

/**
 * Wrapper around LimelightHelpers for clean access to vision data.
 * Uses the Limelight's NetworkTables API to read AprilTag tracking values.
 */
public class LimelightVision {

    private final String limelightName;

    public LimelightVision(String limelightName) {
        this.limelightName = limelightName;
    }

    /** Returns true if the Limelight sees at least one valid target. */
    public boolean hasTarget() {
        return LimelightHelpers.getTV(limelightName);
    }

    /** Returns the horizontal offset from crosshair to target in degrees. Positive = right, negative = left. */
    public double getTX() {
        return LimelightHelpers.getTX(limelightName);
    }

    /** Returns the vertical offset from crosshair to target in degrees. */
    public double getTY() {
        return LimelightHelpers.getTY(limelightName);
    }

    /** Returns the target area as a percentage of the image (0-100%). */
    public double getTA() {
        return LimelightHelpers.getTA(limelightName);
    }
}
