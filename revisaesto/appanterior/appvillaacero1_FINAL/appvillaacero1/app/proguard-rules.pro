# Keep serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class cl.villaacero.uniformes.**$$serializer { *; }
-keepclassmembers class cl.villaacero.uniformes.** {
    *** Companion;
}
-keepclasseswithmembers class cl.villaacero.uniformes.** {
    kotlinx.serialization.KSerializer serializer(...);
}
