package com.example.wifiwalkietalkie.di

import android.content.Context
import com.example.wifiwalkietalkie.controller.WifiWalkieTalkieController
import com.example.wifiwalkietalkie.data.WalkieTakieController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule{
    @Provides
    @Singleton
    fun provideBtController(@ApplicationContext context: Context):WifiWalkieTalkieController{
        return WalkieTakieController(context)
    }
}