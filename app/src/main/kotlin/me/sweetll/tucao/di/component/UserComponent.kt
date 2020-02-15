package me.sweetll.tucao.di.component

import dagger.Subcomponent
import me.sweetll.tucao.base.BaseActivity
import me.sweetll.tucao.base.BaseViewModel
import me.sweetll.tucao.business.home.MainActivity
import me.sweetll.tucao.business.video.fragment.VideoCommentsFragment
import me.sweetll.tucao.di.module.UserModule
import me.sweetll.tucao.di.scope.UserScope

@UserScope
@Subcomponent(modules = [UserModule::class])
interface UserComponent {

    @Subcomponent.Factory
    interface Factory {
        fun create(): UserComponent
    }

    fun inject(baseViewModel: BaseViewModel)
    fun inject(mainActivity: MainActivity)
    fun inject(videoCommentsFragment: VideoCommentsFragment)
}
