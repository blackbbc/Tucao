package me.sweetll.tucao.util

import me.sweetll.tucao.model.json.Channel
import me.sweetll.tucao.model.json.Video
import me.sweetll.tucao.model.raw.Banner
import org.jsoup.nodes.Element

val HID_PATTERN = "/play/h([0-9]+)/".toRegex()
val TID_PATTERN = "/list/([0-9]+)/".toRegex()

fun parseBanner(slides: List<Element>): List<Banner> {
    val banners = slides.map {
        // <div class="slide"><a href="https://www.tucao.cool/play/h4100629/" target="_blank" class="i"><img
        //         src="https://www.tucao.cool/uploadfile/2024/0211/thumb_300_487_20240211125530340.jpg"></a><a
        //         href="https://www.tucao.cool/play/h4100629/" target="_blank" class="t">
        //     <div>【2024日影】神推偶像登上武道馆我就死而无憾 剧场版【幻月】</div>
        //     <p> 绘里飘与自推的第一次相识，是在三年前的七夕祭典上，当时冈山当地的地下偶像团体ChamJam在那里表演。其中一位成员舞菜的表现让绘里飘产生 </p>
        // </a></div>
        val aElement = it.child(0)
        val linkUrl = aElement.attr("href")
        val imgElement = aElement.child(0)
        val imgUrl = imgElement.attr("src")
        val hid: String? = HID_PATTERN.find(linkUrl)?.groupValues?.get(1)
        Banner(imgUrl, linkUrl, hid)
    }
    return banners
}

fun parseChannelList(parent: Element): List<Pair<Channel, List<Video>>> {
    val result = mutableListOf<Pair<Channel, List<Video>>>()
    parent.children().forEachIndexed { index, element ->
        if (index % 2 == 0 && index + 1 < parent.childNodeSize()) {
            val title = element
            val list = parent.child(index + 1)
            if (title.className() == "title" && list.className().startsWith("list")) {
                val channelLinkUrl = title.child(1).attr("href")
                val tid: Int = TID_PATTERN.find(channelLinkUrl)!!.groupValues[1].toInt()
                val channel = Channel.find(tid)!!
                val videos = parseListVideo(list)
                result.add(channel to videos)
            }
        }
    }
    return result
}

fun parseListVideo(list: Element): List<Video> {
    val items = list.getElementsByClass("item")
    val videos = items.map {
        val aElement = it.child(0).child(0)
        val linkUrl = aElement.attr("href")
        val hid: String = HID_PATTERN.find(linkUrl)!!.groupValues[1]
        val style = aElement.child(0).attr("style")
        val thumb = style.substringAfter("url(").substringBeforeLast(')')
        val title = aElement.parent().child(1).text()
        val play = aElement.parent().child(2).child(0).text().replace(",", "").toInt()
        Video(hid = hid, title = title, play = play, thumb = thumb)
    }
    return videos
}