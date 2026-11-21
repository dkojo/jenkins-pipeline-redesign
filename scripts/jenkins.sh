#!/usr/bin/env bash
# Talk to the CI-run Jenkins:
#   bash scripts/jenkins.sh wait-ready          wait until Jenkins is up and the job exists
#   bash scripts/jenkins.sh trigger             start build #1 of the redesign job
#   bash scripts/jenkins.sh follow "<stage>"    wait for one stage to finish and print its lines
#   bash scripts/jenkins.sh result              print the build result and fail unless it is SUCCESS
set -euo pipefail
J=http://localhost:8090
AUTH=admin:admin
JOB=$J/job/redesign
cookies=$(mktemp)

console() { curl -s -u $AUTH "$JOB/1/consoleText"; }
building() { curl -s -u $AUTH "$JOB/1/api/json?tree=building" | python3 -c 'import json,sys; print(json.load(sys.stdin)["building"])' 2>/dev/null || echo True; }
result() { curl -s -u $AUTH "$JOB/1/api/json?tree=result" | python3 -c 'import json,sys; print(json.load(sys.stdin)["result"])' 2>/dev/null; }

case "$1" in
  wait-ready)
    for _ in $(seq 1 90); do
      [ "$(curl -s -o /dev/null -w '%{http_code}' -u $AUTH "$JOB/api/json")" = 200 ] && { echo "Jenkins is up, job 'redesign' is configured"; exit 0; }
      sleep 2
    done
    echo "Jenkins did not come up"; docker logs jenkins | tail -40; exit 1 ;;
  trigger)
    crumb=$(curl -s -c "$cookies" -u $AUTH "$J/crumbIssuer/api/json" | python3 -c 'import json,sys; d=json.load(sys.stdin); print(d["crumbRequestField"] + ":" + d["crumb"])')
    curl -sf -b "$cookies" -u $AUTH -H "$crumb" -X POST "$JOB/build" >/dev/null
    for _ in $(seq 1 60); do
      [ "$(curl -s -o /dev/null -w '%{http_code}' -u $AUTH "$JOB/1/api/json")" = 200 ] && { echo "Jenkins build #1 started"; exit 0; }
      sleep 1
    done
    echo "The build did not start"; exit 1 ;;
  follow)
    stage="$2"
    for _ in $(seq 1 600); do
      if console | grep -q "STAGE-OK $stage\$"; then
        # Show this stage's own lines: from where it started to its marker (parallel stages can interleave)
        log=$(console)
        printf '%s
' "$log" | awk -v s="($stage)" -v m="STAGE-OK $stage" 'index($0, s) {on=1} on && $0 !~ /\[Pipeline\]/ {print} index($0, m) {exit}' | tail -12 || true
        echo "Jenkins stage passed: $stage"
        exit 0
      fi
      if [ "$(building)" = False ]; then
        console | tail -40
        echo "Jenkins finished ($(result)) without passing stage: $stage"
        exit 1
      fi
      sleep 2
    done
    echo "Timed out waiting for stage: $stage"; exit 1 ;;
  result)
    for _ in $(seq 1 120); do [ "$(building)" = False ] && break; sleep 2; done
    r=$(result); echo "Jenkins build #1: $r"
    [ "$r" = SUCCESS ] ;;
esac
