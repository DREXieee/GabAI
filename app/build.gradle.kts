import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.roborazzi)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}

android {
    namespace = "com.gabai"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.gabai"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // gabai-backend base URL. Defaults to the emulator's loopback alias for the
        // backend running on the host machine (`npm run dev` in gabai-backend); override
        // in local.properties (API_BASE_URL=https://...) once it's deployed.
        buildConfigField(
            "String",
            "API_BASE_URL",
            "\"${localProperties.getProperty("API_BASE_URL", "http://10.0.2.2:8080/")}\""
        )

        // Google Cloud Console -> APIs & Services -> Credentials -> the OAuth 2.0
        // "Web application" client ID (NOT the Android client ID) -- this is what
        // Credential Manager's GetGoogleIdOption.setServerClientId() needs, and
        // it's also what must be entered as the Client ID in Supabase Dashboard ->
        // Authentication -> Providers -> Google. Set in local.properties
        // (GOOGLE_WEB_CLIENT_ID=...); Google Sign-In is disabled without it.
        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProperties.getProperty("GOOGLE_WEB_CLIENT_ID", "")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    // AGP 9's built-in Kotlin takes its jvmTarget from targetCompatibility.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        // java.time below API 26, for the booking calendar
        isCoreLibraryDesugaringEnabled = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    // Robolectric needs the merged resources for the Roborazzi screenshot tests,
    // and its Android 16 (API 36) runtime reflects into jdk.internal.access,
    // which JDK 17+ only allows when opened explicitly.
    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.all { it.jvmArgs("--add-opens=java.base/jdk.internal.access=ALL-UNNAMED") }
    }
}

// Reference images live in the repo: `./gradlew recordRoborazziDebug` rewrites
// them, `./gradlew verifyRoborazziDebug` fails on any visual difference.
roborazzi {
    outputDir.set(file("src/test/screenshots"))
}

dependencies {
    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // Jetpack Compose BOM (Bill of Materials - manages versions)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    // Skeleton placeholders while lists load (see ui/common/SkeletonList.kt)
    implementation(libs.compose.shimmer)

    // Renders markdown in GabAI's chat replies (see ChatBubble)
    implementation(libs.markdown.renderer.m3)

    // Month calendar for picking a consultation date
    implementation(libs.calendar.compose)
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    // Paced breathing orb on the breathing exercises (see BreathingGuide)
    implementation(libs.lottie.compose)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // DataStore for storing user preferences
    implementation(libs.androidx.datastore.preferences)

    // Google Sign-In via Credential Manager (current recommended API, replacing
    // the deprecated GoogleSignInClient/GoogleSignInOptions)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Retrofit for API calls
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging.interceptor)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.play.services)

    // Maps (open-source, no API key/billing) + one-shot location fetch for
    // the Nearest Clinics feature
    implementation(libs.maplibre.android.sdk)
    implementation(libs.play.services.location)

    implementation(libs.gson)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}