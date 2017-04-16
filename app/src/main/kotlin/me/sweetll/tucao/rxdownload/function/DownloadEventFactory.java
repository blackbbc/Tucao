package me.sweetll.tucao.rxdownload.function;

import me.sweetll.tucao.rxdownload.entity.DownloadEvent;
import me.sweetll.tucao.rxdownload.entity.DownloadStatus;

import static me.sweetll.tucao.rxdownload.entity.DownloadFlag.COMPLETED;
import static me.sweetll.tucao.rxdownload.entity.DownloadFlag.FAILED;
import static me.sweetll.tucao.rxdownload.entity.DownloadFlag.NORMAL;
import static me.sweetll.tucao.rxdownload.entity.DownloadFlag.PAUSED;
import static me.sweetll.tucao.rxdownload.entity.DownloadFlag.STARTED;
import static me.sweetll.tucao.rxdownload.entity.DownloadFlag.WAITING;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2016/11/23
 * Time: 14:18
 * FIXME
 */
public class DownloadEventFactory {
    public static DownloadEvent normal(DownloadStatus status) {
        return createEvent(NORMAL, status);
    }

    public static DownloadEvent waiting(DownloadStatus status) {
        return createEvent(WAITING, status);
    }

    public static DownloadEvent started(DownloadStatus status) {
        return createEvent(STARTED, status);
    }

    public static DownloadEvent paused(DownloadStatus status) {
        return createEvent(PAUSED, status);
    }

    public static DownloadEvent completed(DownloadStatus status) {
        return createEvent(COMPLETED, status);
    }

    public static DownloadEvent failed(DownloadStatus status, Throwable throwable) {
        return createEvent(FAILED, status, throwable);
    }

    private static DownloadEvent createEvent(int flag, DownloadStatus status, Throwable throwable) {
        DownloadEvent event = createEvent(flag, status);
        event.setError(throwable);
        return event;
    }

    public static DownloadEvent createEvent(int flag, DownloadStatus status) {
        DownloadEvent event = new DownloadEvent();
        event.setDownloadStatus(status == null ? new DownloadStatus() : status);
        event.setFlag(flag);
        return event;
    }
}
