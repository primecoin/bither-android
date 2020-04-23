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
$ ./gradlew --version

------------------------------------------------------------
Gradle 4.9
------------------------------------------------------------

Build time:   2018-07-16 08:14:03 UTC
Revision:     efcf8c1cf533b03c70f394f270f46a174c738efc

Kotlin DSL:   0.18.4
Kotlin:       1.2.41
Groovy:       2.4.12
Ant:          Apache Ant(TM) version 1.9.11 compiled on March 23 2018
JVM:          1.8.0_242 (Private Build 25.242-b08)
OS:           Linux 5.3.0-28-generic amd64

$ ./gradlew clean
$ ./gradlew assembleDebug
```

## Release Build

```
$ ./gradlew assembleRelease \
    -Pandroid.injected.signing.store.file=/<pathTo>/primer.jks \
    -Pandroid.injected.signing.store.password=<storePassword> \
    -Pandroid.injected.signing.key.alias=primer_release \
    -Pandroid.injected.signing.key.password=<keyPassword>
```

## Features

* HOT and COLD wallet modes to ensure private key safety
* Communication between hot and cold wallet via QR Codes
* SPV primecoin p2p client
* Private password keyboard to avoid using third party keyboard for password

## Acknowledgements

Primer is based on [Bither](https://github.com/bither/bither-android), a multi-platform Bitcoin wallet with cold wallet feature.

## Donation

Primer developers (XPM): ANTqRE6wpE2psbJvvMLJLixfCFms1zp61y

Bither developers (BTC): 1BitherUnNvB2NsfxMnbS35kS3DTPr7PW5

