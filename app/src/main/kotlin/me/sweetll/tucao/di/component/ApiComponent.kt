package me.sweetll.tucao.di.component

import dagger.Subcomponent
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.business.channel.fragment.ChannelDetailFragment
import me.sweetll.tucao.business.rank.fragment.RankDetailFragment
import me.sweetll.tucao.business.splash.SplashActivity
import me.sweetll.tucao.di.module.ApiModule
import me.sweetll.tucao.di.module.UserModule
import me.sweetll.tucao.di.scope.ApplicationScope
import me.sweetll.tucao.extension.DownloadHelpers

@ApplicationScope
@Subcomponent(modules = [ApiModule::class])
interface ApiComponent {

    fun userComponent(): UserComponent.Factory

    @Subcomponent.Factory
    interface Factory {
        fun create(): ApiComponent
    }

    fun inject(serviceInstance: DownloadHelpers.ServiceInstance)
}
