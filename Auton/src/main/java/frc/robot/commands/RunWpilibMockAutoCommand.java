package frc.robot.commands;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.sim.WpilibMockScenario;
import frc.robot.sim.WpilibMockScenario.ObstacleRect;
import frc.robot.sim.WpilibMockScenario.TagPose;
import frc.robot.subsystems.DriveSubsystem;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.Set;

public class RunWpilibMockAutoCommand extends Command {
  private static final double XY_KP = 1.8;
  private static final double OMEGA_KP = 2.0;
  private static final double GRID_CELL_METERS = 0.20;
  private static final double ROBOT_CLEARANCE_METERS = 0.45;

  private final DriveSubsystem driveSubsystem;
  private final String deployLayoutPath;

  private List<Pose2d> waypoints = List.of();
  private int waypointIndex = 0;
  private Pose2d targetTagPose = new Pose2d();
  private double maxLinearSpeedMetersPerSec = 1.5;
  private boolean finished = false;

  public RunWpilibMockAutoCommand(DriveSubsystem driveSubsystem, String deployLayoutPath) {
    this.driveSubsystem = driveSubsystem;
    this.deployLayoutPath = deployLayoutPath;
    addRequirements(driveSubsystem);
  }

  @Override
  public void initialize() {
    finished = false;
    waypoints = List.of();
    waypointIndex = 0;

    Optional<WpilibMockScenario> maybeScenario = WpilibMockScenario.fromDeployFile(deployLayoutPath);
    if (maybeScenario.isEmpty()) {
      DriverStation.reportWarning("WPILib mock layout missing: " + deployLayoutPath, false);
      finished = true;
      return;
    }

    WpilibMockScenario scenario = maybeScenario.get();
    if (scenario.tags().isEmpty()) {
      DriverStation.reportWarning("WPILib mock layout has no tags", false);
      finished = true;
      return;
    }

    Optional<TagPose> maybeTarget = chooseTargetTag(scenario, driveSubsystem.getPose());
    if (maybeTarget.isEmpty()) {
      DriverStation.reportWarning("WPILib mock could not choose target tag", false);
      finished = true;
      return;
    }

    targetTagPose = maybeTarget.get().pose();
    driveSubsystem.resetPose(scenario.robotPose());
    maxLinearSpeedMetersPerSec = Math.max(0.2, scenario.speedMetersPerSecond());

    Optional<List<Pose2d>> maybePath = buildObstacleAwarePath(scenario, scenario.robotPose(), targetTagPose);
    if (maybePath.isEmpty()) {
      DriverStation.reportWarning("WPILib mock planner found no collision-free path", false);
      finished = true;
      return;
    }

    waypoints = maybePath.get();
    waypointIndex = Math.min(1, waypoints.size() - 1);
  }

  @Override
  public void execute() {
    if (finished) {
      return;
    }

    Pose2d currentPose = driveSubsystem.getPose();
    Pose2d waypoint = waypoints.get(waypointIndex);

    Translation2d error = waypoint.getTranslation().minus(currentPose.getTranslation());
    double distanceMeters = error.getNorm();

    if (distanceMeters < Constants.Mock.POSITION_TOLERANCE_METERS) {
      if (waypointIndex >= waypoints.size() - 1) {
        Translation2d targetError = targetTagPose.getTranslation().minus(currentPose.getTranslation());
        double headingError =
            Math.abs(targetTagPose.getRotation().minus(currentPose.getRotation()).getDegrees());
        if (targetError.getNorm() < Constants.Mock.POSITION_TOLERANCE_METERS
            && headingError < Constants.Mock.HEADING_TOLERANCE_DEG) {
          finished = true;
          driveSubsystem.driveFieldRelative(0.0, 0.0, 0.0);
          return;
        }
      } else {
        waypointIndex += 1;
        return;
      }
    }

    double vx = MathUtil.clamp(error.getX() * XY_KP, -maxLinearSpeedMetersPerSec, maxLinearSpeedMetersPerSec);
    double vy = MathUtil.clamp(error.getY() * XY_KP, -maxLinearSpeedMetersPerSec, maxLinearSpeedMetersPerSec);

    Rotation2d desiredHeading =
        waypointIndex >= waypoints.size() - 1
            ? targetTagPose.getRotation()
            : new Rotation2d(Math.atan2(error.getY(), error.getX()));

    double omega =
        MathUtil.clamp(
            desiredHeading.minus(currentPose.getRotation()).getRadians() * OMEGA_KP,
            -Constants.Drive.MAX_ANGULAR_SPEED_RAD_PER_SEC,
            Constants.Drive.MAX_ANGULAR_SPEED_RAD_PER_SEC);

    driveSubsystem.driveFieldRelative(vx, vy, omega);
  }

  @Override
  public void end(boolean interrupted) {
    driveSubsystem.driveFieldRelative(0.0, 0.0, 0.0);
  }

  @Override
  public boolean isFinished() {
    return finished;
  }

  private Optional<TagPose> chooseTargetTag(WpilibMockScenario scenario, Pose2d currentPose) {
    OptionalInt configuredId = scenario.targetTagId();
    if (configuredId.isPresent()) {
      for (TagPose tag : scenario.tags()) {
        if (tag.id() == configuredId.getAsInt()) {
          return Optional.of(tag);
        }
      }
    }

    TagPose nearest = scenario.tags().get(0);
    double bestDistance =
        nearest.pose().getTranslation().getDistance(currentPose.getTranslation());
    for (TagPose tag : scenario.tags()) {
      double candidateDistance = tag.pose().getTranslation().getDistance(currentPose.getTranslation());
      if (candidateDistance < bestDistance) {
        nearest = tag;
        bestDistance = candidateDistance;
      }
    }

    return Optional.of(nearest);
  }

  private Optional<List<Pose2d>> buildObstacleAwarePath(
      WpilibMockScenario scenario, Pose2d startPose, Pose2d goalPose) {
    Grid grid = Grid.fromScenario(scenario, GRID_CELL_METERS, ROBOT_CLEARANCE_METERS);

    Cell startCell = grid.pointToCell(startPose.getX(), startPose.getY());
    Cell goalCell = grid.pointToCell(goalPose.getX(), goalPose.getY());

    startCell = nearestFreeCell(startCell, grid);
    goalCell = nearestFreeCell(goalCell, grid);

    if (startCell == null || goalCell == null) {
      return Optional.empty();
    }

    if (grid.segmentClear(startPose.getTranslation(), goalPose.getTranslation())) {
      return Optional.of(List.of(startPose, goalPose));
    }

    List<Cell> cellPath = runAStar(startCell, goalCell, grid);
    if (cellPath.isEmpty()) {
      return Optional.empty();
    }

    List<Pose2d> rawPath = new ArrayList<>();
    rawPath.add(startPose);
    for (Cell cell : cellPath) {
      Translation2d point = grid.cellToPoint(cell);
      rawPath.add(new Pose2d(point, goalPose.getRotation()));
    }
    rawPath.add(goalPose);

    return Optional.of(smoothPath(rawPath, grid));
  }

  private Cell nearestFreeCell(Cell start, Grid grid) {
    if (!grid.isBlocked(start)) {
      return start;
    }

    Queue<Cell> queue = new ArrayDeque<>();
    Set<Cell> visited = new HashSet<>();
    queue.add(start);
    visited.add(start);

    while (!queue.isEmpty()) {
      Cell current = queue.remove();
      for (int rowDelta = -1; rowDelta <= 1; rowDelta++) {
        for (int colDelta = -1; colDelta <= 1; colDelta++) {
          if (rowDelta == 0 && colDelta == 0) {
            continue;
          }
          Cell next = new Cell(current.row + rowDelta, current.col + colDelta);
          if (!grid.inBounds(next) || visited.contains(next)) {
            continue;
          }

          if (!grid.isBlocked(next)) {
            return next;
          }

          visited.add(next);
          queue.add(next);
        }
      }
    }

    return null;
  }

  private List<Cell> runAStar(Cell start, Cell goal, Grid grid) {
    List<Cell> open = new ArrayList<>();
    Set<Cell> inOpen = new HashSet<>();
    Map<Cell, Cell> cameFrom = new HashMap<>();
    Map<Cell, Double> gScore = new HashMap<>();
    Map<Cell, Double> fScore = new HashMap<>();

    open.add(start);
    inOpen.add(start);
    gScore.put(start, 0.0);
    fScore.put(start, heuristic(start, goal));

    while (!open.isEmpty()) {
      int bestIndex = 0;
      double bestF = Double.POSITIVE_INFINITY;
      for (int i = 0; i < open.size(); i++) {
        Cell candidate = open.get(i);
        double value = fScore.getOrDefault(candidate, Double.POSITIVE_INFINITY);
        if (value < bestF) {
          bestF = value;
          bestIndex = i;
        }
      }

      Cell current = open.remove(bestIndex);
      inOpen.remove(current);

      if (current.equals(goal)) {
        return reconstructPath(cameFrom, current);
      }

      for (int rowDelta = -1; rowDelta <= 1; rowDelta++) {
        for (int colDelta = -1; colDelta <= 1; colDelta++) {
          if (rowDelta == 0 && colDelta == 0) {
            continue;
          }

          Cell neighbor = new Cell(current.row + rowDelta, current.col + colDelta);
          if (!grid.inBounds(neighbor) || grid.isBlocked(neighbor)) {
            continue;
          }

          if (rowDelta != 0 && colDelta != 0) {
            Cell sideA = new Cell(current.row + rowDelta, current.col);
            Cell sideB = new Cell(current.row, current.col + colDelta);
            if (grid.isBlocked(sideA) || grid.isBlocked(sideB)) {
              continue;
            }
          }

          double stepCost = rowDelta != 0 && colDelta != 0 ? Math.sqrt(2.0) : 1.0;
          double tentative = gScore.getOrDefault(current, Double.POSITIVE_INFINITY) + stepCost;

          if (tentative >= gScore.getOrDefault(neighbor, Double.POSITIVE_INFINITY)) {
            continue;
          }

          cameFrom.put(neighbor, current);
          gScore.put(neighbor, tentative);
          fScore.put(neighbor, tentative + heuristic(neighbor, goal));

          if (!inOpen.contains(neighbor)) {
            open.add(neighbor);
            inOpen.add(neighbor);
          }
        }
      }
    }

    return List.of();
  }

  private List<Cell> reconstructPath(Map<Cell, Cell> cameFrom, Cell end) {
    List<Cell> path = new ArrayList<>();
    Cell current = end;
    path.add(current);
    while (cameFrom.containsKey(current)) {
      current = cameFrom.get(current);
      path.add(0, current);
    }
    return path;
  }

  private double heuristic(Cell a, Cell b) {
    int dx = Math.abs(a.col - b.col);
    int dy = Math.abs(a.row - b.row);
    int diagonal = Math.min(dx, dy);
    int straight = Math.max(dx, dy) - diagonal;
    return diagonal * Math.sqrt(2.0) + straight;
  }

  private List<Pose2d> smoothPath(List<Pose2d> path, Grid grid) {
    if (path.size() <= 2) {
      return path;
    }

    List<Pose2d> simplified = new ArrayList<>();
    simplified.add(path.get(0));

    int anchor = 0;
    while (anchor < path.size() - 1) {
      int next = anchor + 1;
      for (int candidate = path.size() - 1; candidate > anchor + 1; candidate--) {
        if (grid.segmentClear(path.get(anchor).getTranslation(), path.get(candidate).getTranslation())) {
          next = candidate;
          break;
        }
      }
      simplified.add(path.get(next));
      anchor = next;
    }

    return simplified;
  }

  private record Cell(int row, int col) {}

  private static class Grid {
    private final double widthMeters;
    private final double heightMeters;
    private final double cellMeters;
    private final int rows;
    private final int cols;
    private final boolean[][] blocked;
    private final double inflateMeters;
    private final List<ObstacleRect> obstacles;

    private Grid(
        double widthMeters,
        double heightMeters,
        double cellMeters,
        int rows,
        int cols,
        boolean[][] blocked,
        double inflateMeters,
        List<ObstacleRect> obstacles) {
      this.widthMeters = widthMeters;
      this.heightMeters = heightMeters;
      this.cellMeters = cellMeters;
      this.rows = rows;
      this.cols = cols;
      this.blocked = blocked;
      this.inflateMeters = inflateMeters;
      this.obstacles = obstacles;
    }

    static Grid fromScenario(WpilibMockScenario scenario, double cellMeters, double inflateMeters) {
      int rows = Math.max(1, (int) Math.ceil(scenario.fieldHeightMeters() / cellMeters));
      int cols = Math.max(1, (int) Math.ceil(scenario.fieldWidthMeters() / cellMeters));
      boolean[][] blocked = new boolean[rows][cols];

      Grid grid =
          new Grid(
              scenario.fieldWidthMeters(),
              scenario.fieldHeightMeters(),
              cellMeters,
              rows,
              cols,
              blocked,
              inflateMeters,
              scenario.obstacles());

      for (int row = 0; row < rows; row++) {
        for (int col = 0; col < cols; col++) {
          Translation2d point = grid.cellToPoint(new Cell(row, col));
          blocked[row][col] = grid.pointInsideAnyObstacle(point);
        }
      }

      return grid;
    }

    Cell pointToCell(double x, double y) {
      int col = (int) Math.floor(MathUtil.clamp(x, 0.0, widthMeters - 1e-6) / cellMeters);
      int row = (int) Math.floor(MathUtil.clamp(y, 0.0, heightMeters - 1e-6) / cellMeters);
      int clampedRow = Math.max(0, Math.min(rows - 1, row));
      int clampedCol = Math.max(0, Math.min(cols - 1, col));
      return new Cell(clampedRow, clampedCol);
    }

    Translation2d cellToPoint(Cell cell) {
      double x = Math.min(widthMeters, cell.col * cellMeters + cellMeters / 2.0);
      double y = Math.min(heightMeters, cell.row * cellMeters + cellMeters / 2.0);
      return new Translation2d(x, y);
    }

    boolean inBounds(Cell cell) {
      return cell.row >= 0 && cell.row < rows && cell.col >= 0 && cell.col < cols;
    }

    boolean isBlocked(Cell cell) {
      if (!inBounds(cell)) {
        return true;
      }
      return blocked[cell.row][cell.col];
    }

    boolean segmentClear(Translation2d start, Translation2d end) {
      int samples = Math.max(2, (int) Math.ceil(start.getDistance(end) / (cellMeters * 0.5)));
      for (int index = 0; index <= samples; index++) {
        double t = index / (double) samples;
        double x = start.getX() + (end.getX() - start.getX()) * t;
        double y = start.getY() + (end.getY() - start.getY()) * t;
        if (pointInsideAnyObstacle(new Translation2d(x, y))) {
          return false;
        }
      }
      return true;
    }

    private boolean pointInsideAnyObstacle(Translation2d point) {
      for (ObstacleRect obstacle : obstacles) {
        if (pointInsideObstacle(point, obstacle, inflateMeters)) {
          return true;
        }
      }
      return false;
    }

    private boolean pointInsideObstacle(Translation2d point, ObstacleRect obstacle, double inflateMeters) {
      double minX = obstacle.x() - obstacle.width() / 2.0 - inflateMeters;
      double maxX = obstacle.x() + obstacle.width() / 2.0 + inflateMeters;
      double minY = obstacle.y() - obstacle.height() / 2.0 - inflateMeters;
      double maxY = obstacle.y() + obstacle.height() / 2.0 + inflateMeters;

      return point.getX() >= minX
          && point.getX() <= maxX
          && point.getY() >= minY
          && point.getY() <= maxY;
    }
  }
}
