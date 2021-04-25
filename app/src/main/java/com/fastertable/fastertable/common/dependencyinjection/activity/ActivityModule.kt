package com.fastertable.fastertable.common.dependencyinjection.activity

import android.app.Activity
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
class ActivityModule {

    companion object {
        @Provides
        fun layoutInflater(activity: Activity) = LayoutInflater.from(activity)

        @Provides
        fun appCompatActivity(activity: Activity) = activity as AppCompatActivity

        @Provides
        fun fragmentManager(activity: AppCompatActivity) = activity.supportFragmentManager
    }
}