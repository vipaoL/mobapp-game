#!/bin/sh -e

# Make it possible to run this script from any directory
WORK_DIR=`readlink -f $(dirname $0)`
cd ${WORK_DIR}

. ./build-config.sh

if [ ! -e "${MANIFEST}" ] ; then
  echo
  echo "${MANIFEST} is not found in $(pwd)"
  exit 2
fi

MANIFEST_TMP="${WORK_DIR}"/bin/manifest-tmp.mf
mkdir -p "$(dirname "$MANIFEST_TMP")"
grep -v "^Commit:" "${MANIFEST}" > "$MANIFEST_TMP" || true

LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
TAG_VERSION="${LAST_TAG#v}"

MIDLET_VERSION=$(grep -i "^MIDlet-Version:" "${MANIFEST}" | sed 's/.*: *//' | tr -d '\r' || echo "")

echo
if [ -n "${TAG_VERSION}" ] && [ "${TAG_VERSION}" = "${MIDLET_VERSION}" ]; then
  echo "MIDlet-Version matches git tag (${TAG_VERSION}). Skipping commit hash."
else
  COMMIT=$(git rev-parse --short HEAD 2>/dev/null || echo "unknown")
  echo "Adding commit hash $COMMIT to $MANIFEST_TMP"
  echo "Commit: ${COMMIT}" >> "${MANIFEST_TMP}"
fi

MANIFEST=$MANIFEST_TMP

J2ME_CLASSPATH_DIR="${WORK_DIR}"/lib
CLASSPATH=${J2ME_CLASSPATH_DIR}/*
CLDCAPI=${J2ME_CLASSPATH_DIR}/cldcapi11.jar
MIDPAPI=${J2ME_CLASSPATH_DIR}/midpapi20.jar
JAVAC=javac
JAR=jar

echo
if [ ! -n "${JAVA_HOME}" ] ; then
  # let's assume you have openjdk-8 installed
  JAVA_HOME="$(dirname $(dirname $(readlink -f $(which javac))))"
fi

if [ -d "${JAVA_HOME}" ] ; then
  JAVAC=${JAVA_HOME}/bin/javac
  JAR=${JAVA_HOME}/bin/jar
else
  echo "Error: java is not found:"
  file "${JAVA_HOME}"
  exit 1
fi

echo "Java: ${JAVA_HOME}"
"${JAVA_HOME}"/bin/java -version

echo
echo "Cleaning tmp directories..."
mkdir -p bin/tmpclasses
mkdir -p bin/classes
rm -rf bin/tmpclasses/*
rm -rf bin/classes/*

if [ -n "${LIB_JARS_DIR}" ] ; then
  cd bin/tmpclasses
  LIB_JARS="${LIB_JARS_DIR}/*.jar"
  echo $LIB_JARS
  echo "Unpacking your libraries: ${LIB_JARS}"
  ${JAR} xf ${LIB_JARS}
  rm -rf META-INF
fi

cd ${WORK_DIR}
echo
echo "Compiling source files..."
PATHSEP=":"
${JAVAC} \
    -Xlint:-options \
    -bootclasspath ${CLDCAPI}${PATHSEP}${MIDPAPI} \
    -source 1.3 \
    -target 1.3 \
    -d bin/tmpclasses \
    -classpath bin/tmpclasses${PATHSEP}${CLASSPATH} \
    -extdirs ../lib \
    `find ${SOURCES_PATHS} -name '*.java' | grep -vE "${EXCLUDE_PATTERN}"`

echo
echo "Preverifying class files..."
if [ -z "${PROGUARD_HOME}" ]; then
  PROGUARD_HOME=./proguard6.2.2
fi

PROGUARD_JAR="${PROGUARD_HOME}/lib/proguard.jar"
if [ ! -f "${PROGUARD_JAR}" ]; then
  echo "Error: proguard.jar not found in ${PROGUARD_HOME}/lib/. PWD: $PWD"
  exit 1
fi

PROGUARD_LIBS=""
for jar in "${J2ME_CLASSPATH_DIR}"/*.jar; do
  if [ "$(basename "$jar")" != "cldcapi10.jar" ]; then
    if [ -z "$PROGUARD_LIBS" ]; then
      PROGUARD_LIBS="$jar"
    else
      PROGUARD_LIBS="${PROGUARD_LIBS}${PATHSEP}${jar}"
    fi
  fi
done

if [ -f "proguard.cfg" ] && [ -s "proguard.cfg" ]; then
  echo "Preverifying and obfuscating class files with proguard.cfg..."
  java -jar "${PROGUARD_JAR}" \
      -injars bin/tmpclasses \
      -outjars bin/classes \
      -libraryjars "${PROGUARD_LIBS}" \
      @proguard.cfg
else
  echo "Preverifying class files without obfuscation (proguard.cfg not found)..."
  java -jar "${PROGUARD_JAR}" \
      -injars bin/tmpclasses \
      -outjars bin/classes \
      -libraryjars "${PROGUARD_LIBS}" \
      -microedition \
      -dontshrink \
      -dontoptimize \
      -dontobfuscate \
      -dontwarn
fi

echo
echo "Jaring preverified class files..."
APP="${WORK_DIR}"/bin/"${APP_NAME}".jar
${JAR} cmf "${MANIFEST}" "${APP}" -C bin/classes .

echo
if [ -d "${RES}" ] ; then
  echo "Adding resources: ${RES}"
  ${JAR} uf "${APP}" -C "${RES}" .
else
  echo "Resource folder "${RES}" not found, skipping..."
fi

echo
echo "Done!" "${APP}"
