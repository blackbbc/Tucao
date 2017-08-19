package me.sweetll.tucao.model.raw

import com.chad.library.adapter.base.entity.SectionEntity
import me.sweetll.tucao.model.json.Video

class ShowtimeSection: SectionEntity<Video> {
    constructor(header: String, isHeader: Boolean = true): super(isHeader, header)

    constructor(video: Video): super(video)
}
