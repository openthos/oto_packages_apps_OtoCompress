#!/bin/bash
#Please change "ndkROOT" value
ndkROOT=/root/xly/ly/android-ndk-r14b

export PATH=$ndkROOT:$PATH
export PATH=$ndkROOT/toolchains/x86-4.9/prebuilt/linux-x86_64/bin:$PATH

ndk-build -j8
