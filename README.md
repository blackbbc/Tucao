# Tucao

[![Travis](https://img.shields.io/travis/blackbbc/Tucao.svg)](https://travis-ci.org/blackbbc/Tucao)
[![Dependency Status](https://www.versioneye.com/user/projects/58f365330f9f35002b0bc851/badge.svg?style=flat-square)](https://www.versioneye.com/user/projects/58f365330f9f35002b0bc851)
[![GitHub release](https://img.shields.io/github/release/blackbbc/Tucao.svg)](https://github.com/blackbbc/Tucao/releases)
[![license](https://img.shields.io/github/license/blackbbc/Tucao.svg)](https://github.com/blackbbc/Tucao/blob/master/LICENSE)

![](http://www.tucao.tv/skin2013/banner.jpg)

## Features
- 首页六大模块，推荐、新番、影剧、游戏、动画、频道
- 全站排行榜，支持每日/每周排序
- 放映时间表，可以查看周一到周日新番的更新情况
- 频道列表，支持按照发布时间/播放量/弹幕排序
- 视频搜索，支持分频道搜索
- 视频查看，使用IjkPlayer播放视频，DanmakuFlameMaster播放弹幕，自动拼接多段视频（使用concat协议）
- 视频离线缓存，支持缓存弹幕
- 增量更新（[Google File By File Patch](https://github.com/andrewhayden/archive-patcher)）

## Screenshots
<a href="art/1.gif"><img src="art/1.gif" width="30%"/></a>
<a href="art/2.gif"><img src="art/2.gif" width="30%"/></a>

<a href="art/3.gif"><img src="art/3.gif" width="50%"/></a>

<a href="art/4.png"><img src="art/4.png" width="30%"/></a>
<a href="art/5.png"><img src="art/5.png" width="30%"/></a>
<a href="art/6.png"><img src="art/6.png" width="30%"/></a>

<a href="art/7.png"><img src="art/7.png" width="30%"/></a>
<a href="art/8.png"><img src="art/8.png" width="30%"/></a>
<a href="art/9.png"><img src="art/9.png" width="30%"/></a>

<a href="art/10.png"><img src="art/10.png" width="30%"/></a>
<a href="art/11.png"><img src="art/11.png" width="30%"/></a>
<a href="art/12.png"><img src="art/12.png" width="30%"/></a>

<a href="art/13.png"><img src="art/13.png" width="30%"/></a>
<a href="art/14.png"><img src="art/14.png" width="30%"/></a>
<a href="art/15.png"><img src="art/15.png" width="30%"/></a>

<a href="art/16.png"><img src="art/16.png" width="30%"/></a>
<a href="art/17.png"><img src="art/17.png" width="30%"/></a>
<a href="art/18.png"><img src="art/18.png" width="30%"/></a>

## TODO
- [x] 修复排行榜闪退问题（V1.0.9）
- [x] 修复大文件不能下载的问题（V1.0.9）
- [x] 修复Android 4.4及以下搜索闪退问题（V1.0.9）
- [x] 添加缓冲进度条（V1.0.9）
- [x] 减少滑动手势调整的播放进度幅度（V1.0.9）
- [x] 播放设置新增弹幕速度（V1.0.9）
- [x] 播放设置新增解码方式（V1.0.9）
- [x] 优化播放器播放参数，减少视频加载时间（V1.0.9）
- [x] 修复离线播放不能旋转屏幕的Bug（V1.1.0）
- [x] 登陆（V1.1.0）
- [x] 发送评论（V1.1.0）
- [x] 查看Up主视频（V1.1.0）
- [ ] 弹幕屏蔽功能（V1.1.1）
- [ ] 重写播放器（V1.1.1）
- [ ] 重写弹幕引擎（V1.1.1）
- [ ] 个人中心（设计中）
- [ ] 同步收藏（接口不全）
- [ ] 修复播放时跳回开头的问题（无法重现，原因不明）

## FAQ
- [FAQ](https://github.com/blackbbc/Tucao/blob/master/FAQ.md)

## UpdateLog
- [更新历史](https://github.com/blackbbc/Tucao/blob/master/changelog.md)

## Instructions
- 热烈庆祝[Kotlin](https://kotlinlang.org/)成为Android开发一级语言，撒花～～～
- 设计编码均独立完成，如果你觉得太丑，欢迎砸设计稿
- 架构基于MVVM模式，使用`DataBinding` + `RxJava2` + `Dagger2` + `Retrofit`实现

## Development
Android Studio 版本: [3.0 Canary 7](https://developer.android.com/studio/preview/index.html)

由于项目中使用了[子模块Submodule](https://git-scm.com/book/zh/v1/Git-工具-子模块)，请务必使用以下命令克隆项目
```
git clone --recursive -j8 https://github.com/blackbbc/Tucao.git
```

如果使用`Windows`进行开发，由于Data Binding在Windows上存在UTF8编码问题，不能编译通过，请参考如下两个Issue自行解决：
[#4](https://github.com/blackbbc/Tucao/issues/4) [#7](https://github.com/blackbbc/Tucao/issues/7)


## Statement
该项目仅供交流学习使用，如果该项目有侵犯Tucao版权问题，本人会及时删除此页面与整个项目。

## Thanks to the open source project
- [Kotlin](https://github.com/JetBrains/kotlin)
- [RxJava](https://github.com/ReactiveX/RxJava)
- [RxLifecycle](https://github.com/trello/RxLifecycle)
- [RxDownload](https://github.com/ssseasonnn/RxDownload)
- [Retrofit](https://github.com/square/retrofit)
- [Dagger2](https://github.com/google/dagger)
- [EventBus](https://github.com/greenrobot/EventBus)
- [GSYVideoPlayer](https://github.com/CarGuo/GSYVideoPlayer)
- [ijkplayer](https://github.com/Bilibili/ijkplayer)
- [DanmakuFlameMaster](https://github.com/Bilibili/DanmakuFlameMaster)
- [Glide](https://github.com/bumptech/glide)
- [BaseRecyclerViewAdapterHelper](https://github.com/CymChad/BaseRecyclerViewAdapterHelper)
- [CrashWoodpecker](https://github.com/drakeet/CrashWoodpecker)
- [Leakcanary](https://github.com/square/leakcanary)
- [Convenientbanner](https://github.com/saiwu-bigkoo/Android-ConvenientBanner)
