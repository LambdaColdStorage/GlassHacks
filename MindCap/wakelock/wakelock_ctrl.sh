#!/bin/bash
if [ $1 ]
then
    CMD=$1
    LOCKNAME=$2
    adb root;
    echo "$CMD $LOCKNAME"
    if [ $2 ] && [ "$CMD" = "lock" ]; then
        adb shell "echo $LOCKNAME > /sys/power/wake_lock"
    elif [ $2 ] && [ "$CMD"  = "release" ]; then
        adb shell "echo $LOCKNAME > /sys/power/wake_unlock"
    elif [ "$CMD"  = "ls" ]; then
        adb shell "cat /proc/wakelocks"
    fi
else
    echo "Usage: wakelock_ctrl <lock|release|ls> <wakelock_name>"
fi
