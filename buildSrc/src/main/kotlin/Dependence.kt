object Dependence {

    const val ton = "org.ton:ton-kotlin:0.2.15"
    const val fresco = "com.facebook.fresco:fresco:3.1.3"
    const val guava = "com.google.guava:guava:33.1.0-android"
    const val zxing = "com.google.zxing:core:3.5.3"

    object Analytics {
        const val aptabase = "com.github.aptabase:aptabase-kotlin:0.0.8"
    }

    object KotlinX {
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1"
        const val serialization = "org.jetbrains.kotlinx:kotlinx-serialization-core:1.7.0"
        const val serializationJSON = "org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.0"
        const val datetime = "org.jetbrains.kotlinx:kotlinx-datetime:0.6.0"
        const val io = "org.jetbrains.kotlinx:kotlinx-io-core:0.4.0"
    }

    object GooglePlay {
        const val cronet = "com.google.android.gms:play-services-cronet:18.0.1"
        const val cronetOkhttp = "com.google.net.cronet:cronet-okhttp:0.1.0"
        const val review = "com.google.android.play:review-ktx:2.0.1"
    }

    object UI {
        const val material = "com.google.android.material:material:1.12.0"
        const val flexbox = "com.google.android.flexbox:flexbox:3.0.0"
    }

    object Koin {
        const val core = "io.insert-koin:koin-android:3.5.6"
    }

    object ML {
        const val barcode = "com.google.mlkit:barcode-scanning:17.2.0"
    }

    object AndroidX {
        const val core = "androidx.core:core-ktx:1.13.1"
        const val shortcuts = "androidx.core:core-google-shortcuts:1.1.0"
        const val appCompat = "androidx.appcompat:appcompat:1.7.0"
        const val activity = "androidx.activity:activity-ktx:1.9.0"
        const val fragment = "androidx.fragment:fragment-ktx:1.8.0"
        const val recyclerView = "androidx.recyclerview:recyclerview:1.3.2"
        const val viewPager2 = "androidx.viewpager2:viewpager2:1.1.0"
        const val security = "androidx.security:security-crypto:1.0.0"
        const val workManager = "androidx.work:work-runtime-ktx:2.9.0"
        const val biometric = "androidx.biometric:biometric:1.1.0"
        const val annotation = "androidx.annotation:annotation:1.8.0"
        const val splashscreen = "androidx.core:core-splashscreen:1.0.1"
        const val constraintlayout = "androidx.constraintlayout:constraintlayout:2.1.4"

        const val swiperefreshlayout = "androidx.swiperefreshlayout:swiperefreshlayout:1.1.0"
        const val profileinstaller = "androidx.profileinstaller:profileinstaller:1.3.1"

        const val lifecycle = "androidx.lifecycle:lifecycle-runtime-ktx:2.8.2"
        const val lifecycleSavedState = "androidx.lifecycle:lifecycle-viewmodel-savedstate:2.8.2"

        object Camera {
            private const val version = "1.4.0-alpha04"

            const val base = "androidx.camera:camera-camera2:$version"
            const val core = "androidx.camera:camera-core:$version"
            const val lifecycle = "androidx.camera:camera-lifecycle:$version"
            const val view = "androidx.camera:camera-view:$version"
        }

        object Emoji {
            const val core = "androidx.emoji2:emoji2:1.4.0"
        }
    }

    object Squareup {
        const val okhttp = "com.squareup.okhttp3:okhttp:4.12.0"
        const val sse = "com.squareup.okhttp3:okhttp-sse:4.12.0"
        const val moshi = "com.squareup.moshi:moshi-kotlin:1.15.0"
        const val moshiAdapters = "com.squareup.moshi:moshi-adapters:1.15.0"
        const val okio = "com.squareup.okio:okio:3.9.0"
    }

    object Firebase {
        const val bom = "com.google.firebase:firebase-bom:33.1.0"
        const val analytics = "com.google.firebase:firebase-analytics-ktx"
        const val crashlytics = "com.google.firebase:firebase-crashlytics-ktx"
        const val messaging = "com.google.firebase:firebase-messaging-ktx"
    }

    object LedgerHQ {
        const val bleManager = "com.github.cosmostation:hw-transport-android-ble:0.0.8"
    }

    object Module {
        const val tonApi = ":tonapi"

        const val shimmer = ":ui:shimmer"
        const val blur = ":ui:blur"
    }

    object Lib {
        const val extensions = ":lib:extensions"
        const val network = ":lib:network"
        const val security = ":lib:security"
        const val qr = ":lib:qr"
        const val emoji = ":lib:emoji"
        const val blockchain = ":lib:blockchain"
        const val icu = ":lib:icu"
        const val sqlite = ":lib:sqlite"
        const val ledger = ":lib:ledger"
    }

    object UIKit {
        const val core = ":ui:uikit:core"
        const val color = ":ui:uikit:color"
        const val icon = ":ui:uikit:icon"
        const val list = ":ui:uikit:list"
    }

    object Wallet {
        const val localization = ":apps:wallet:localization"
        const val api = ":apps:wallet:api"
        const val app = ":apps:wallet:instance:app"

        object Data {
            const val core = ":apps:wallet:data:core"
            const val account = ":apps:wallet:data:account"
            const val settings = ":apps:wallet:data:settings"
            const val rates = ":apps:wallet:data:rates"
            const val tokens = ":apps:wallet:data:tokens"
            const val collectibles = ":apps:wallet:data:collectibles"
            const val events = ":apps:wallet:data:events"
            const val tonconnect = ":apps:wallet:data:tonconnect"
            const val push = ":apps:wallet:data:push"
            const val browser = ":apps:wallet:data:browser"
            const val backup = ":apps:wallet:data:backup"
            const val rn = ":apps:wallet:data:rn"
            const val passcode = ":apps:wallet:data:passcode"
            const val staking = ":apps:wallet:data:staking"
            const val purchase = ":apps:wallet:data:purchase"
        }

    }
}