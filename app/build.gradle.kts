import com.android.build.gradle.internal.api.ApkVariantOutputImpl
import java.time.Instant

plugins {
    alias(libs.plugins.self.application)
    alias(libs.plugins.self.compose)
    alias(libs.plugins.self.room)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.licensee)
}

val baseVersionName = "0.0.1"
val devVersion = exec("git tag --points-at HEAD").isEmpty()
val shaSuffix = gitCommitSha.let { ".${it.substring(0, 7)}" }
val devSuffix = if (devVersion) ".dev" else ""

android {
    namespace = "dev.sanmer.template"

    defaultConfig {
        applicationId = namespace
        versionName = "${baseVersionName}${shaSuffix}${devSuffix}"
        versionCode = gitCommitCount

        ndk.abiFilters += listOf("arm64-v8a", "x86_64")
    }

    androidResources {
        generateLocaleConfig = true
        localeFilters += listOf("en")
    }

    val releaseSigning = if (hasReleaseKeyStore) {
        signingConfigs.create("release") {
            storeFile = releaseKeyStore
            storePassword = releaseKeyStorePassword
            keyAlias = releaseKeyAlias
            keyPassword = releaseKeyPassword
            enableV3Signing = true
            enableV4Signing = true
        }
    } else {
        signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        all {
            signingConfig = releaseSigning
            buildConfigField("boolean", "DEV_VERSION", devVersion.toString())
            buildConfigField("long", "BUILD_TIME", Instant.now().toEpochMilli().toString())
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging.resources.excludes += setOf(
        "META-INF/**",
        "kotlin/**",
        "**.bin",
        "**.properties"
    )

    dependenciesInfo.includeInApk = false

    applicationVariants.configureEach {
        outputs.configureEach {
            if (this is ApkVariantOutputImpl) {
                outputFileName = "Template-${versionName}-${versionCode}-${name}.apk"
            }
        }
    }
}

licensee {
    bundleAndroidAsset = true
    androidAssetReportPath = "artifacts.json"
    allow("Apache-2.0")
}

dependencies {
implementation(platform("androidx.compose:compose-bom:2025.12.00"))
implementation("androidx.activity:activity-compose:1.12.2")
implementation("androidx.compose.ui:ui")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.ui:ui-tooling-preview")
implementation("androidx.navigation:navigation-compose:2.9.6")
debugImplementation("androidx.compose.ui:ui-tooling")
}
