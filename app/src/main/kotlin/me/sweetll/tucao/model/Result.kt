package me.sweetll.tucao.model

data class Result(val title: String,
                  val play: Int,
                  val mukio: Int,
                  val creat: String,
                  val thumb: String,
                  val typename: String,
                  val typeid: Int,
                  val description: String,
                  val user: String,
                  val userid: String,
                  val keywords: String,
                  val part: Int,
                  val video: MutableList<Video>)
