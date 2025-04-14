#!/bin/bash
while read line; do
  printf '{"timeStamp": "%s"}\n' "$(date -u +%Y-%m-%dT%H:%M:%SZ)"
done