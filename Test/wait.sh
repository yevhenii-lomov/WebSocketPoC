#!/bin/bash

# Specify the target time in HH:MM format
TARGET_TIME="$TIME_TO_RUN"

echo "Current time $(date)"

if [ ! -z "${TIME_TO_RUN+x}" ]; then
    # Get the current time in seconds since epoch
    current_time=$(date +%s)

    # Get the target time in seconds since epoch
    target_time=$(date -d "$TARGET_TIME" +%s)

    # If the target time is already passed today, schedule it for tomorrow
    if [ "$current_time" -ge "$target_time" ]; then
    target_time=$(date -d "tomorrow $TARGET_TIME" +%s)
    fi

    # Calculate the sleep duration in seconds
    sleep_duration=$(( target_time - current_time ))

    # Wait until the target time
    echo "Waiting for $sleep_duration seconds until $TARGET_TIME..."
    sleep "$sleep_duration"
fi
k6 run /scripts/script.js