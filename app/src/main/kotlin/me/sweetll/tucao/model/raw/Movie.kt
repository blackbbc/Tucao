package me.sweetll.tucao.model.raw

import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.model.json.Channel

data class Movie(val banners: List<Banner>,
                 val recommends: List<Pair<Channel,List<Video>>>)
