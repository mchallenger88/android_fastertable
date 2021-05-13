package com.fastertable.fastertable.common.dependencyinjection.app

import com.fastertable.fastertable.api.FastertableApi
import com.fastertable.fastertable.api.UrlProvider
import com.fastertable.fastertable.common.dependencyinjection.Retrofit1
import com.fastertable.fastertable.utils.NullOnEmptyConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @AppScope
    @Retrofit1
    fun retrofit1(urlProvider: UrlProvider): Retrofit {
        val interceptor : HttpLoggingInterceptor = HttpLoggingInterceptor().apply {
            this.level = HttpLoggingInterceptor.Level.BODY
        }
        val client : OkHttpClient = OkHttpClient.Builder().apply {
            connectTimeout(5, TimeUnit.MINUTES)
            writeTimeout(5, TimeUnit.MINUTES)
            readTimeout(5, TimeUnit.MINUTES)
            this.addInterceptor(interceptor)
        }.build()

        return Retrofit.Builder()
            .baseUrl(urlProvider.getBaseUrl1())
            .addConverterFactory( NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }


    @AppScope
    @Provides
    fun urlProvider() = UrlProvider()

    @Provides
    @AppScope
    fun fastertableApi(@Retrofit1 retrofit: Retrofit) = retrofit.create(FastertableApi::class.java)

}