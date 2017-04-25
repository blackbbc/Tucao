package me.sweetll.tucao.rxdownload.entity

data class DownloadMission(
        val bean: DownloadBean = DownloadBean(),
        var pause: Boolean = true
)