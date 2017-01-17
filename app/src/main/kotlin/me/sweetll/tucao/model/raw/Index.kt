package me.sweetll.tucao.model.raw

import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Result

data class Index(val banners: List<Banner>,
                 val recommends: List<Pair<Channel,List<Result>>>)
