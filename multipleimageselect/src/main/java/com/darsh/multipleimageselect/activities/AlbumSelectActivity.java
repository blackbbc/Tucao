package com.darsh.multipleimageselect.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.darsh.multipleimageselect.R;
import com.darsh.multipleimageselect.adapters.CustomAlbumSelectAdapter;
import com.darsh.multipleimageselect.helpers.Constants;
import com.darsh.multipleimageselect.models.Album;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by Darshan on 4/14/2015.
 */
public class AlbumSelectActivity extends AppCompatActivity {
    private final String TAG = AlbumSelectActivity.class.getName();

    private ArrayList<Album> albums;

    private TextView requestPermission;
    private Button grantPermission;
    private final String[] requiredPermissions = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE };

    private TextView errorDisplay;

    private ProgressBar progressBar;
    private GridView gridView;
    private CustomAlbumSelectAdapter adapter;

    private ActionBar actionBar;

    private ContentObserver observer;
    private Handler handler;
    private Thread thread;

    private final String[] projection = new String[]{ MediaStore.Images.Media.BUCKET_DISPLAY_NAME, MediaStore.Images.Media.DATA };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album_select);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_arrow_back);

            actionBar.setDisplayShowTitleEnabled(true);
            actionBar.setTitle(R.string.album_view);
        }

        Intent intent = getIntent();
        if (intent == null) {
            finish();
        }
        Constants.limit = intent.getIntExtra(Constants.INTENT_EXTRA_LIMIT, Constants.DEFAULT_LIMIT);

        errorDisplay = (TextView) findViewById(R.id.text_view_error);
        errorDisplay.setVisibility(View.INVISIBLE);

        requestPermission = (TextView) findViewById(R.id.text_view_request_permission);
        grantPermission = (Button) findViewById(R.id.button_grant_permission);
        grantPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();
            }
        });
        hidePermissionHelperUI();

        progressBar = (ProgressBar) findViewById(R.id.progress_bar_album_select);
        gridView = (GridView) findViewById(R.id.grid_view_album_select);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), ImageSelectActivity.class);
                intent.putExtra(Constants.INTENT_EXTRA_ALBUM, albums.get(position).name);
                startActivityForResult(intent, Constants.REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Constants.PERMISSION_GRANTED: {
                        hidePermissionHelperUI();

                        loadAlbums();

                        break;
                    }

                    case Constants.PERMISSION_DENIED: {
                        showPermissionHelperUI();

                        progressBar.setVisibility(View.INVISIBLE);
                        gridView.setVisibility(View.INVISIBLE);

                        break;
                    }

                    case Constants.FETCH_STARTED: {
                        progressBar.setVisibility(View.VISIBLE);
                        gridView.setVisibility(View.INVISIBLE);

                        break;
                    }

                    case Constants.FETCH_COMPLETED: {
                        if (adapter == null) {
                            adapter = new CustomAlbumSelectAdapter(getApplicationContext(), albums);
                            gridView.setAdapter(adapter);

                            progressBar.setVisibility(View.INVISIBLE);
                            gridView.setVisibility(View.VISIBLE);
                            orientationBasedUI(getResources().getConfiguration().orientation);

                        } else {
                            adapter.notifyDataSetChanged();
                        }

                        break;
                    }

                    case Constants.ERROR: {
                        progressBar.setVisibility(View.INVISIBLE);
                        errorDisplay.setVisibility(View.VISIBLE);

                        break;
                    }

                    default: {
                        super.handleMessage(msg);
                    }
                }
            }
        };
        observer = new ContentObserver(handler) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                loadAlbums();
            }
        };
        getContentResolver().registerContentObserver(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, false, observer);

        checkIfPermissionGranted();
    }

    private void checkIfPermissionGranted() {
        if (ContextCompat.checkSelfPermission(AlbumSelectActivity.this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermission();
            return;
        }

        Message message = handler.obtainMessage();
        message.what = Constants.PERMISSION_GRANTED;
        message.sendToTarget();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(AlbumSelectActivity.this,
                requiredPermissions,
                Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == Constants.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            Message message = handler.obtainMessage();
            message.what = grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED ? Constants.PERMISSION_GRANTED : Constants.PERMISSION_DENIED;
            message.sendToTarget();
        }
    }

    private void hidePermissionHelperUI() {
        requestPermission.setVisibility(View.INVISIBLE);
        grantPermission.setVisibility(View.INVISIBLE);
    }

    private void showPermissionHelperUI() {
        requestPermission.setVisibility(View.VISIBLE);
        grantPermission.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();

        abortLoading();

        getContentResolver().unregisterContentObserver(observer);
        observer = null;

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
            handler = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (actionBar != null) {
            actionBar.setHomeAsUpIndicator(null);
        }
        albums = null;
        if (adapter != null) {
            adapter.releaseResources();
        }
        gridView.setOnItemClickListener(null);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        orientationBasedUI(newConfig.orientation);
    }

    private void orientationBasedUI(int orientation) {
        final WindowManager windowManager = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        final DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);

        if (adapter != null) {
            int size = orientation == Configuration.ORIENTATION_PORTRAIT ? metrics.widthPixels / 2 : metrics.widthPixels / 4;
            adapter.setLayoutParams(size);
        }
        gridView.setNumColumns(orientation == Configuration.ORIENTATION_PORTRAIT ? 2 : 4);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            setResult(RESULT_OK, data);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                onBackPressed();
                return true;
            }

            default: {
                return false;
            }
        }
    }

    private void loadAlbums() {
        abortLoading();

        AlbumLoaderRunnable runnable = new AlbumLoaderRunnable();
        thread = new Thread(runnable);
        thread.start();
    }


    private void abortLoading() {
        if (thread == null) {
            return;
        }

        if (thread.isAlive()) {
            thread.interrupt();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class AlbumLoaderRunnable implements Runnable {
        @Override
        public void run() {
            android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);

            Message message;
            if (adapter == null) {
                message = handler.obtainMessage();
                message.what = Constants.FETCH_STARTED;
                message.sendToTarget();
            }

            if (Thread.interrupted()) {
                return;
            }

            Cursor cursor = getApplicationContext().getContentResolver()
                    .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                            null, null, MediaStore.Images.Media.DATE_ADDED);

            if (cursor == null) {
                message = handler.obtainMessage();
                message.what = Constants.ERROR;
                message.sendToTarget();
                return;
            }

            ArrayList<Album> temp = new ArrayList<>(cursor.getCount());
            HashSet<String> albumSet = new HashSet<>();
            File file;

            if (cursor.moveToLast()) {
                do {
                    if (Thread.interrupted()) {
                        return;
                    }

                    String album = cursor.getString(cursor.getColumnIndex(projection[0]));
                    String image = cursor.getString(cursor.getColumnIndex(projection[1]));

                    /*
                    It may happen that some image file paths are still present in cache,
                    though image file does not exist. These last as long as media
                    scanner is not run again. To avoid get such image file paths, check
                    if image file exists.
                     */
                    file = new File(image);
                    if (file.exists() && !albumSet.contains(album)) {
                        temp.add(new Album(album, image));
                        albumSet.add(album);
                    }

                } while (cursor.moveToPrevious());
            }
            cursor.close();

            if (albums == null) {
                albums = new ArrayList<>();
            }
            albums.clear();
            albums.addAll(temp);

            message = handler.obtainMessage();
            message.what = Constants.FETCH_COMPLETED;
            message.sendToTarget();

            Thread.interrupted();
        }
    }
}
