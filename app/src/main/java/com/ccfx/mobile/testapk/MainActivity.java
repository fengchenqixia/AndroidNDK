package com.ccfx.mobile.testapk;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String SDCARD_PATH = Environment.getExternalStorageDirectory() + File.separator;
    public static final String PATCH_FILE = "old-to-new.patch";
    public static final String NEW_APK_FILE = "new.apk";

    public static final String TEST_NEW_APK_FILE = "test_new.apk";
    public static final String TEST_PATCH_FILE = "test_patch.patch";
    public static final String TEST_OLDER_APK_FILE = "test_old.apk";

    // Used to load the 'native-lib' library on application startup.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        TextView tv = (TextView) findViewById(R.id.sample_text);

        if (ContextCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
        }

        findViewById(R.id.sample_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //并行任务
               // new ApkUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                new TestApkUpdateTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

            }
        });


    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    /**
     * 合并增量文件任务
     */
    private class ApkUpdateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            String oldApkPath = ApkUtils.getCurApkPath(MainActivity.this);
            File oldApkFile = new File(oldApkPath);
            File patchFile = new File(getPatchFilePath());
            if(oldApkFile.exists() && patchFile.exists()) {
                Log("正在合并增量文件...");
                String newApkPath = getNewApkFilePath();
                BsPatchJNI.patch(oldApkPath, newApkPath, getPatchFilePath());
//                //检验文件MD5值
//                return Signtils.checkMd5(oldApkFile, MD5);

                Log("增量文件的MD5值为：" + SignUtils.getMd5ByFile(patchFile));
                Log("新文件的MD5值为：" + SignUtils.getMd5ByFile(new File(newApkPath)));

                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result) {
                Log("合并成功，开始安装");
                ApkUtils.installApk(MainActivity.this, getNewApkFilePath());
            } else {
                Log("合并失败");
            }
        }
    }


    private class TestApkUpdateTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... params) {
            File oldApkFile = new File(SDCARD_PATH+TEST_OLDER_APK_FILE);
            File patchFile = new File(SDCARD_PATH+TEST_PATCH_FILE);
            if(oldApkFile.exists() && patchFile.exists()) {
                Log("正在合并增量文件...");
                String newApkPath = SDCARD_PATH+TEST_NEW_APK_FILE;
                BsPatchJNI.patch(oldApkFile.getAbsolutePath(), newApkPath, patchFile.getAbsolutePath());
//                //检验文件MD5值
//                return Signtils.checkMd5(oldApkFile, MD5);

                Log("增量文件的MD5值为：" + SignUtils.getMd5ByFile(patchFile));
                Log("新文件的MD5值为：" + SignUtils.getMd5ByFile(new File(newApkPath)));

                return true;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if(result) {
                Log("合并成功，开始安装");
                ApkUtils.installApk(MainActivity.this, SDCARD_PATH+TEST_NEW_APK_FILE);
            } else {
                Log("合并失败");
            }
        }
    }

    private String getPatchFilePath() {
        return SDCARD_PATH + PATCH_FILE;
    }

    private String getNewApkFilePath() {
        return SDCARD_PATH + NEW_APK_FILE;
    }

    /**
     * 打印日志
     * @param log
     */
    private void Log(String log) {
        Log.e("MainActivity", log);
    }


}
