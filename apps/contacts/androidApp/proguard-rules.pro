# Vela Contacts — R8/ProGuard rules
# SPDX-License-Identifier: Apache-2.0

# Koin
-keep class org.koin.** { *; }
# Kotlinx Serialization (type-safe navigation routes)
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
