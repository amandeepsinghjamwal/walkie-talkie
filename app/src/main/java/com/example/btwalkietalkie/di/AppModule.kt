package com.example.btwalkietalkie.di

import android.content.Context
import com.example.btwalkietalkie.controller.BluetoothController
import com.example.btwalkietalkie.data.AndroidBtController
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
    fun provideBtController(@ApplicationContext context: Context):BluetoothController{
        return AndroidBtController(context)
    }
}