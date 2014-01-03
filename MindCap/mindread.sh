#!/bin/sh
MINDCAP_FOLDER=/mnt/sdcard/MindCap
OUT_FOLDER=out
MINDCAP_BIN=bin/MindCap.apk

clear_mind() {
	echo 1>&2 Clearing your mind.
	adb shell rm $MINDCAP_FOLDER/*
}
read_mind() {
	num_imgs=`adb shell ls -l $MINDCAP_FOLDER | wc -l`
	echo 1>&2 "Reading your mind. I see $num_imgs image(s)."
	mkdir -p $OUT_FOLDER
	adb pull $MINDCAP_FOLDER $OUT_FOLDER
	echo 1>&2 "Should have pulled $num_imgs image(s)."
}

case "$1" in 
	"clear")
		clear_mind;
		;;
	"animate")
		OUTFILE=mindcap_`date | sed 's/ /-/g'`.gif
		echo "Animating your mind (requires imagemagick) -> $OUT_FOLDER/$OUTFILE"
		convert $OUT_FOLDER/*.jpg $OUT_FOLDER/$OUTFILE
		;;
	"run")
		adb install -r $MINDCAP_BIN
		adb shell am start -a android.intent.action.MAIN -n com.lambdal.mindcap/.MindCap
		;;
	*)
		read_mind
esac
echo DONE
