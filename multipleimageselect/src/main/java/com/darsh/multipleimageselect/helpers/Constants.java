package com.darsh.multipleimageselect.helpers;

/**
 * Created by Darshan on 5/26/2015.
 */
public class Constants {
    public static final int REQUEST_CODE = 2000;

    public static final int FETCH_STARTED = 2001;
    public static final int FETCH_COMPLETED = 2002;
    public static final int PERMISSION_GRANTED = 2003;
    public static final int PERMISSION_DENIED = 2004;
    public static final int ERROR = 2005;

    public static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 23;

    public static final String INTENT_EXTRA_ALBUM = "album";
    public static final String INTENT_EXTRA_IMAGES = "images";
    public static final String INTENT_EXTRA_LIMIT = "limit";
    public static final int DEFAULT_LIMIT = 10;

    //Maximum number of images that can be selected at a time
    public static int limit;
}
