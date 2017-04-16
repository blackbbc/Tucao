package me.sweetll.tucao.rxdownload.entity;

import java.util.Map;
import java.util.concurrent.Semaphore;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.schedulers.Schedulers;
import me.sweetll.tucao.rxdownload.db.DataBaseHelper;
import me.sweetll.tucao.rxdownload.function.Constant;
import me.sweetll.tucao.rxdownload.RxDownload;

import static me.sweetll.tucao.rxdownload.entity.DownloadFlag.WAITING;
import static me.sweetll.tucao.rxdownload.function.DownloadEventFactory.completed;
import static me.sweetll.tucao.rxdownload.function.DownloadEventFactory.failed;
import static me.sweetll.tucao.rxdownload.function.DownloadEventFactory.normal;
import static me.sweetll.tucao.rxdownload.function.DownloadEventFactory.paused;
import static me.sweetll.tucao.rxdownload.function.DownloadEventFactory.started;
import static me.sweetll.tucao.rxdownload.function.DownloadEventFactory.waiting;
import static me.sweetll.tucao.rxdownload.function.Utils.createProcessor;
import static me.sweetll.tucao.rxdownload.function.Utils.deleteFiles;
import static me.sweetll.tucao.rxdownload.function.Utils.dispose;
import static me.sweetll.tucao.rxdownload.function.Utils.formatStr;
import static me.sweetll.tucao.rxdownload.function.Utils.getFiles;
import static me.sweetll.tucao.rxdownload.function.Utils.log;

/**
 * Author: Season(ssseasonnn@gmail.com)
 * Date: 2017/2/24
 * <p>
 * SingleMission, only one url.
 */
public class SingleMission extends DownloadMission {
    protected DownloadStatus status;
    protected Disposable disposable;
    private DownloadBean bean;

    private String missionId;
    private Observer<DownloadStatus> observer;

    public SingleMission(RxDownload rxdownload, DownloadBean bean) {
        super(rxdownload);
        this.bean = bean;
    }

    public SingleMission(RxDownload rxDownload, DownloadBean bean,
                         String missionId, Observer<DownloadStatus> observer) {
        super(rxDownload);
        this.bean = bean;
        this.missionId = missionId;
        this.observer = observer;
    }

    public SingleMission(SingleMission other, Observer<DownloadStatus> observer) {
        super(other.rxdownload);
        this.bean = other.getBean();
        this.missionId = other.getMissionId();
        this.observer = observer;
    }

    @Override
    public String getUrl() {
        return bean.getUrl();
    }

    @Override
    public void init(Map<String, DownloadMission> missionMap,
                     Map<String, FlowableProcessor<DownloadEvent>> processorMap) {
        DownloadMission mission = missionMap.get(getUrl());
        if (mission == null) {
            missionMap.put(getUrl(), this);
        } else {
            if (mission.isCanceled()) {
                missionMap.put(getUrl(), this);
            } else {
                throw new IllegalArgumentException(formatStr(Constant.DOWNLOAD_URL_EXISTS, getUrl()));
            }
        }
        this.processor = createProcessor(getUrl(), processorMap);
    }

    @Override
    public void insertOrUpdate(DataBaseHelper dataBaseHelper) {
        if (dataBaseHelper.recordNotExists(getUrl())) {
            dataBaseHelper.insertRecord(bean, WAITING, missionId);
        } else {
            dataBaseHelper.updateRecord(getUrl(), WAITING, missionId);
        }
    }

    @Override
    public void sendWaitingEvent(DataBaseHelper dataBaseHelper) {
        processor.onNext(waiting(dataBaseHelper.readStatus(getUrl())));
    }

    @Override
    public void start(final Semaphore semaphore) throws InterruptedException {
        if (isCanceled()) {
            return;
        }

        semaphore.acquire();

        if (isCanceled()) {
            semaphore.release();
            return;
        }

        disposable = rxdownload.download(bean)
                .subscribeOn(Schedulers.io())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        if (observer != null) {
                            observer.onSubscribe(disposable);
                        }
                    }
                })
                .doFinally(new Action() {
                    @Override
                    public void run() throws Exception {
                        log("finally and release...");
                        setCanceled(true);
                        semaphore.release();
                    }
                })
                .subscribe(new Consumer<DownloadStatus>() {
                    @Override
                    public void accept(DownloadStatus value) throws Exception {
                        status = value;
                        processor.onNext(started(value));
                        if (observer != null) {
                            observer.onNext(value);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        processor.onNext(failed(status, throwable));
                        if (observer != null) {
                            observer.onError(throwable);
                        }
                    }
                }, new Action() {
                    @Override
                    public void run() throws Exception {
                        processor.onNext(completed(status));
                        setCompleted(true);

                        if (observer != null) {
                            observer.onComplete();
                        }
                    }
                });
    }

    @Override
    public void pause(DataBaseHelper dataBaseHelper) {
        dispose(disposable);
        setCanceled(true);
        if (processor != null && !isCompleted()) {
            processor.onNext(paused(dataBaseHelper.readStatus(getUrl())));
        }
    }

    @Override
    public void delete(DataBaseHelper dataBaseHelper, boolean deleteFile) {
        pause(dataBaseHelper);
        if (processor != null) {
            processor.onNext(normal(null));
        }
        if (deleteFile) {
            DownloadRecord record = dataBaseHelper.readSingleRecord(getUrl());
            if (record != null) {
                deleteFiles(getFiles(record.getSaveName(), record.getSavePath()));
            }
        }
        dataBaseHelper.deleteRecord(getUrl());
    }

    private String getMissionId() {
        return missionId;
    }

    private Observer<DownloadStatus> getObserver() {
        return observer;
    }

    private DownloadBean getBean() {
        return bean;
    }
}