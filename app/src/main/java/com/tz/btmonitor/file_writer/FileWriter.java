package com.tz.btmonitor.file_writer;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

public class FileWriter {
    private final Context context;
    private final Uri uri;
    private final String fileName;
    private OutputStream outputStream;

    public FileWriter(Context context, Uri uri) {
        this.context = context;
        this.uri = uri;
        this.fileName = getFileNameFromUri(uri);
        openStream();
    }

    private void openStream() {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            outputStream = contentResolver.openOutputStream(uri, "w");
//            outputStream = new FileOutputStream(contentResolver.openFileDescriptor(uri, "w").getFileDescriptor());
//            outputStream = contentResolver.openFileDescriptor(uri, "w") != null ?
//                    contentResolver.openFileDescriptor(uri, "w").getFileDescriptor() :
//                    new FileOutputStream(Environment.getExternalStorageDirectory().getPath() + "/" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void write(List<String[]> data) {
        if (outputStream != null) {
            try {
                for (String[] row : data) {
                    outputStream.write(String.join(",", Arrays.asList(row)).getBytes());
                    outputStream.write("\n".getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Handle error: Stream is not open
            System.err.println("Error: Stream is not open for writing.");
        }
    }

    public void close() {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String displayName = null;
        try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                displayName = cursor.getString(nameIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return displayName;
    }

    public String getFileName() {
        return fileName;
    }
}
