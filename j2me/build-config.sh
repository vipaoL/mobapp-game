#!/bin/sh

SOURCES_PATHS=""${WORK_DIR}"/src "${WORK_DIR}"/../src "${WORK_DIR}"/../lib/EminiPhysicsEngine/src "${WORK_DIR}"/../lib/mobapp-framework/src "${WORK_DIR}"/../lib/mobapp-platform-j2me/src"
EXCLUDE_PATTERN="at/emini/physics2DDesigner|at/emini/physics2DSimulationTests|at/emini/physics2DUnitTests|at/emini/physics2DVisualTest"
#LIB_JARS_DIR="${WORK_DIR}"/lib         # app libraries (jars)
RES="${WORK_DIR}"/../res                # resourses
RES_EXCLUDE_PATTERN="editorlogo.png"
GIT_VERSION=$(git describe --tags 2>/dev/null || echo "dev")
APP_NAME="mobapp-game-j2me-${GIT_VERSION}"
MANIFEST="${WORK_DIR}"/'META-INF/MANIFEST.MF'
#JAVA_HOME=

