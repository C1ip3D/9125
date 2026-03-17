package frc.robot.sim;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record WpilibMockScenario(
    double fieldWidthMeters,
    double fieldHeightMeters,
    Pose2d robotPose,
    List<TagPose> tags,
    List<ObstacleRect> obstacles,
    OptionalInt targetTagId,
    double speedMetersPerSecond) {

  public record TagPose(int id, Pose2d pose) {}

  public record ObstacleRect(int id, double x, double y, double width, double height) {}

  public static Optional<WpilibMockScenario> fromDeployFile(String deployRelativePath) {
    Path path = Filesystem.getDeployDirectory().toPath().resolve(deployRelativePath);
    if (!Files.exists(path)) {
      return Optional.empty();
    }

    try {
      String json = Files.readString(path, StandardCharsets.UTF_8);
      return Optional.of(parse(json));
    } catch (IOException ex) {
      return Optional.empty();
    }
  }

  private static WpilibMockScenario parse(String json) {
    double fieldWidth = extractDouble(json, "width").orElse(16.54);
    double fieldHeight = extractDouble(json, "height").orElse(8.21);

    Optional<Pose2d> robotPose = extractRobotPose(json);
    List<TagPose> tags = extractTags(json);
    List<ObstacleRect> obstacles = extractObstacles(json);

    OptionalInt targetTagId = extractInt(json, "targetTagId");
    double speed = extractDouble(json, "speedMetersPerSec").orElse(1.6);

    Pose2d startPose = robotPose.orElseGet(() -> tags.isEmpty() ? new Pose2d() : tags.get(0).pose());

    return new WpilibMockScenario(
        fieldWidth,
        fieldHeight,
        startPose,
        tags,
        obstacles,
        targetTagId,
        Math.max(0.2, speed));
  }

  private static Optional<Pose2d> extractRobotPose(String json) {
    Pattern robotPattern = Pattern.compile("\\\"robot\\\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
    Matcher matcher = robotPattern.matcher(json);
    if (!matcher.find()) {
      return Optional.empty();
    }

    String block = matcher.group(1);
    double x = extractDouble(block, "x").orElse(0.0);
    double y = extractDouble(block, "y").orElse(0.0);
    double rotationDeg = extractDouble(block, "rotationDeg").orElse(0.0);
    return Optional.of(new Pose2d(x, y, Rotation2d.fromDegrees(rotationDeg)));
  }

  private static List<TagPose> extractTags(String json) {
    return extractArray(json, "tags").stream()
        .map(WpilibMockScenario::parseTag)
        .flatMap(Optional::stream)
        .toList();
  }

  private static Optional<TagPose> parseTag(String objectBlock) {
    OptionalInt id = extractInt(objectBlock, "id");
    OptionalDouble x = extractDouble(objectBlock, "x");
    OptionalDouble y = extractDouble(objectBlock, "y");
    double rotationDeg = extractDouble(objectBlock, "rotationDeg").orElse(0.0);

    if (id.isEmpty() || x.isEmpty() || y.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(new TagPose(id.getAsInt(), new Pose2d(x.getAsDouble(), y.getAsDouble(), Rotation2d.fromDegrees(rotationDeg))));
  }

  private static List<ObstacleRect> extractObstacles(String json) {
    return extractArray(json, "obstacles").stream()
        .map(WpilibMockScenario::parseObstacle)
        .flatMap(Optional::stream)
        .toList();
  }

  private static Optional<ObstacleRect> parseObstacle(String objectBlock) {
    OptionalInt id = extractInt(objectBlock, "id");
    OptionalDouble x = extractDouble(objectBlock, "x");
    OptionalDouble y = extractDouble(objectBlock, "y");
    OptionalDouble width = extractDouble(objectBlock, "width");
    OptionalDouble height = extractDouble(objectBlock, "height");

    if (id.isEmpty() || x.isEmpty() || y.isEmpty() || width.isEmpty() || height.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(
        new ObstacleRect(
            id.getAsInt(),
            x.getAsDouble(),
            y.getAsDouble(),
            Math.max(0.05, width.getAsDouble()),
            Math.max(0.05, height.getAsDouble())));
  }

  private static List<String> extractArray(String json, String key) {
    Pattern arrayPattern =
        Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL);
    Matcher arrayMatcher = arrayPattern.matcher(json);
    if (!arrayMatcher.find()) {
      return List.of();
    }

    String body = arrayMatcher.group(1);
    Pattern objectPattern = Pattern.compile("\\{(.*?)\\}", Pattern.DOTALL);
    Matcher objectMatcher = objectPattern.matcher(body);

    List<String> objects = new ArrayList<>();
    while (objectMatcher.find()) {
      objects.add(objectMatcher.group(1));
    }
    return objects;
  }

  private static OptionalDouble extractDouble(String block, String key) {
    Pattern pattern =
        Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(-?\\d+(?:\\.\\d+)?)", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(block);
    if (!matcher.find()) {
      return OptionalDouble.empty();
    }

    try {
      return OptionalDouble.of(Double.parseDouble(matcher.group(1)));
    } catch (NumberFormatException ex) {
      return OptionalDouble.empty();
    }
  }

  private static OptionalInt extractInt(String block, String key) {
    Pattern pattern = Pattern.compile("\\\"" + Pattern.quote(key) + "\\\"\\s*:\\s*(\\d+)", Pattern.DOTALL);
    Matcher matcher = pattern.matcher(block);
    if (!matcher.find()) {
      return OptionalInt.empty();
    }

    try {
      return OptionalInt.of(Integer.parseInt(matcher.group(1)));
    } catch (NumberFormatException ex) {
      return OptionalInt.empty();
    }
  }
}
