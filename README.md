# Looching2026 — Spirit of the Looch

**jlooch** is a generative drone-music program written in Java by
**Brad Garton** (Columbia University Computer Music Center) in 2001, using
Phil Burk's [JSyn](http://www.softsynth.com/jsyn/) synthesis library.
It layers four randomly triggered voices — drones, sequences, warbles and
noises — that you balance with sliders.

This repo gets the 2001 code building and running on a modern Windows machine.
Brad's original notes are in [`README`](README) and the original Linux
[`Makefile`](Makefile) is kept for reference.

## Running it (Windows)

Requires **JDK 21**. Newer JDKs (26+) removed the Applet API this code uses.

```powershell
.\build.ps1        # compile into .\classes
.\build.ps1 go     # compile, then run
```

`build.ps1` uses `JAVA_HOME` if it is set, otherwise the first `jdk-21*`
folder in `%USERPROFILE%\.jdks`.

Click **drono...** to start. Push a slider up to make that voice play more
often, and use the **+** buttons to switch individual voices on or off.

## Changes from the 2001 original

- Builds against the pure-Java JSyn in `lib/`. The old `com.softsynth.jsyn`
  API (`jsyn-old-api-20161206.jar`) runs on top of the modern engine
  (`jsyn-20171016.jar`), so no native plugin is needed.
- `jlooch.java`: a larger 440×600 window with manual layout, so all four
  sliders are the same size. Also adds clear slider labels, bigger buttons,
  and a window/taskbar icon.
- `looch.ico`: a Windows icon made from `loochicon.gif`, for desktop shortcuts.

## Credits and licensing

- jlooch © 2001 Brad Garton. More information: http://music.columbia.edu/~brad/jlooch
  (the original code was distributed without a license statement).
- JSyn © Phil Burk, SoftSynth.com. Licensed under the Apache License 2.0.
  See [`lib/LICENSE-JSyn.txt`](lib/LICENSE-JSyn.txt).
