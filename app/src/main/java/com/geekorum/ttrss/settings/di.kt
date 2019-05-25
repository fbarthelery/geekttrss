package com.geekorum.ttrss.settings

import com.geekorum.ttrss.settings.manage_modules.ManageFeaturesModule
import dagger.Module

@Module(includes = [SettingsInitializerModule::class, ManageFeaturesModule::class])
class SettingsModule
