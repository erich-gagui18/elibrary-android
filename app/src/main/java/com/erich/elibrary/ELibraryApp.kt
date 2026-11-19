package com.erich.elibrary

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.util.DebugLogger
import okhttp3.Dispatcher
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response

class ELibraryApp : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        Session.init(this)
    }

    /**
     * Image loader for book covers.
     * - Identifies the app with its own User-Agent.
     * - Limits parallel requests so the cover server is not flooded.
     * - Retries when the server is busy (503, 502, 429).
     * - Logs failures to Logcat under the tag "Coil".
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .dispatcher(Dispatcher().apply {
                        maxRequests = 8
                        maxRequestsPerHost = 3
                    })
                    .addInterceptor { chain ->
                        chain.proceed(
                            chain.request().newBuilder()
                                .header("User-Agent", "ELibraryApp/1.0 (student project, Android)")
                                .build()
                        )
                    }
                    .addInterceptor(::retryWhenBusy)
                    .build()
            }
            .logger(DebugLogger())
            .crossfade(true)
            .build()

    private fun retryWhenBusy(chain: Interceptor.Chain): Response {
        var attempt = 0
        while (true) {
            val response = chain.proceed(chain.request())
            val busy = response.code == 503 || response.code == 502 || response.code == 429
            if (!busy || attempt >= 3 || chain.call().isCanceled()) return response

            // Wait as long as the server asks (up to 5 s), otherwise 0.6 s, 1.2 s, 2.4 s.
            val waitMs = response.header("Retry-After")?.toLongOrNull()
                ?.coerceIn(1L, 5L)?.times(1000L) ?: (600L shl attempt)
            response.close()
            Thread.sleep(waitMs)
            attempt++
        }
    }
}