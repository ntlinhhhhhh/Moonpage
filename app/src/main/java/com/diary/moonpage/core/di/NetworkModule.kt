package com.diary.moonpage.core.di

import android.content.Context
import com.diary.moonpage.core.network.AuthInterceptor
import com.diary.moonpage.data.remote.api.*
import com.diary.moonpage.data.remote.dto.stats.MusicSummaryDto
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.Inet4Address
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .registerTypeAdapter(
                MusicSummaryDto::class.java,
                JsonDeserializer<MusicSummaryDto> { json, _, _ ->
                    if (json == null || json.isJsonNull) {
                        MusicSummaryDto(songTitle = "", artistName = "", albumArtUrl = null, occurrence = 0)
                    } else if (json.isJsonPrimitive) {
                        MusicSummaryDto(songTitle = json.asString, artistName = "", albumArtUrl = null, occurrence = 1)
                    } else {
                        val obj = json.asJsonObject
                        fun stringValue(vararg names: String): String? {
                            return names.asSequence()
                                .mapNotNull { name ->
                                    obj.get(name)
                                        ?.takeUnless { element -> element.isJsonNull }
                                        ?.asString
                                }
                                .firstOrNull()
                        }
                        fun intValue(vararg names: String): Int? {
                            return names.asSequence()
                                .mapNotNull { name ->
                                    obj.get(name)
                                        ?.takeUnless { element -> element.isJsonNull }
                                        ?.let { element -> runCatching { element.asInt }.getOrNull() }
                                }
                                .firstOrNull()
                        }

                        MusicSummaryDto(
                            songTitle = stringValue("songTitle", "title", "name", "song").orEmpty(),
                            artistName = stringValue("artistName", "artist", "artist_name").orEmpty(),
                            albumArtUrl = stringValue("albumArtUrl", "albumArt", "imageUrl", "image_url"),
                            occurrence = intValue("occurrence", "count", "total") ?: 1
                        )
                    }
                }
            )
            .create()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        @ApplicationContext context: Context,
        authInterceptor: AuthInterceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val cacheSize = 50 * 1024 * 1024L // 50MB
        val cache = Cache(File(context.cacheDir, "http_cache"), cacheSize)

        return OkHttpClient.Builder()
            .cache(cache)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .dns(object : Dns {
                override fun lookup(hostname: String): List<InetAddress> {
                    return Dns.SYSTEM.lookup(hostname).sortedBy {
                        if (it is Inet4Address) 0 else 1 
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://hieu-wikipedia.io.vn/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideThemeApi(retrofit: Retrofit): ThemeApi {
        return retrofit.create(ThemeApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideMomentApi(retrofit: Retrofit): MomentApi {
        return retrofit.create(MomentApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDailyLogApi(retrofit: Retrofit): DailyLogApi {
        return retrofit.create(DailyLogApi::class.java)
    }

    @Provides
    @Singleton
    fun provideActivityApi(retrofit: Retrofit): ActivityApi {
        return retrofit.create(ActivityApi::class.java)
    }

    @Provides
    @Singleton
    fun provideStatisticsApi(retrofit: Retrofit): StatisticsApi {
        return retrofit.create(StatisticsApi::class.java)
    }

    @Provides
    @Singleton
    fun provideSpotifyApi(retrofit: Retrofit): SpotifyApi {
        return retrofit.create(SpotifyApi::class.java)
    }

    @Provides
    @Singleton
    fun provideWeatherApi(retrofit: Retrofit): WeatherApi {
        return retrofit.create(WeatherApi::class.java)
    }

    @Provides
    @Singleton
    fun provideNotificationApi(retrofit: Retrofit): NotificationApi {
        return retrofit.create(NotificationApi::class.java)
    }
}
