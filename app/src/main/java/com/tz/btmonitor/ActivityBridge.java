package com.tz.btmonitor;

import android.net.Uri;

import com.tz.btmonitor.file_writer.FileWriter;

public interface ActivityBridge {
    void checkPermissions(Runnable permissionsGranted);

    void createFile(Callback<FileWriter> callback);
}