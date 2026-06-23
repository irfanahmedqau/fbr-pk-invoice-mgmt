#!/usr/bin/env bash
# Starts the Spring Boot backend and Angular frontend together (macOS/Linux).
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BACKEND_DIR="$SCRIPT_DIR/backend"
FRONTEND_DIR="$SCRIPT_DIR/frontend"
LOG_DIR="$SCRIPT_DIR/logs"
mkdir -p "$LOG_DIR"

BACKEND_PORT=8081
FRONTEND_PORT=4200
REQUIRED_JAVA_MAJOR=17

# ---------------------------------------------------------------------------
# spring-boot-maven-plugin needs Java 17+ to run, regardless of what the
# project itself compiles against. Find a suitable JDK without touching the
# user's global JAVA_HOME.
# ---------------------------------------------------------------------------

get_java_major_version() {
  local ver_str
  ver_str=$("$1" -version 2>&1 | head -1)
  if [[ $ver_str =~ \"1\.([0-9]+) ]]; then
    echo "${BASH_REMATCH[1]}"
  elif [[ $ver_str =~ \"([0-9]+) ]]; then
    echo "${BASH_REMATCH[1]}"
  else
    echo 0
  fi
}

find_suitable_java_home() {
  if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
    if [ "$(get_java_major_version "$JAVA_HOME/bin/java")" -ge "$REQUIRED_JAVA_MAJOR" ] 2>/dev/null; then
      echo "$JAVA_HOME"
      return 0
    fi
  fi

  if command -v java >/dev/null 2>&1; then
    local path_java resolved
    path_java=$(command -v java)
    if [ "$(get_java_major_version "$path_java")" -ge "$REQUIRED_JAVA_MAJOR" ] 2>/dev/null; then
      resolved=$(readlink -f "$path_java" 2>/dev/null || echo "$path_java")
      (cd "$(dirname "$resolved")/.." && pwd)
      return 0
    fi
  fi

  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    local jh
    if jh=$(/usr/libexec/java_home -v "$REQUIRED_JAVA_MAJOR"+ 2>/dev/null); then
      echo "$jh"
      return 0
    fi
  fi

  return 1
}

JAVA_RUN_HOME="$(find_suitable_java_home)" || {
  echo "ERROR: Java $REQUIRED_JAVA_MAJOR+ is required to run the backend but was not found."
  echo "Install a JDK $REQUIRED_JAVA_MAJOR+ (e.g. https://adoptium.net) and try again."
  exit 1
}
echo "Using JDK at: $JAVA_RUN_HOME"

if lsof -ti tcp:$BACKEND_PORT -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Backend already running on port $BACKEND_PORT"
else
  echo "Starting backend (Spring Boot) on port $BACKEND_PORT..."
  (cd "$BACKEND_DIR" && JAVA_HOME="$JAVA_RUN_HOME" PATH="$JAVA_RUN_HOME/bin:$PATH" \
    nohup ./mvnw spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &)
fi

if lsof -ti tcp:$FRONTEND_PORT -sTCP:LISTEN >/dev/null 2>&1; then
  echo "Frontend already running on port $FRONTEND_PORT"
else
  if [ ! -d "$FRONTEND_DIR/node_modules" ]; then
    echo "Installing frontend dependencies..."
    (cd "$FRONTEND_DIR" && npm install)
  fi
  echo "Starting frontend (Angular) on port $FRONTEND_PORT..."
  (cd "$FRONTEND_DIR" && nohup npm start > "$LOG_DIR/frontend.log" 2>&1 &)
fi

echo ""
echo "Backend log:  $LOG_DIR/backend.log"
echo "Frontend log: $LOG_DIR/frontend.log"
echo "Backend:  http://localhost:$BACKEND_PORT"
echo "Frontend: http://localhost:$FRONTEND_PORT"
echo "Run ./stop.sh to shut both down."
