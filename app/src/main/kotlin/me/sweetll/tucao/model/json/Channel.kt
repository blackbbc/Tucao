package me.sweetll.tucao.model.json

data class Channel(var id: Int, var name: String, var parentId: Int? = null) {
    companion object {
        private val CHANNELS = listOf(
                Channel(19, "动画"),
                Channel(28, "MAD·AMV·GMV", 19),
                Channel(6, "MMD·3D", 19),
                Channel(25, "原创·配音", 19),
                Channel(29, "综合·周边·其他", 19),
                Channel(20, "音乐"),
                Channel(7, "acg音乐", 20),
                Channel(31, "翻唱·原唱", 20),
                Channel(37, "舞蹈", 20),
                Channel(30, "VOCALOID·UTAU", 20),
                Channel(40, "演奏·乐器", 20),
                Channel(88, "op·ed·ost·角色歌", 20),
                Channel(52, "Live·声优相关", 20),
                Channel(21, "游戏"),
                Channel(8, "游戏影像", 21),
                Channel(34, "网络游戏", 21),
                Channel(44, "单机游戏", 21),
                Channel(33, "电子竞技", 21),
                Channel(42, "家机·掌机·手机", 21),
                Channel(22, "三次元"),
                Channel(9, "喜闻乐见", 22),
                Channel(32, "娱乐鬼畜", 22),
                Channel(57, "科技·数码", 22),
                Channel(61, "运动体育", 22),
                Channel(65, "军事情报", 22),
                Channel(15, "宠物·猫·狗", 22),
                Channel(23, "影剧"),
                Channel(39, "电视剧", 23),
                Channel(38, "电影", 23),
                Channel(16, "综艺娱乐", 23),
                Channel(27, "完结剧集", 23),
                Channel(24, "新番"),
                Channel(11, "连载新番", 24),
                Channel(43, "天朝出品", 24),
                Channel(26, "OAD·OVA·剧场版", 24),
                Channel(10, "完结番组", 24)
        )

        fun find(tid: Int): Channel? = CHANNELS.find { it.id == tid }

        fun findSiblingChannels(tid: Int) = CHANNELS.filter { tid == it.id || tid == it.parentId }

        fun findAllParentChannels() = CHANNELS.filter { it.parentId == null }
    }

    fun getValidParentId(): Int = if (parentId != null) parentId!! else id
}
