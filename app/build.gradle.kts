import java.util.Properties

plugins {
    id("com.android.application")
}

val localProps = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProps.load(localPropertiesFile.inputStream())
}

val WEATHER_API_KEY: String = localProps.getProperty("WEATHER_API_KEY") ?: ""
val GOOGLE_MAP_API_KEY: String = localProps.getProperty("GOOGLE_MAP_API_KEY") ?: ""

android {
    namespace = "com.example.weathertune"
    compileSdk = 34

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.example.weathertune"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BuildConfig
        buildConfigField("String", "WEATHER_API_KEY", "\"$WEATHER_API_KEY\"")
        buildConfigField("String", "GOOGLE_MAP_API_KEY", "\"$GOOGLE_MAP_API_KEY\"")

        // Manifest Placeholders
        manifestPlaceholders["GOOGLE_MAP_API_KEY"] = GOOGLE_MAP_API_KEY
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("com.google.android.material:material:1.13.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.3.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.7.0")

    // Async HTTP
    implementation("com.loopj.android:android-async-http:1.4.11")

    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    // OkHttp Logging Interceptor
    implementation("com.squareup.okhttp3:logging-interceptor:4.10.0")

    // Google Location API
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Volley
    implementation("com.android.volley:volley:1.2.1")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Places API
    implementation("com.google.android.libraries.places:places:3.3.0")
}
