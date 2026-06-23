#!/usr/bin/env bash
# Stops the Spring Boot backend and Angular frontend (macOS/Linux).
# Kills by port rather than PID file, since mvnw/npm fork a child process
# that doesn't share the PID of the launcher process.

BACKEND_PORT=8081
FRONTEND_PORT=4200

kill_port() {
  local port=$1
  local name=$2
  local pids
  pids=$(lsof -ti tcp:"$port" -sTCP:LISTEN 2>/dev/null || true)
  if [ -z "$pids" ]; then
    echo "$name not running on port $port"
  else
    echo "Stopping $name (port $port, pid(s): $pids)..."
    kill -9 $pids
  fi
}

kill_port $BACKEND_PORT "Backend"
kill_port $FRONTEND_PORT "Frontend"

echo "Done."
