#!/bin/sh
#
# Start, slurp, and wipe your Recap.
#
PRODNAME=Recap
BASEDIR=$(dirname $0)
MINDCAP_FOLDER=/mnt/sdcard/MindCap
APP_PACKAGE=com.lambdal.mindcap
MINDCAP_BIN=$BASEDIR/MindCap.apk

usage() {
	cat 1>&2 <<EOF
Usage:
	$0 start
		Start capturing on your device.
	$0 stop
		Stop capturing on your device.
	$0 clear
		Wipe files from your $PRODNAME.
	$0 ls
		Show all images in the $PRODNAME folder.
	$0 pull outdir
		Pull files from your $PRODNAME.
	$0 install
		Install MindCap on your device.

EOF
}

clear_mind() {
	echo 1>&2 Clearing your mind.
	adb shell rm $MINDCAP_FOLDER/*
}
read_mind() {
	OUT_FOLDER=$1
	num_imgs=`adb shell ls -l $MINDCAP_FOLDER | wc -l`
	echo 1>&2 "Reading your mind. I see $num_imgs image(s)."
	mkdir -p $OUT_FOLDER
	adb pull $MINDCAP_FOLDER $OUT_FOLDER
	echo 1>&2 "Should have pulled $num_imgs image(s)."
}

case "$1" in 
	install)
		adb install -r $MINDCAP_BIN
		;;
	start|run)
		adb shell am start -a android.intent.action.MAIN -n $APP_PACKAGE/.MindCap
		;;
	stop|kill)
		adb shell su -c "kill -9 $(adb shell ps | grep com.lambdal.mindcap | awk '{ print $2 }')"
		;;
	ls)
		adb shell ls $MINDCAP_FOLDER
		;;
	"clear")
		clear_mind;
		;;
	"pull")
		read_mind $2
		;;
	*)
		usage;
		;;
esac
