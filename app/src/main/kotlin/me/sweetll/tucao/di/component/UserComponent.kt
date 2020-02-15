package me.sweetll.tucao.di.component

import dagger.Subcomponent
import dagger.android.AndroidInjectionModule
import me.sweetll.tucao.AppApplication
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.MainActivity
import me.sweetll.tucao.business.video.fragment.VideoCommentsFragment
import me.sweetll.tucao.di.module.ActivityModule
import me.sweetll.tucao.di.module.FragmentModule
import me.sweetll.tucao.di.module.ServiceModule
import me.sweetll.tucao.di.module.UserModule
import me.sweetll.tucao.di.scope.UserScope

@UserScope
@Subcomponent(modules = [UserModule::class, ActivityModule::class, FragmentModule::class, ServiceModule::class])
interface UserComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): UserComponent
    }

    fun inject(application: AppApplication)
    fun inject(baseViewModel: BaseViewModel)
}
