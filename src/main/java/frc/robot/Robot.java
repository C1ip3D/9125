package frc.robot;

import org.littletonrobotics.junction.LoggedRobot;
import org.littletonrobotics.junction.Logger;
import org.littletonrobotics.junction.networktables.NT4Publisher;
import org.littletonrobotics.junction.wpilog.WPILOGWriter;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.units.Units;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import frc.robot.Constants.TurretConstants;

public class Robot extends LoggedRobot {
  private Command m_autonomousCommand;

  private RobotContainer m_robotContainer;

  @Override
  public void robotInit() {
    // Instantiate our RobotContainer. This will perform all our button bindings,
    // and put our
    // autonomous chooser on the dashboard.
    m_robotContainer = new RobotContainer();

    // AdvantageKit
    Logger.addDataReceiver(new NT4Publisher()); // Publish data to NetworkTables
    if (isReal()) {
      Logger.addDataReceiver(new WPILOGWriter()); // Log to a USB stick ("/U/logs")
    } else {
      // simulator
    }
    Logger.start();
  }

  /**
   * This function is called every 20 ms, no matter the mode. Use this for items
   * like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();

    // Limelight Localization
    LimelightHelpers.SetRobotOrientation(TurretConstants.LIMELIGHT_NAME,
        m_robotContainer.drivebase.swerveDrive.getOdometryHeading().getDegrees(), 0, 0,
        0, 0, 0);
    LimelightHelpers.PoseEstimate mt2 = LimelightHelpers
        .getBotPoseEstimate_wpiBlue_MegaTag2(TurretConstants.LIMELIGHT_NAME);

    boolean doRejectUpdate = false;
    if (m_robotContainer.drivebase.swerveDrive.getGyro().getYawAngularVelocity().abs(Units.DegreesPerSecond) > 360) {
      doRejectUpdate = true;
    }
    if (mt2.tagCount == 0) {
      doRejectUpdate = true;
    }
    if (!doRejectUpdate) {
      m_robotContainer.drivebase.swerveDrive.setVisionMeasurementStdDevs(VecBuilder.fill(.7, .7, 9999999));
      m_robotContainer.drivebase.swerveDrive.addVisionMeasurement(
          mt2.pose,
          mt2.timestampSeconds);
    }

    m_robotContainer.logging.updateLogging();
  }

  @Override
  public void disabledInit() {
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void autonomousInit() {
    m_autonomousCommand = m_robotContainer.getAutonomousCommand();

    if (m_autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(m_autonomousCommand);
    }
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
    if (m_autonomousCommand != null) {
      m_autonomousCommand.cancel();
    }
  }

  @Override
  public void teleopPeriodic() {
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void testPeriodic() {
  }
}