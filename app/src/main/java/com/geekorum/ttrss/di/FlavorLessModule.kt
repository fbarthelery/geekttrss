package com.geekorum.ttrss.di

import com.geekorum.geekdroid.dagger.AndroidFrameworkModule
import com.geekorum.ttrss.accounts.AndroidTinyrssAccountManagerModule
import com.geekorum.ttrss.data.ArticlesDatabaseModule
import com.geekorum.ttrss.logging.LoggingModule
import dagger.Module

/**
 * Base module who includes all the modules common to all flavor of the applicatio
 */
@Module(includes = [
    AndroidFrameworkModule::class,
    AndroidBindingsModule::class,
    ViewModelsModule::class,
    NetworkModule::class,
    ArticlesDatabaseModule::class,
    LoggingModule::class,
    AndroidTinyrssAccountManagerModule::class,
    com.geekorum.ttrss.article_details.ActivitiesInjectorModule::class,
    com.geekorum.ttrss.articles_list.ActivitiesInjectorModule::class,
    com.geekorum.ttrss.sync.ServiceInjectorModule::class,
    com.geekorum.ttrss.accounts.ServicesInjectorModule::class,
    com.geekorum.ttrss.add_feed.AndroidInjectorsModule::class,
    com.geekorum.ttrss.providers.AndroidInjectorsModule::class
])
abstract class FlavorLessModule
