# Vela Recorder — R8/ProGuard rules
# SPDX-License-Identifier: Apache-2.0

# Koin
-keep class org.koin.** { *; }
# Room generated constructors
-keep class * extends androidx.room.RoomDatabase { <init>(); }
