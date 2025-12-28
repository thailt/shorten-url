#!/bin/bash

# wrk script to test GET requests with random aliases
# Runs for 2 minutes with maximum throughput

BASE_URL="${BASE_URL:-http://localhost:8080}"

echo "Starting wrk load test for random GET requests..."
echo "Target: ${BASE_URL}"
echo "Duration: 2 minutes"
echo "Generating random GET requests..."
echo ""

# Run wrk with maximum threads and connections
# -t: number of threads (use CPU cores)
# -c: number of connections (high for maximum load)
# -d: duration (2 minutes = 120s)
# -s: Lua script for random alias generation
# --latency: show latency statistics
# --timeout: request timeout (10 seconds)

wrk -t$(sysctl -n hw.ncpu 2>/dev/null || nproc 2>/dev/null || echo 12) \
    -c400 \
    -d120s \
    -s wrk-random-get.lua \
    --latency \
    --timeout 10s \
    "${BASE_URL}"

