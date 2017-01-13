package me.sweetll.tucao.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.shuyu.gsyvideoplayer.video.CustomGSYVideoPlayer;

import me.sweetll.tucao.R;

public class LandLayoutVideo extends CustomGSYVideoPlayer {

    /**
     * 1.5.0开始加入，如果需要不同布局区分功能，需要重载
     */
    public LandLayoutVideo(Context context, Boolean fullFlag) {
        super(context, fullFlag);
    }

    public LandLayoutVideo(Context context) {
        super(context);
    }

    public LandLayoutVideo(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    //这个必须配置最上面的构造才能生效
    @Override
    public int getLayoutId() {
        if (mIfCurrentIsFullscreen) {
            return R.layout.danmu_video_land;
        }
        return R.layout.danmu_video;
    }

    @Override
    protected void updateStartImage() {
        if (mIfCurrentIsFullscreen) {
            ImageView imageView = (ImageView) mStartButton;
            if (mCurrentState == CURRENT_STATE_PLAYING) {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_play_selector);
            } else if (mCurrentState == CURRENT_STATE_ERROR) {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector);
            } else {
                imageView.setImageResource(com.shuyu.gsyvideoplayer.R.drawable.video_click_pause_selector);
            }
        } else {
            super.updateStartImage();
        }
    }


}
