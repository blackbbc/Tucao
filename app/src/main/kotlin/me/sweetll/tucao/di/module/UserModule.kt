package me.sweetll.tucao.di.module

import dagger.Module
import dagger.Provides
import me.sweetll.tucao.di.scope.UserScope
import me.sweetll.tucao.model.other.User

@Module
class UserModule {

    @UserScope
    @Provides
    fun provideUser() = User.load()

}
