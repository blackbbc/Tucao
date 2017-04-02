package master.flame.danmaku.danmaku.util;

public class SystemClock {
    private static ClockProvider mClockProvider = null;

    public static long uptimeMillis() {
        if (mClockProvider != null) {
            return mClockProvider.updateMillis();
        } else {
            return 0;
        }
    }

    public static void sleep(long mills) {
        android.os.SystemClock.sleep(mills);
    }

    public static void setClockProvider(ClockProvider clockProvider) {
        mClockProvider = clockProvider;
    }

    public static void releaseClockProvider() {
        // TODO: 不知道会不会内存泄漏 =.=
//        mClockProvider = null;
    }

    public interface ClockProvider {
        long updateMillis();
    }
}
