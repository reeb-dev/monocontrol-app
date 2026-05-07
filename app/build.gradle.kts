plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

// Aplica el plugin de Google Services SOLO si existe app/google-services.json.
// Esto permite que la app compile aunque todavía no hayas conectado Firebase.
// Una vez que descargues el archivo desde la consola de Firebase y lo pegues
// en app/google-services.json, Firebase Auth empieza a funcionar automáticamente.
if (file("google-services.json").exists()) {
    apply(plugin = "com.google.gms.google-services")
}

android {
    namespace = "com.reeb.controlmonotributoar"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.reeb.controlmonotributoar"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        create("release") {
            storeFile = file("keystore_nuevo_release.jks")
            storePassword = System.getenv("STORE_PASSWORD") ?: "MonoControl2026!@#"
            keyAlias = System.getenv("KEY_ALIAS") ?: "key0"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "MonoControl2026!@#"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true  // Habilitar ofuscación para producción
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/DEPENDENCIES",
                "META-INF/LICENSE",
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE",
                "META-INF/NOTICE.txt",
                "META-INF/*.kotlin_module",
                "META-INF/versions/9/OSGI-INF/MANIFEST.MF"
            )
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.kotlinx.coroutines.android)

    // Firebase Authentication. Las clases compilan siempre; en runtime sólo
    // funcionan si app/google-services.json fue agregado (ver SETUP_FIREBASE.md).
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.firebase.firestore.ktx)  // Sync de clientes online/offline
    implementation(libs.play.services.auth)  // Google Sign-In
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.androidx.work.runtime.ktx)  // Programación de envíos automáticos
    implementation(libs.apache.poi) {
        exclude(group = "commons-logging", module = "commons-logging")
        exclude(group = "org.apache.logging.log4j")
    }
    implementation(libs.apache.poi.ooxml) {
        exclude(group = "org.apache.xmlbeans", module = "xmlbeans")
        exclude(group = "com.github.virtuald", module = "curvesapi")
        exclude(group = "org.bouncycastle")
        exclude(group = "org.apache.santuario")
        exclude(group = "org.apache.logging.log4j")
        exclude(group = "com.fasterxml.jackson.core")
        exclude(group = "de.rototor.pdfbox")
        exclude(group = "org.apache.pdfbox")
    }
    implementation("org.apache.xmlbeans:xmlbeans:5.1.1") {
        exclude(group = "org.apache.logging.log4j")
    }
    ksp(libs.androidx.room.compiler)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}