package com.fastertable.fastertable.common.dependencyinjection.app

import com.fastertable.fastertable.api.FastertableApi
import com.fastertable.fastertable.api.UrlProvider
import com.fastertable.fastertable.common.dependencyinjection.Retrofit1
import com.fastertable.fastertable.utils.NullOnEmptyConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @AppScope
    @Retrofit1
    fun retrofit1(urlProvider: UrlProvider): Retrofit {
        return Retrofit.Builder()
            .baseUrl(urlProvider.getBaseUrl1())
            .addConverterFactory( NullOnEmptyConverterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }


    @AppScope
    @Provides
    fun urlProvider() = UrlProvider()

    @Provides
    @AppScope
    fun fastertableApi(@Retrofit1 retrofit: Retrofit) = retrofit.create(FastertableApi::class.java)
}