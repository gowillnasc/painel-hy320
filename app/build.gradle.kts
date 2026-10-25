plugins { id("com.android.application") }
android {
    namespace = "com.hy320.painel"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.hy320.painel"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
