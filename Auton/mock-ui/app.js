const canvas = document.getElementById("fieldCanvas");
const ctx = canvas.getContext("2d");
const statusEl = document.getElementById("status");

const modeButtons = {
  select: document.getElementById("modeSelect"),
  tag: document.getElementById("modeTag"),
  obstacle: document.getElementById("modeObstacle"),
  robot: document.getElementById("modeRobot"),
};

const runMockAutoBtn = document.getElementById("runMockAuto");
const stopMockAutoBtn = document.getElementById("stopMockAuto");
const deleteSelectedBtn = document.getElementById("deleteSelected");
const clearAllBtn = document.getElementById("clearAll");
const tabSimulationBtn = document.getElementById("tabSimulation");
const tabDeploymentBtn = document.getElementById("tabDeployment");
const simulationCanvasEl = document.getElementById("simulationCanvas");
const simulationTabPanelEl = document.getElementById("simulationTabPanel");
const deploymentTabPanelEl = document.getElementById("deploymentTabPanel");
const exportJsonBtn = document.getElementById("exportJson");
const importJsonBtn = document.getElementById("importJson");

const emptySelectionEl = document.getElementById("emptySelection");
const editorEl = document.getElementById("editor");
const obstacleFieldsEl = document.getElementById("obstacleFields");
const objType = document.getElementById("objType");
const objId = document.getElementById("objId");
const objX = document.getElementById("objX");
const objY = document.getElementById("objY");
const objRot = document.getElementById("objRot");
const objW = document.getElementById("objW");
const objH = document.getElementById("objH");
const applyEditsBtn = document.getElementById("applyEdits");
const layoutJson = document.getElementById("layoutJson");
const fieldWidthMetersInput = document.getElementById("fieldWidthMeters");
const fieldHeightMetersInput = document.getElementById("fieldHeightMeters");
const exportOriginSelect = document.getElementById("exportOrigin");
const jsonModeSelect = document.getElementById("jsonMode");
const targetTagIdInput = document.getElementById("targetTagId");
const robotSpeedInput = document.getElementById("robotSpeed");
const runEngineSelect = document.getElementById("runEngine");
const teamNumberInput = document.getElementById("teamNumber");
const deployTargetSelect = document.getElementById("deployTarget");
const deployConnectionSelect = document.getElementById("deployConnection");
const driverJoystickTypeSelect = document.getElementById("driverJoystickType");
const driverJoystickPortInput = document.getElementById("driverJoystickPort");
const driverJoystickAttachedInput = document.getElementById("driverJoystickAttached");
const operatorJoystickTypeSelect = document.getElementById("operatorJoystickType");
const operatorJoystickPortInput = document.getElementById("operatorJoystickPort");
const operatorJoystickAttachedInput = document.getElementById("operatorJoystickAttached");
const exportDeployConfigBtn = document.getElementById("exportDeployConfig");

const state = {
  mode: "select",
  tags: [],
  obstacles: [],
  robot: null,
  nextTagId: 1,
  nextObstacleId: 1,
  selected: null,
  dragging: false,
  dragOffsetX: 0,
  dragOffsetY: 0,
  simulation: {
    running: false,
    pathPoints: [],
    segmentIndex: 0,
    targetTagId: null,
    speedPxPerSec: 180,
    lastTimestampMs: 0,
    frameHandle: null,
  },
  activeTab: "simulation",
};

function setActiveTab(tab) {
  const isSimulation = tab === "simulation";
  state.activeTab = isSimulation ? "simulation" : "deployment";

  tabSimulationBtn.classList.toggle("active", isSimulation);
  tabDeploymentBtn.classList.toggle("active", !isSimulation);

  simulationCanvasEl.classList.toggle("hidden", !isSimulation);
  simulationTabPanelEl.classList.toggle("hidden", !isSimulation);
  deploymentTabPanelEl.classList.toggle("hidden", isSimulation);

  modeButtons.select.classList.toggle("hidden", !isSimulation);
  modeButtons.tag.classList.toggle("hidden", !isSimulation);
  modeButtons.obstacle.classList.toggle("hidden", !isSimulation);
  modeButtons.robot.classList.toggle("hidden", !isSimulation);
  runMockAutoBtn.classList.toggle("hidden", !isSimulation);
  stopMockAutoBtn.classList.toggle("hidden", !isSimulation);
  deleteSelectedBtn.classList.toggle("hidden", !isSimulation);
  clearAllBtn.classList.toggle("hidden", !isSimulation);

  if (isSimulation) {
    setStatus(`Mode: ${modeLabel(state.mode)}`);
  } else {
    setStatus("Deployment settings and export tools");
  }
}

function modeLabel(mode) {
  if (mode === "tag") {
    return "Place AprilTag";
  }
  if (mode === "obstacle") {
    return "Place Obstacle";
  }
  if (mode === "robot") {
    return "Place Robot";
  }
  return "Select";
}

function setStatus(text) {
  statusEl.textContent = text;
}

function setMode(mode) {
  state.mode = mode;
  Object.entries(modeButtons).forEach(([key, button]) => {
    button.classList.toggle("active", key === mode);
  });
  setStatus(`Mode: ${modeLabel(mode)}`);
}

function toCanvasPoint(event) {
  const rect = canvas.getBoundingClientRect();
  const scaleX = canvas.width / rect.width;
  const scaleY = canvas.height / rect.height;
  return {
    x: (event.clientX - rect.left) * scaleX,
    y: (event.clientY - rect.top) * scaleY,
  };
}

function clampPosition(x, y) {
  return {
    x: Math.max(0, Math.min(canvas.width, x)),
    y: Math.max(0, Math.min(canvas.height, y)),
  };
}

function distance(a, b) {
  const dx = b.x - a.x;
  const dy = b.y - a.y;
  return Math.hypot(dx, dy);
}

function drawGrid() {
  ctx.fillStyle = "#ffffff";
  ctx.fillRect(0, 0, canvas.width, canvas.height);

  ctx.strokeStyle = "#e5e7eb";
  ctx.lineWidth = 1;
  for (let x = 0; x <= canvas.width; x += 50) {
    ctx.beginPath();
    ctx.moveTo(x, 0);
    ctx.lineTo(x, canvas.height);
    ctx.stroke();
  }
  for (let y = 0; y <= canvas.height; y += 50) {
    ctx.beginPath();
    ctx.moveTo(0, y);
    ctx.lineTo(canvas.width, y);
    ctx.stroke();
  }
}

function drawTag(tag, isSelected) {
  ctx.save();
  ctx.translate(tag.x, tag.y);
  ctx.rotate((tag.rotationDeg * Math.PI) / 180);

  ctx.fillStyle = isSelected ? "#16a34a" : "#22c55e";
  ctx.strokeStyle = "#166534";
  ctx.lineWidth = 2;
  ctx.fillRect(-18, -18, 36, 36);
  ctx.strokeRect(-18, -18, 36, 36);

  ctx.beginPath();
  ctx.moveTo(0, 0);
  ctx.lineTo(24, 0);
  ctx.strokeStyle = "#111827";
  ctx.lineWidth = 3;
  ctx.stroke();

  ctx.restore();

  ctx.fillStyle = "#111827";
  ctx.font = "12px sans-serif";
  ctx.fillText(`Tag ${tag.id}`, tag.x - 18, tag.y - 24);
}

function drawObstacle(obstacle, isSelected) {
  ctx.save();
  ctx.translate(obstacle.x, obstacle.y);
  ctx.rotate((obstacle.rotationDeg * Math.PI) / 180);

  ctx.fillStyle = isSelected ? "#dc2626" : "#ef4444";
  ctx.strokeStyle = "#7f1d1d";
  ctx.lineWidth = 2;
  ctx.fillRect(-obstacle.width / 2, -obstacle.height / 2, obstacle.width, obstacle.height);
  ctx.strokeRect(-obstacle.width / 2, -obstacle.height / 2, obstacle.width, obstacle.height);

  ctx.restore();

  ctx.fillStyle = "#111827";
  ctx.font = "12px sans-serif";
  ctx.fillText(`Obs ${obstacle.id}`, obstacle.x - 20, obstacle.y - obstacle.height / 2 - 8);
}

function drawRobot(isSelected) {
  if (!state.robot) {
    return;
  }

  const robot = state.robot;
  ctx.save();
  ctx.translate(robot.x, robot.y);
  ctx.rotate((robot.rotationDeg * Math.PI) / 180);

  ctx.fillStyle = isSelected ? "#1d4ed8" : "#3b82f6";
  ctx.strokeStyle = "#1e3a8a";
  ctx.lineWidth = 2;
  ctx.fillRect(-robot.width / 2, -robot.height / 2, robot.width, robot.height);
  ctx.strokeRect(-robot.width / 2, -robot.height / 2, robot.width, robot.height);

  ctx.beginPath();
  ctx.moveTo(robot.width / 2, 0);
  ctx.lineTo(robot.width / 2 + 20, 0);
  ctx.strokeStyle = "#111827";
  ctx.lineWidth = 3;
  ctx.stroke();

  ctx.restore();

  ctx.fillStyle = "#111827";
  ctx.font = "12px sans-serif";
  ctx.fillText("Robot", robot.x - 20, robot.y - robot.height / 2 - 10);
}

function drawPathOverlay() {
  const path = state.simulation.pathPoints;
  if (!path || path.length < 2) {
    return;
  }

  ctx.save();
  ctx.strokeStyle = "#0f766e";
  ctx.lineWidth = 2;
  ctx.setLineDash([8, 6]);
  ctx.beginPath();
  ctx.moveTo(path[0].x, path[0].y);
  for (let i = 1; i < path.length; i += 1) {
    ctx.lineTo(path[i].x, path[i].y);
  }
  ctx.stroke();
  ctx.setLineDash([]);

  const target = path[path.length - 1];
  ctx.fillStyle = "#0f766e";
  ctx.beginPath();
  ctx.arc(target.x, target.y, 6, 0, Math.PI * 2);
  ctx.fill();
  ctx.restore();
}

function render() {
  drawGrid();

  state.obstacles.forEach((obstacle) => {
    drawObstacle(obstacle, state.selected?.type === "obstacle" && state.selected.id === obstacle.id);
  });

  state.tags.forEach((tag) => {
    drawTag(tag, state.selected?.type === "tag" && state.selected.id === tag.id);
  });

  drawPathOverlay();
  drawRobot(state.selected?.type === "robot");
}

function hitTestTag(point, tag) {
  const dx = point.x - tag.x;
  const dy = point.y - tag.y;
  return Math.abs(dx) <= 20 && Math.abs(dy) <= 20;
}

function hitTestObstacle(point, obstacle) {
  const localX = point.x - obstacle.x;
  const localY = point.y - obstacle.y;
  return Math.abs(localX) <= obstacle.width / 2 && Math.abs(localY) <= obstacle.height / 2;
}

function hitTestRobot(point) {
  if (!state.robot) {
    return false;
  }
  const robot = state.robot;
  return (
    Math.abs(point.x - robot.x) <= robot.width / 2 + 4 &&
    Math.abs(point.y - robot.y) <= robot.height / 2 + 4
  );
}

function findObjectAtPoint(point) {
  if (hitTestRobot(point)) {
    return { type: "robot", id: 1 };
  }

  for (let i = state.tags.length - 1; i >= 0; i -= 1) {
    if (hitTestTag(point, state.tags[i])) {
      return { type: "tag", id: state.tags[i].id };
    }
  }

  for (let i = state.obstacles.length - 1; i >= 0; i -= 1) {
    if (hitTestObstacle(point, state.obstacles[i])) {
      return { type: "obstacle", id: state.obstacles[i].id };
    }
  }

  return null;
}

function getSelectedObject() {
  if (!state.selected) {
    return null;
  }

  if (state.selected.type === "robot") {
    return state.robot;
  }

  if (state.selected.type === "tag") {
    return state.tags.find((tag) => tag.id === state.selected.id) ?? null;
  }

  return state.obstacles.find((obs) => obs.id === state.selected.id) ?? null;
}

function updateEditor() {
  const selected = getSelectedObject();
  if (!selected) {
    emptySelectionEl.classList.remove("hidden");
    editorEl.classList.add("hidden");
    return;
  }

  emptySelectionEl.classList.add("hidden");
  editorEl.classList.remove("hidden");

  objType.value = state.selected.type;
  objId.value = state.selected.type === "robot" ? "robot" : String(selected.id);
  objX.value = String(Math.round(selected.x));
  objY.value = String(Math.round(selected.y));
  objRot.value = String(Math.round(selected.rotationDeg || 0));

  const isObstacle = state.selected.type === "obstacle";
  obstacleFieldsEl.classList.toggle("hidden", !isObstacle);
  if (isObstacle) {
    objW.value = String(Math.round(selected.width));
    objH.value = String(Math.round(selected.height));
  }
}

function addTag(point) {
  const pos = clampPosition(point.x, point.y);
  state.tags.push({
    id: state.nextTagId,
    x: pos.x,
    y: pos.y,
    rotationDeg: 0,
  });
  state.selected = { type: "tag", id: state.nextTagId };
  state.nextTagId += 1;
}

function addObstacle(point) {
  const pos = clampPosition(point.x, point.y);
  state.obstacles.push({
    id: state.nextObstacleId,
    x: pos.x,
    y: pos.y,
    width: 80,
    height: 60,
    rotationDeg: 0,
  });
  state.selected = { type: "obstacle", id: state.nextObstacleId };
  state.nextObstacleId += 1;
}

function placeRobot(point) {
  const pos = clampPosition(point.x, point.y);
  if (!state.robot) {
    state.robot = {
      x: pos.x,
      y: pos.y,
      width: 70,
      height: 55,
      rotationDeg: 0,
    };
  } else {
    state.robot.x = pos.x;
    state.robot.y = pos.y;
  }
  state.selected = { type: "robot", id: 1 };
}

function clearPathOverlay() {
  state.simulation.pathPoints = [];
  state.simulation.segmentIndex = 0;
  state.simulation.targetTagId = null;
}

function deleteSelected() {
  if (!state.selected) {
    return;
  }

  if (state.selected.type === "robot") {
    stopMockAuto();
    state.robot = null;
  } else if (state.selected.type === "tag") {
    state.tags = state.tags.filter((tag) => tag.id !== state.selected.id);
  } else {
    state.obstacles = state.obstacles.filter((obs) => obs.id !== state.selected.id);
  }

  state.selected = null;
  updateEditor();
  render();
}

function applyEdits() {
  const selected = getSelectedObject();
  if (!selected) {
    return;
  }

  selected.x = Number(objX.value);
  selected.y = Number(objY.value);
  selected.rotationDeg = Number(objRot.value) || 0;

  const clamped = clampPosition(selected.x, selected.y);
  selected.x = clamped.x;
  selected.y = clamped.y;

  if (state.selected.type === "obstacle") {
    selected.width = Math.max(10, Number(objW.value));
    selected.height = Math.max(10, Number(objH.value));
  }

  render();
  updateEditor();
}

function getFieldMeters() {
  const widthMeters = Math.max(0.1, Number(fieldWidthMetersInput.value) || 16.54);
  const heightMeters = Math.max(0.1, Number(fieldHeightMetersInput.value) || 8.21);
  return { widthMeters, heightMeters };
}

function pixelToMeters(pixelX, pixelY) {
  const { widthMeters, heightMeters } = getFieldMeters();
  const metersPerPixelX = widthMeters / canvas.width;
  const metersPerPixelY = heightMeters / canvas.height;
  const origin = exportOriginSelect.value;

  const xMetersBlue = pixelX * metersPerPixelX;
  const yMetersBlue = (canvas.height - pixelY) * metersPerPixelY;

  if (origin === "red-left") {
    return {
      xMeters: widthMeters - xMetersBlue,
      yMeters: yMetersBlue,
    };
  }

  return {
    xMeters: xMetersBlue,
    yMeters: yMetersBlue,
  };
}

function metersToPixel(xMeters, yMeters) {
  const { widthMeters, heightMeters } = getFieldMeters();
  const origin = exportOriginSelect.value;

  const xBlue = origin === "red-left" ? widthMeters - xMeters : xMeters;
  const yBlue = yMeters;

  const pixelX = (xBlue / widthMeters) * canvas.width;
  const pixelY = canvas.height - (yBlue / heightMeters) * canvas.height;
  return clampPosition(pixelX, pixelY);
}

function exportMetersLayout() {
  const { widthMeters, heightMeters } = getFieldMeters();
  const configuredTargetTagId = Number(targetTagIdInput.value);
  const targetTagId = Number.isFinite(configuredTargetTagId) && configuredTargetTagId > 0 ? configuredTargetTagId : null;
  const speedPxPerSec = Math.max(20, Number(robotSpeedInput.value) || 180);
  const speedMetersPerSec = Number(((speedPxPerSec / canvas.width) * widthMeters).toFixed(4));

  const robotMeters =
    state.robot == null
      ? null
      : (() => {
          const pos = pixelToMeters(state.robot.x, state.robot.y);
          return {
            x: Number(pos.xMeters.toFixed(4)),
            y: Number(pos.yMeters.toFixed(4)),
            rotationDeg: Number((state.robot.rotationDeg || 0).toFixed(2)),
          };
        })();

  return {
    mode: "meters",
    frame: {
      origin: exportOriginSelect.value,
      units: "meters",
    },
    deploy: getDeployConfig(),
    mockRun: {
      targetTagId,
      speedMetersPerSec,
      planner: "A_STAR_GRID",
    },
    fieldMeters: {
      width: widthMeters,
      height: heightMeters,
    },
    robot: robotMeters,
    tags: state.tags.map((tag) => {
      const pos = pixelToMeters(tag.x, tag.y);
      return {
        id: tag.id,
        x: Number(pos.xMeters.toFixed(4)),
        y: Number(pos.yMeters.toFixed(4)),
        rotationDeg: Number((tag.rotationDeg || 0).toFixed(2)),
      };
    }),
    obstacles: state.obstacles.map((obs) => {
      const pos = pixelToMeters(obs.x, obs.y);
      const width = (obs.width / canvas.width) * widthMeters;
      const height = (obs.height / canvas.height) * heightMeters;
      return {
        id: obs.id,
        x: Number(pos.xMeters.toFixed(4)),
        y: Number(pos.yMeters.toFixed(4)),
        width: Number(width.toFixed(4)),
        height: Number(height.toFixed(4)),
        rotationDeg: Number((obs.rotationDeg || 0).toFixed(2)),
      };
    }),
  };
}

function exportLayout() {
  const mode = jsonModeSelect.value;
  let payload;

  if (mode === "meters") {
    payload = exportMetersLayout();
  } else {
    payload = {
      mode: "pixels",
      fieldPx: {
        width: canvas.width,
        height: canvas.height,
      },
      deploy: getDeployConfig(),
      robot: state.robot,
      tags: state.tags,
      obstacles: state.obstacles,
    };
  }

  layoutJson.value = JSON.stringify(payload, null, 2);
}

function importPixelLayout(data) {
  const tags = Array.isArray(data.tags) ? data.tags : [];
  const obstacles = Array.isArray(data.obstacles) ? data.obstacles : [];

  let robot = null;
  if (data.robot && Number.isFinite(Number(data.robot.x)) && Number.isFinite(Number(data.robot.y))) {
    const pos = clampPosition(Number(data.robot.x), Number(data.robot.y));
    robot = {
      x: pos.x,
      y: pos.y,
      width: Math.max(20, Number(data.robot.width) || 70),
      height: Math.max(20, Number(data.robot.height) || 55),
      rotationDeg: Number(data.robot.rotationDeg) || 0,
    };
  }

  return {
    robot,
    tags: tags.map((tag, idx) => ({
      id: Number.isFinite(tag.id) ? tag.id : idx + 1,
      x: Number(tag.x) || 0,
      y: Number(tag.y) || 0,
      rotationDeg: Number(tag.rotationDeg) || 0,
    })),
    obstacles: obstacles.map((obs, idx) => ({
      id: Number.isFinite(obs.id) ? obs.id : idx + 1,
      x: Number(obs.x) || 0,
      y: Number(obs.y) || 0,
      width: Math.max(10, Number(obs.width) || 80),
      height: Math.max(10, Number(obs.height) || 60),
      rotationDeg: Number(obs.rotationDeg) || 0,
    })),
  };
}

function importMetersLayout(data) {
  applyDeployConfig(data.deploy);

  if (data.fieldMeters?.width) {
    fieldWidthMetersInput.value = String(data.fieldMeters.width);
  }
  if (data.fieldMeters?.height) {
    fieldHeightMetersInput.value = String(data.fieldMeters.height);
  }
  if (data.frame?.origin === "red-left" || data.frame?.origin === "blue-left") {
    exportOriginSelect.value = data.frame.origin;
  }

  const tags = Array.isArray(data.tags) ? data.tags : [];
  const obstacles = Array.isArray(data.obstacles) ? data.obstacles : [];

  const tagObjs = tags.map((tag, idx) => {
    const pos = metersToPixel(Number(tag.x) || 0, Number(tag.y) || 0);
    return {
      id: Number.isFinite(tag.id) ? tag.id : idx + 1,
      x: pos.x,
      y: pos.y,
      rotationDeg: Number(tag.rotationDeg) || 0,
    };
  });

  const obstacleObjs = obstacles.map((obs, idx) => {
    const pos = metersToPixel(Number(obs.x) || 0, Number(obs.y) || 0);
    const { widthMeters, heightMeters } = getFieldMeters();
    return {
      id: Number.isFinite(obs.id) ? obs.id : idx + 1,
      x: pos.x,
      y: pos.y,
      width: Math.max(10, ((Number(obs.width) || 0.5) / widthMeters) * canvas.width),
      height: Math.max(10, ((Number(obs.height) || 0.5) / heightMeters) * canvas.height),
      rotationDeg: Number(obs.rotationDeg) || 0,
    };
  });

  let robot = null;
  if (data.robot && Number.isFinite(Number(data.robot.x)) && Number.isFinite(Number(data.robot.y))) {
    const pos = metersToPixel(Number(data.robot.x), Number(data.robot.y));
    robot = {
      x: pos.x,
      y: pos.y,
      width: 70,
      height: 55,
      rotationDeg: Number(data.robot.rotationDeg) || 0,
    };
  }

  return {
    robot,
    tags: tagObjs,
    obstacles: obstacleObjs,
  };
}

function getDeployConfig() {
  const teamNumber = Number(teamNumberInput.value);

  return {
    teamNumber: Number.isFinite(teamNumber) && teamNumber > 0 ? teamNumber : null,
    target: deployTargetSelect.value,
    connection: deployConnectionSelect.value,
    joysticks: {
      driver: {
        type: driverJoystickTypeSelect.value,
        port: Math.max(0, Number(driverJoystickPortInput.value) || 0),
        attached: Boolean(driverJoystickAttachedInput.checked),
      },
      operator: {
        type: operatorJoystickTypeSelect.value,
        port: Math.max(0, Number(operatorJoystickPortInput.value) || 1),
        attached: Boolean(operatorJoystickAttachedInput.checked),
      },
    },
  };
}

function applyDeployConfig(deploy) {
  if (!deploy || typeof deploy !== "object") {
    return;
  }

  if (Number.isFinite(Number(deploy.teamNumber)) && Number(deploy.teamNumber) > 0) {
    teamNumberInput.value = String(Number(deploy.teamNumber));
  }

  if (["sim", "wpilib-mock", "roborio"].includes(deploy.target)) {
    deployTargetSelect.value = deploy.target;
  }

  if (["usb", "ethernet", "wifi"].includes(deploy.connection)) {
    deployConnectionSelect.value = deploy.connection;
  }

  const driver = deploy.joysticks?.driver;
  if (driver) {
    if (["xbox", "joystick", "ps", "custom"].includes(driver.type)) {
      driverJoystickTypeSelect.value = driver.type;
    }
    if (Number.isFinite(Number(driver.port))) {
      driverJoystickPortInput.value = String(Math.max(0, Number(driver.port)));
    }
    if (typeof driver.attached === "boolean") {
      driverJoystickAttachedInput.checked = driver.attached;
    }
  }

  const operator = deploy.joysticks?.operator;
  if (operator) {
    if (["xbox", "joystick", "ps", "none"].includes(operator.type)) {
      operatorJoystickTypeSelect.value = operator.type;
    }
    if (Number.isFinite(Number(operator.port))) {
      operatorJoystickPortInput.value = String(Math.max(0, Number(operator.port)));
    }
    if (typeof operator.attached === "boolean") {
      operatorJoystickAttachedInput.checked = operator.attached;
    }
  }
}

function importLayout() {
  try {
    const data = JSON.parse(layoutJson.value);
    const mode = data.mode === "meters" ? "meters" : "pixels";
    const parsed = mode === "meters" ? importMetersLayout(data) : importPixelLayout(data);

    applyDeployConfig(data.deploy);

    stopMockAuto();
    state.robot = parsed.robot;
    state.tags = parsed.tags;
    state.obstacles = parsed.obstacles;
    state.nextTagId = Math.max(1, ...state.tags.map((tag) => tag.id + 1));
    state.nextObstacleId = Math.max(1, ...state.obstacles.map((obs) => obs.id + 1));
    state.selected = null;

    render();
    updateEditor();
    setStatus("Layout imported");
  } catch {
    setStatus("Invalid JSON");
  }
}

function pointInsideRect(point, obstacle, inflate = 0) {
  return (
    point.x >= obstacle.x - obstacle.width / 2 - inflate &&
    point.x <= obstacle.x + obstacle.width / 2 + inflate &&
    point.y >= obstacle.y - obstacle.height / 2 - inflate &&
    point.y <= obstacle.y + obstacle.height / 2 + inflate
  );
}

function segmentIntersectsInflatedObstacle(p1, p2, obstacle, inflate = 0) {
  const minX = obstacle.x - obstacle.width / 2 - inflate;
  const maxX = obstacle.x + obstacle.width / 2 + inflate;
  const minY = obstacle.y - obstacle.height / 2 - inflate;
  const maxY = obstacle.y + obstacle.height / 2 + inflate;

  if (pointInsideRect(p1, obstacle, inflate) || pointInsideRect(p2, obstacle, inflate)) {
    return true;
  }

  const edges = [
    [{ x: minX, y: minY }, { x: maxX, y: minY }],
    [{ x: maxX, y: minY }, { x: maxX, y: maxY }],
    [{ x: maxX, y: maxY }, { x: minX, y: maxY }],
    [{ x: minX, y: maxY }, { x: minX, y: minY }],
  ];

  for (const [e1, e2] of edges) {
    if (segmentsIntersect(p1, p2, e1, e2)) {
      return true;
    }
  }
  return false;
}

function segmentsIntersect(a, b, c, d) {
  const cross = (p, q, r) => (q.x - p.x) * (r.y - p.y) - (q.y - p.y) * (r.x - p.x);
  const onSegment = (p, q, r) =>
    Math.min(p.x, q.x) <= r.x + 1e-9 &&
    r.x <= Math.max(p.x, q.x) + 1e-9 &&
    Math.min(p.y, q.y) <= r.y + 1e-9 &&
    r.y <= Math.max(p.y, q.y) + 1e-9;

  const o1 = cross(a, b, c);
  const o2 = cross(a, b, d);
  const o3 = cross(c, d, a);
  const o4 = cross(c, d, b);

  if ((o1 > 0 && o2 < 0) || (o1 < 0 && o2 > 0)) {
    if ((o3 > 0 && o4 < 0) || (o3 < 0 && o4 > 0)) {
      return true;
    }
  }

  if (Math.abs(o1) < 1e-9 && onSegment(a, b, c)) {
    return true;
  }
  if (Math.abs(o2) < 1e-9 && onSegment(a, b, d)) {
    return true;
  }
  if (Math.abs(o3) < 1e-9 && onSegment(c, d, a)) {
    return true;
  }
  if (Math.abs(o4) < 1e-9 && onSegment(c, d, b)) {
    return true;
  }

  return false;
}

function segmentClear(p1, p2, ignoreObstacle = null) {
  const margin = getRobotClearancePx();
  for (const obs of state.obstacles) {
    if (ignoreObstacle && ignoreObstacle.id === obs.id) {
      continue;
    }
    if (segmentIntersectsInflatedObstacle(p1, p2, obs, margin)) {
      return false;
    }
  }
  return true;
}

function getRobotClearancePx() {
  const robotWidth = state.robot?.width ?? 70;
  const robotHeight = state.robot?.height ?? 55;
  return Math.max(robotWidth, robotHeight) / 2 + 8;
}

function buildGridConfig() {
  const cellSize = 22;
  return {
    cellSize,
    cols: Math.max(1, Math.ceil(canvas.width / cellSize)),
    rows: Math.max(1, Math.ceil(canvas.height / cellSize)),
    inflate: getRobotClearancePx(),
  };
}

function cellKey(row, col) {
  return `${row},${col}`;
}

function pointToCell(point, grid) {
  const col = Math.max(0, Math.min(grid.cols - 1, Math.floor(point.x / grid.cellSize)));
  const row = Math.max(0, Math.min(grid.rows - 1, Math.floor(point.y / grid.cellSize)));
  return { row, col };
}

function cellToPoint(cell, grid) {
  return {
    x: Math.min(canvas.width, cell.col * grid.cellSize + grid.cellSize / 2),
    y: Math.min(canvas.height, cell.row * grid.cellSize + grid.cellSize / 2),
  };
}

function makeBlockedGrid(grid) {
  const blocked = Array.from({ length: grid.rows }, () => Array(grid.cols).fill(false));

  for (let row = 0; row < grid.rows; row += 1) {
    for (let col = 0; col < grid.cols; col += 1) {
      const center = cellToPoint({ row, col }, grid);
      blocked[row][col] = state.obstacles.some((obs) => pointInsideRect(center, obs, grid.inflate));
    }
  }

  return blocked;
}

function nearestFreeCell(startCell, blocked, grid) {
  if (!blocked[startCell.row][startCell.col]) {
    return startCell;
  }

  const visited = new Set([cellKey(startCell.row, startCell.col)]);
  const queue = [startCell];

  while (queue.length > 0) {
    const current = queue.shift();

    for (let dr = -1; dr <= 1; dr += 1) {
      for (let dc = -1; dc <= 1; dc += 1) {
        if (dr === 0 && dc === 0) {
          continue;
        }
        const row = current.row + dr;
        const col = current.col + dc;
        if (row < 0 || row >= grid.rows || col < 0 || col >= grid.cols) {
          continue;
        }

        const key = cellKey(row, col);
        if (visited.has(key)) {
          continue;
        }
        visited.add(key);

        if (!blocked[row][col]) {
          return { row, col };
        }
        queue.push({ row, col });
      }
    }
  }

  return null;
}

function heuristicCost(a, b) {
  const dx = Math.abs(a.col - b.col);
  const dy = Math.abs(a.row - b.row);
  const diagonal = Math.min(dx, dy);
  const straight = Math.max(dx, dy) - diagonal;
  return diagonal * Math.SQRT2 + straight;
}

function reconstructCellPath(cameFrom, endCell) {
  const result = [endCell];
  let current = endCell;
  while (true) {
    const parent = cameFrom.get(cellKey(current.row, current.col));
    if (!parent) {
      break;
    }
    result.push(parent);
    current = parent;
  }
  result.reverse();
  return result;
}

function runAStar(startCell, goalCell, blocked, grid) {
  const open = [startCell];
  const inOpen = new Set([cellKey(startCell.row, startCell.col)]);
  const cameFrom = new Map();
  const gScore = new Map([[cellKey(startCell.row, startCell.col), 0]]);
  const fScore = new Map([
    [cellKey(startCell.row, startCell.col), heuristicCost(startCell, goalCell)],
  ]);

  while (open.length > 0) {
    let bestIndex = 0;
    let bestF = Number.POSITIVE_INFINITY;
    for (let i = 0; i < open.length; i += 1) {
      const candidate = open[i];
      const candidateF = fScore.get(cellKey(candidate.row, candidate.col)) ?? Number.POSITIVE_INFINITY;
      if (candidateF < bestF) {
        bestF = candidateF;
        bestIndex = i;
      }
    }

    const current = open.splice(bestIndex, 1)[0];
    inOpen.delete(cellKey(current.row, current.col));

    if (current.row === goalCell.row && current.col === goalCell.col) {
      return reconstructCellPath(cameFrom, current);
    }

    for (let dr = -1; dr <= 1; dr += 1) {
      for (let dc = -1; dc <= 1; dc += 1) {
        if (dr === 0 && dc === 0) {
          continue;
        }

        const row = current.row + dr;
        const col = current.col + dc;
        if (row < 0 || row >= grid.rows || col < 0 || col >= grid.cols) {
          continue;
        }
        if (blocked[row][col]) {
          continue;
        }

        if (dr !== 0 && dc !== 0) {
          const sideA = blocked[current.row + dr][current.col];
          const sideB = blocked[current.row][current.col + dc];
          if (sideA || sideB) {
            continue;
          }
        }

        const neighbor = { row, col };
        const moveCost = dr !== 0 && dc !== 0 ? Math.SQRT2 : 1;
        const currentKey = cellKey(current.row, current.col);
        const neighborKey = cellKey(neighbor.row, neighbor.col);
        const tentative = (gScore.get(currentKey) ?? Number.POSITIVE_INFINITY) + moveCost;

        if (tentative >= (gScore.get(neighborKey) ?? Number.POSITIVE_INFINITY)) {
          continue;
        }

        cameFrom.set(neighborKey, current);
        gScore.set(neighborKey, tentative);
        fScore.set(neighborKey, tentative + heuristicCost(neighbor, goalCell));

        if (!inOpen.has(neighborKey)) {
          open.push(neighbor);
          inOpen.add(neighborKey);
        }
      }
    }
  }

  return null;
}

function smoothPath(pathPoints) {
  if (!pathPoints || pathPoints.length <= 2) {
    return pathPoints;
  }

  const simplified = [pathPoints[0]];
  let anchor = 0;

  while (anchor < pathPoints.length - 1) {
    let next = anchor + 1;
    for (let candidate = pathPoints.length - 1; candidate > anchor + 1; candidate -= 1) {
      if (segmentClear(pathPoints[anchor], pathPoints[candidate])) {
        next = candidate;
        break;
      }
    }
    simplified.push(pathPoints[next]);
    anchor = next;
  }

  return simplified;
}

function buildObstacleAwarePath(start, goal) {
  const grid = buildGridConfig();
  const blocked = makeBlockedGrid(grid);

  let startCell = pointToCell(start, grid);
  let goalCell = pointToCell(goal, grid);

  startCell = nearestFreeCell(startCell, blocked, grid);
  goalCell = nearestFreeCell(goalCell, blocked, grid);

  if (!startCell || !goalCell) {
    return null;
  }

  if (segmentClear(start, goal)) {
    return [start, goal];
  }

  const cellPath = runAStar(startCell, goalCell, blocked, grid);
  if (!cellPath || cellPath.length === 0) {
    return null;
  }

  const middle = cellPath.map((cell) => cellToPoint(cell, grid));
  const rawPath = [start, ...middle, goal];
  return smoothPath(rawPath);
}

function downloadLayoutJson(fileName, payload) {
  const blob = new Blob([JSON.stringify(payload, null, 2)], { type: "application/json" });
  const blobUrl = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = blobUrl;
  link.download = fileName;
  link.click();
  URL.revokeObjectURL(blobUrl);
}

function getTargetTag() {
  const configuredId = Number(targetTagIdInput.value);
  if (Number.isFinite(configuredId) && configuredId > 0) {
    return state.tags.find((tag) => tag.id === configuredId) ?? null;
  }

  if (state.selected?.type === "tag") {
    const selectedTag = state.tags.find((tag) => tag.id === state.selected.id);
    if (selectedTag) {
      return selectedTag;
    }
  }

  if (!state.robot || state.tags.length === 0) {
    return null;
  }

  let nearest = state.tags[0];
  let best = distance(state.robot, nearest);
  for (const tag of state.tags) {
    const d = distance(state.robot, tag);
    if (d < best) {
      best = d;
      nearest = tag;
    }
  }
  return nearest;
}

function startMockAuto() {
  if (!state.robot) {
    setStatus("Place robot first");
    return;
  }
  if (state.tags.length === 0) {
    setStatus("Place at least one AprilTag");
    return;
  }

  const targetTag = getTargetTag();
  if (!targetTag) {
    setStatus("Target tag not found");
    return;
  }

  const path = buildObstacleAwarePath(
    { x: state.robot.x, y: state.robot.y },
    { x: targetTag.x, y: targetTag.y }
  );

  if (!path || path.length < 2) {
    setStatus("No collision-free path to target tag");
    return;
  }

  state.simulation.running = true;
  state.simulation.pathPoints = path;
  state.simulation.segmentIndex = 0;
  state.simulation.targetTagId = targetTag.id;
  state.simulation.speedPxPerSec = Math.max(20, Number(robotSpeedInput.value) || 180);
  state.simulation.lastTimestampMs = 0;

  setStatus(`Running mock auto to Tag ${targetTag.id}`);
  render();

  if (state.simulation.frameHandle) {
    cancelAnimationFrame(state.simulation.frameHandle);
  }
  state.simulation.frameHandle = requestAnimationFrame(stepSimulation);
}

function startWpilibMockRun() {
  if (!state.robot) {
    setStatus("Place robot first");
    return;
  }
  if (state.tags.length === 0) {
    setStatus("Place at least one AprilTag");
    return;
  }

  const payload = exportMetersLayout();
  layoutJson.value = JSON.stringify(payload, null, 2);
  downloadLayoutJson("ui-current-layout.json", payload);

  setStatus(
    "WPILib mock layout downloaded. Save to src/main/deploy/layouts/ui-current-layout.json and run WPILib simulation"
  );
}

function exportDeployConfig() {
  const payload = {
    format: "frc-deploy-config-v1",
    generatedAt: new Date().toISOString(),
    deploy: getDeployConfig(),
  };
  downloadLayoutJson("deploy-config.json", payload);
  setStatus("Deploy config exported");
}

function stopMockAuto() {
  state.simulation.running = false;
  if (state.simulation.frameHandle) {
    cancelAnimationFrame(state.simulation.frameHandle);
    state.simulation.frameHandle = null;
  }
  clearPathOverlay();
  render();
}

function stepSimulation(timestampMs) {
  if (!state.simulation.running || !state.robot) {
    return;
  }

  if (!state.simulation.lastTimestampMs) {
    state.simulation.lastTimestampMs = timestampMs;
  }

  const dt = Math.min(0.05, Math.max(0, (timestampMs - state.simulation.lastTimestampMs) / 1000));
  state.simulation.lastTimestampMs = timestampMs;

  const path = state.simulation.pathPoints;
  if (path.length < 2) {
    stopMockAuto();
    setStatus("No valid path");
    return;
  }

  let remain = state.simulation.speedPxPerSec * dt;

  while (remain > 0 && state.simulation.segmentIndex < path.length - 1) {
    const to = path[state.simulation.segmentIndex + 1];

    const current = { x: state.robot.x, y: state.robot.y };
    const segVec = { x: to.x - current.x, y: to.y - current.y };
    const segLen = Math.hypot(segVec.x, segVec.y);

    if (segLen < 1.2) {
      state.robot.x = to.x;
      state.robot.y = to.y;
      state.simulation.segmentIndex += 1;
      continue;
    }

    const step = Math.min(remain, segLen);
    const ux = segVec.x / segLen;
    const uy = segVec.y / segLen;
    state.robot.x += ux * step;
    state.robot.y += uy * step;
    state.robot.rotationDeg = (Math.atan2(uy, ux) * 180) / Math.PI;
    remain -= step;
  }

  if (state.simulation.segmentIndex >= path.length - 1) {
    state.simulation.running = false;
    clearPathOverlay();
    render();
    setStatus(`Arrived at Tag ${state.simulation.targetTagId}`);
    return;
  }

  render();
  state.simulation.frameHandle = requestAnimationFrame(stepSimulation);
}

function onCanvasMouseDown(event) {
  const point = toCanvasPoint(event);

  if (state.mode === "tag") {
    addTag(point);
    updateEditor();
    render();
    return;
  }

  if (state.mode === "obstacle") {
    addObstacle(point);
    updateEditor();
    render();
    return;
  }

  if (state.mode === "robot") {
    placeRobot(point);
    updateEditor();
    render();
    return;
  }

  const hit = findObjectAtPoint(point);
  state.selected = hit;
  updateEditor();
  render();

  if (hit) {
    const selected = getSelectedObject();
    state.dragging = true;
    state.dragOffsetX = point.x - selected.x;
    state.dragOffsetY = point.y - selected.y;
  }
}

function onCanvasMouseMove(event) {
  if (!state.dragging) {
    return;
  }

  const selected = getSelectedObject();
  if (!selected) {
    state.dragging = false;
    return;
  }

  const point = toCanvasPoint(event);
  const nextPos = clampPosition(point.x - state.dragOffsetX, point.y - state.dragOffsetY);
  selected.x = nextPos.x;
  selected.y = nextPos.y;

  updateEditor();
  render();
}

function onCanvasMouseUp() {
  state.dragging = false;
}

modeButtons.select.addEventListener("click", () => setMode("select"));
modeButtons.tag.addEventListener("click", () => setMode("tag"));
modeButtons.obstacle.addEventListener("click", () => setMode("obstacle"));
modeButtons.robot.addEventListener("click", () => setMode("robot"));
tabSimulationBtn.addEventListener("click", () => setActiveTab("simulation"));
tabDeploymentBtn.addEventListener("click", () => setActiveTab("deployment"));

runMockAutoBtn.addEventListener("click", () => {
  if (runEngineSelect.value === "wpilib") {
    stopMockAuto();
    startWpilibMockRun();
    return;
  }
  startMockAuto();
});
stopMockAutoBtn.addEventListener("click", () => {
  stopMockAuto();
  setStatus("Mock auto stopped");
});

deleteSelectedBtn.addEventListener("click", deleteSelected);
clearAllBtn.addEventListener("click", () => {
  stopMockAuto();
  state.tags = [];
  state.obstacles = [];
  state.robot = null;
  state.selected = null;
  updateEditor();
  render();
  setStatus("Cleared all objects");
});

exportJsonBtn.addEventListener("click", exportLayout);
importJsonBtn.addEventListener("click", importLayout);
applyEditsBtn.addEventListener("click", applyEdits);
exportDeployConfigBtn.addEventListener("click", exportDeployConfig);

window.addEventListener("keydown", (event) => {
  if (event.key === "Delete") {
    deleteSelected();
  }
});

canvas.addEventListener("mousedown", onCanvasMouseDown);
canvas.addEventListener("mousemove", onCanvasMouseMove);
canvas.addEventListener("mouseup", onCanvasMouseUp);
canvas.addEventListener("mouseleave", onCanvasMouseUp);

setMode("select");
setActiveTab("simulation");
updateEditor();
render();
