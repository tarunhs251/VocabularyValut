# Add project specific ProGuard rules here.
# Retrofit
-keepattributes Signature
-keepattributes Exceptions
-keep class retrofit2.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.example.vocabvault.data.remote.model.** { *; }
