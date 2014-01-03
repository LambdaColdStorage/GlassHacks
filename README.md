GlassHacks
==========

Related source code for the 30C3 talk: Glass Hacks.

Video: http://www.youtube.com/watch?v=PnXGb7RHXWQ

## Railgun

Railgun is a Google Glass launcher that lets you run your third-party
applications on Google Glass.

Note that XE12 breaks the current method of utilizing an alternative launcher.
This means that you should either:

	1) Not upgrade to XE12.
	2) Install an alternative build of android like Cyanogenmod.

You can still sideload applications normall, you just won't be able to use
a launcher to find them.

## MindCap

MindCap is an always-on camera that lets you take pictures with the video
turned off. To learn more about how to keep the screen off when recording,
see wakelock_ctrl.sh:

	./MindCap/wakelock/wakelock_ctrl.sh lock <lockname>
		Create a new wakelock by writing to /sys/power/wake_lock

	./MindCap/wakelock/wakelock_ctrl.sh release <lockname>
		Release a wakelock by writing to /sys/power/wake_unlock

	./MindCap/wakelock/wakelock_ctrl.sh ls <lockname>
		List all active system wakelocks

## scripts

Misc. scripts created in development.

### mindread.sh

Mindread is a script for managing your always-on camera.

Usage:
	./scripts/mindread.sh start
		Start capturing on your device.
	./scripts/mindread.sh stop
		Stop capturing on your device.
	./scripts/mindread.sh clear
		Wipe files from your Recap.
	./scripts/mindread.sh ls
		Show all images in the Recap folder.
	./scripts/mindread.sh pull outdir
		Pull files from your Recap.
	./scripts/mindread.sh install
		Install MindCap on your device.
