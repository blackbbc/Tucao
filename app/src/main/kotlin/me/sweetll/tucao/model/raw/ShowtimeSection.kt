package me.sweetll.tucao.model.raw

import com.chad.library.adapter.base.entity.SectionEntity
import me.sweetll.tucao.model.json.Result

class ShowtimeSection: SectionEntity<Result> {
    constructor(header: String, isHeader: Boolean = true): super(isHeader, header)

    constructor(result: Result): super(result)
}
