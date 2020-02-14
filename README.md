Primer Android
==============

Primer Android is a mobile Primecoin hot/cold wallet for Android with high security cold wallet mode.

## Build

Primer Android build is based on gradle build system.

```
# Build instructions for Ubuntu 18.04 LTS
# Requires Android Sdk and Java 1.8

$ export ANDROID_HOME="/home/ubuntu/Android/Sdk"
$ sudo apt install openjdk-8-jdk
$ export JAVA_HOME="/usr/lib/jvm/java-8-openjdk-amd64"
$ sudo apt install gradle
$ gradle --version
------------------------------------------------------------
Gradle 4.4.1
------------------------------------------------------------

Build time:   2012-12-21 00:00:00 UTC
Revision:     none

Groovy:       2.4.16
Ant:          Apache Ant(TM) version 1.10.5 compiled on March 28 2019
JVM:          1.8.0_242 (Private Build 25.242-b08)
OS:           Linux 5.3.0-28-generic amd64

$ gradle clean
$ gradle assembleDebug
```

## Features

* HOT and COLD wallet modes to ensure private key safety
* Communication between hot and cold wallet via QR Codes
* SPV primecoin p2p client
* Private password keyboard to avoid using third party keyboard for password

## Acknowledgements

Primer is based on [Bither](https://github.com/bither/bither-android), a Bitcoin modile wallet with cold wallet feature.

## Donation

Primer developers (XPM): ANTqRE6wpE2psbJvvMLJLixfCFms1zp61y

Bither developers (BTC): 1BitherUnNvB2NsfxMnbS35kS3DTPr7PW5

