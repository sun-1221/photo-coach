plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.photocoach.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.photocoach.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        mapOf("RESEARCH_CONDITION" to "NONE", "RESEARCH_SCENE" to "WINDOW", "RESEARCH_CONFIG_ID" to "").forEach { (key, default) ->
            val value = providers.gradleProperty(key).orElse(default).get()
            require(value.matches(Regex("[A-Za-z0-9_-]{0,64}"))) { "Invalid research configuration: $key" }
            if (key == "RESEARCH_CONDITION") require(value in setOf("NONE", "STATIC", "DYNAMIC"))
            if (key == "RESEARCH_SCENE") require(value in setOf("WINDOW", "SCENERY", "BACKLIGHT"))
            if (key == "RESEARCH_CONFIG_ID" && providers.gradleProperty("RESEARCH_CONDITION").orElse("NONE").get() != "NONE") require(value.isNotBlank())
            buildConfigField("String", key, "\"$value\"")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(project(":coach"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.exifinterface)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.tooling.preview)
    implementation(libs.compose.material3)
    implementation(libs.compose.material.icons)
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    implementation(libs.camerax.video)
    implementation(libs.camerax.extensions)
    implementation(libs.camerax.mlkit)
    implementation(libs.mlkit.face)
    implementation(libs.mlkit.pose)
    implementation(libs.kotlinx.serialization.json)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// Verify the merger output, including transitive manifests; source XML alone cannot prove permissions.
for (variant in listOf("Debug", "Release")) {
    val flavor = variant.lowercase()
    val verify = tasks.register("verify${variant}ManifestPolicy") {
        dependsOn("process${variant}Manifest")
        val merged = layout.buildDirectory.file("intermediates/merged_manifests/$flavor/process${variant}Manifest/AndroidManifest.xml")
        inputs.file(merged)
        doLast {
            val parser = javax.xml.parsers.DocumentBuilderFactory.newInstance().apply { isNamespaceAware = true }
            val doc = parser.newDocumentBuilder().parse(merged.get().asFile)
            val ns = "http://schemas.android.com/apk/res/android"
            fun elements(tag: String) = doc.getElementsByTagName(tag).let { nodes ->
                (0 until nodes.length).map { nodes.item(it) as org.w3c.dom.Element }
            }
            fun names(tag: String) = elements(tag).map { it.getAttributeNS(ns, "name") }.toSet()
            val requested = names("uses-permission")
            val signature = "com.photocoach.app.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION"
            check(requested == setOf("android.permission.CAMERA", signature)) { "Unexpected merged permissions: $requested" }
            check(elements("permission").single { it.getAttributeNS(ns, "name") == signature }
                .getAttributeNS(ns, "protectionLevel") == "signature")
            val removed = setOf("com.google.android.datatransport.runtime.backends.TransportBackendDiscovery",
                "com.google.android.datatransport.runtime.scheduling.jobscheduling.JobInfoSchedulerService",
                "com.google.android.datatransport.runtime.scheduling.jobscheduling.AlarmManagerSchedulerBroadcastReceiver",
                "androidx.work.impl.foreground.SystemForegroundService", "androidx.work.impl.background.systemalarm.SystemAlarmService",
                "androidx.work.impl.background.systemalarm.RescheduleReceiver")
            val components = names("service") + names("receiver") + names("provider") + names("meta-data")
            check((components intersect removed).isEmpty()) { "Removed background entries reappeared" }
            check(components.containsAll(setOf("androidx.work.WorkManagerInitializer",
                "androidx.work.impl.background.systemjob.SystemJobService", "androidx.startup.InitializationProvider",
                "com.google.mlkit.common.internal.MlKitInitProvider", "com.google.mlkit.common.internal.MlKitComponentDiscoveryService",
                "com.google.mlkit.acceleration.internal.MlKitRemoteWorkerService", "androidx.camera.core.impl.MetadataHolderService")))
            check("android.intent.action.TTS_SERVICE" in names("action"))
            logger.lifecycle("$variant merged manifest policy verified: CAMERA and app signature protection; required ML Kit/CameraX/TTS entries retained")
        }
    }
    tasks.matching { it.name == "test${variant}UnitTest" || it.name == "assemble$variant" }.configureEach { dependsOn(verify) }
}
