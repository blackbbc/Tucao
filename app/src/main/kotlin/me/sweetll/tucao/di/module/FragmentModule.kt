package me.sweetll.tucao.di.module

import dagger.Module
import dagger.android.ContributesAndroidInjector
import me.sweetll.tucao.business.channel.fragment.ChannelDetailFragment
import me.sweetll.tucao.business.rank.fragment.RankDetailFragment
import me.sweetll.tucao.business.video.fragment.VideoCommentsFragment

@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeChannelDetailFragment(): ChannelDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeVideoCommentsFragment(): VideoCommentsFragment

    @ContributesAndroidInjector
    abstract fun contributeRankDetailFragment(): RankDetailFragment

}