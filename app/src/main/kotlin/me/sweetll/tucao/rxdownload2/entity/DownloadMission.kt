package me.sweetll.tucao.rxdownload2.entity

data class DownloadMission(
        val bean: DownloadBean = DownloadBean(),
        var pause: Boolean = false
)