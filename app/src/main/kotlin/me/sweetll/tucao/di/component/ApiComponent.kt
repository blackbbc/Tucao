package me.sweetll.tucao.di.component

import dagger.Subcomponent
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.di.module.ApiModule
import me.sweetll.tucao.di.scope.ApplicationScope

@ApplicationScope
@Subcomponent(modules = arrayOf(ApiModule::class))
interface ApiComponent {
    fun inject(baseViewModel: BaseViewModel)
}
