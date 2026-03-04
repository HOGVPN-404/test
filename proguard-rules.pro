# Keep kotlinx serialization metadata used at runtime
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}
