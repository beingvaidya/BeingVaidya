package com.mayurkakade.beingvaidya.custom;

import android.app.Activity;
import android.app.Dialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.URLUtil;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.mayurkakade.beingvaidya.BuildConfig;
import com.mayurkakade.beingvaidya.ui.activities.PDFViewerActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;


public class MyDownloadManager {
    Activity mContext = null;
    String url = null;
    String mFileName = null;
    Dialog dialog = null;
    BroadcastReceiver receiver = null;

    public void DownloadFile(
            Activity context,
            String mUrl,
            String fileName,
            Dialog determinant_dialog
    ) {
        mContext = context;
        url = mUrl;
        mFileName = fileName;
        dialog = determinant_dialog;
        String title = URLUtil.guessFileName(url, null, null);
        mFileName = title;
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle(title);
        request.setDescription("Downloading");
        String cookie = CookieManager.getInstance().getCookie(url);
        request.addRequestHeader("cookie", cookie);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title);
//        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES, title)
        /* request.setDestinationInExternalFilesDir(
              context.applicationContext,
              folder.absolutePath,
              title
          )*/
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadManager.enqueue(request);
        RegisterDownloadManagerReciever(context, downloadManager);
    }


    public void RegisterDownloadManagerReciever(Context context, DownloadManager downloadManager) {
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == action) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    context.unregisterReceiver(receiver);
                    Toast.makeText(context, "Download Successfully", Toast.LENGTH_SHORT).show();
                    Bundle bundle = intent.getExtras();
                    Long file_id = bundle.getLong(DownloadManager.EXTRA_DOWNLOAD_ID);
//                    val downloadURI = downloadManager.getUriForDownloadedFile(file_id)
                    try {
                        File myDir = new File(
                                Environment.getExternalStorageDirectory()
                                        .toString() + File.separator + Environment.DIRECTORY_DOWNLOADS);
                        File fileDownload = new File(myDir, mFileName);
                        if (fileDownload.exists()) {
                            ContextWrapper cw = new ContextWrapper(context);
                            File directory = cw.getFilesDir();
                            File file = new File(directory, mFileName);
                            try {
                                file.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            try {
                                copyFile(fileDownload, file);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            scannFile(context, file);

                            fileDownload.delete();

                            Uri photoURI = Uri.fromFile(file);
                            Intent intent1 = new Intent(mContext, PDFViewerActivity.class);
                            intent1.putExtra("PDF_URI", photoURI.toString());
                            mContext.startActivity(intent1);
                        } else {
                            Log.e("Error Download: ", "File Not Download");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.e("Error Download: ", "" + e.getMessage().toString());
                    }
                }
            }
        };
        context.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }


    public void copyFile(File src, File dst) throws IOException {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(src);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (!dst.exists()) {
            dst.mkdir();
        }
        if (!dst.canWrite()) {
            // print("CAN'T WRITE")
            Log.e("Error copyFile: ", "CAN'T WRITE");
            return;
        }
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outStream.getChannel());
        try {
            inStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        outStream.close();
    }

    public void scannFile(Context context, File file) {
        Intent intent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        Uri mSelectedImageUri = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mSelectedImageUri = Uri.fromFile(file);
        } else {
            FileProvider.getUriForFile(
                    context, BuildConfig.APPLICATION_ID + ".provider",
                    file
            );
        }
        intent.setData(mSelectedImageUri);
        context.sendBroadcast(intent);
    }
}