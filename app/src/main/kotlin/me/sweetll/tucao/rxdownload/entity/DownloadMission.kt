package me.sweetll.tucao.rxdownload.entity

data class DownloadMission(
        val bean: DownloadBean = DownloadBean(),
        @Volatile var pause: Boolean = true
)