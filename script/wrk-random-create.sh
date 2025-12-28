#!/bin/bash

# wrk script to test POST requests for creating aliases
# Runs for 2 minutes with maximum throughput

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Starting wrk load test for alias creation..."
echo "Target: ${BASE_URL}/app/api/create"
echo "Duration: 2 minutes"
echo "Generating random POST requests..."
echo ""

# Run wrk with maximum threads and connections
# -t: number of threads (use CPU cores)
# -c: number of connections (lower for write-heavy workloads)
# -d: duration (2 minutes = 120s)
# -s: Lua script for random alias creation
# --latency: show latency statistics
# --timeout: request timeout (10 seconds)

wrk -t$(sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo 12) \
    -c200 \
    -d120s \
    -s wrk-random-create.lua \
    --latency \
    --timeout 10s \
    "${BASE_URL}"

