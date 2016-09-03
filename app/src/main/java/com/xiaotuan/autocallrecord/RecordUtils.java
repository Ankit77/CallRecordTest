package com.xiaotuan.autocallrecord;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

public class RecordUtils {
    public static final int PHONE = 0;
    public static final int SDCARD = 1;
    public static final String SHARED_NAME = "autoCallRecord";
    public static final String STORAGE_LOCATION = "storage_location";
    public static final String SWITCH_STATU = "statu";
    private static final String TAG = "RecordUtils";

    public static String getExternalStoragePath() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            File sdDir = Environment.getExternalStorageDirectory();
            Log.d(TAG, "SD Path: " + sdDir.getAbsolutePath());
            return sdDir.getAbsolutePath();
        }
        Log.d(TAG, "SD card isn't mounted.");
        return null;
    }

    public static String getStorageRootPath() {
        String path = getExternalStoragePath();
        String root = null;
        if (path != null) {
            root = new File(path).getParent();
        }
        Log.d(TAG, "root path=>" + root);
        return root;
    }

    public static String[] getStoragePath(Context context) {
        StorageManager storageManager = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method getVolumePathsMethod = StorageManager.class.getMethod("getVolumePaths", new Class[PHONE]);
            getVolumePathsMethod.setAccessible(true);
            Object invoke = getVolumePathsMethod.invoke(storageManager, new Object[PHONE]);
            int i = PHONE;
            while (true) {
                if (i >= ((String[]) invoke).length) {
                    return (String[]) invoke;
                }
                Log.d(TAG, "path" + i + ": " + ((String[]) invoke)[i]);
                i += SDCARD;
            }
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
            return null;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e2) {
            e2.printStackTrace();
            return null;
        } catch (InvocationTargetException e3) {
            e3.printStackTrace();
            return null;
        }
    }

    public static boolean isExistSDCard(Context context) {
        String sdPath = getSDCardPath(context);
        if (sdPath == null) {
            return false;
        }
        File file = new File(sdPath + "/Auto Call Record");
        if (!file.exists()) {
            file.mkdirs();
        }
        if (!file.exists()) {
            return false;
        }
        file.delete();
        return true;
    }

    public static String getPhonePath(Context context) {
        String path = Environment.getExternalStorageDirectory().toString();
        Log.d(TAG, "Phone path=>" + path);
        return path;
    }

    public static String getSDCardPath(Context context) {
        String path = Environment.getExternalStorageDirectory().toString();
        Log.d(TAG, "SDCardPath=>" + path);
        return path;
    }

    public static int getFileCount(File file) {
        int fileCount = PHONE;
        if (file.exists() && file.isDirectory()) {
            fileCount = file.listFiles().length;
        }
        Log.d(TAG, "fileCount=>" + fileCount);
        return fileCount;
    }

    public static ArrayList<FileInfo> getList(String path, Context context) {
        ArrayList<FileInfo> list = new ArrayList();
        if (path != null) {
            String storagePath = getStorageRootPath();
            if (storagePath == null) {
                return list;
            }
            if (path.equals(storagePath)) {
                return getRootFileList(context);
            }
            return getCallRecordList(path, context);
        }
        Log.d(TAG, "getList(): path = null");
        return list;
    }

    public static ArrayList<FileInfo> getCallRecordList(String path, Context context) {
        ArrayList<FileInfo> list = new ArrayList();
        if (new File(path).exists()) {
            File[] arr$ = new File(path).listFiles();
            int len$ = arr$.length;
            for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
                File file = arr$[i$];
                if (file.isDirectory()) {
                    list.add(new FileInfo(file, file.listFiles().length));
                } else {
                    list.add(new FileInfo(file, (int) PHONE));
                }
            }
        }
        return list;
    }

    public static ArrayList<FileInfo> getRootFileList(Context context) {
        ArrayList<FileInfo> list = new ArrayList();
        String phonePath;
        File phone;
        FileInfo phoneInfo;
        if (isExistSDCard(context)) {
            String sdPath = getSDCardPath(context);
            if (sdPath != null) {
                File sd = new File(sdPath + "/Auto Call Record");
                if (!sd.exists()) {
                    sd.mkdirs();
                }
                if (sd.exists()) {
                    FileInfo sdInfo = new FileInfo(sd, sd.listFiles().length);
                    sdInfo.setFileName("SD Card");
                    list.add(sdInfo);
                }
            }
            phonePath = getPhonePath(context);
            if (phonePath != null) {
                phone = new File(phonePath + "/Auto Call Record");
                if (!phone.exists()) {
                    phone.mkdirs();
                }
                if (phone.exists()) {
                    phoneInfo = new FileInfo(phone, phone.listFiles().length);
                    phoneInfo.setFileName("Phone");
                    list.add(phoneInfo);
                }
            }
        } else {
            phonePath = getPhonePath(context);
            if (phonePath != null) {
                phone = new File(phonePath + "/Auto Call Record");
                if (!phone.exists()) {
                    phone.mkdirs();
                }
                if (phone.exists()) {
                    phoneInfo = new FileInfo(phone, phone.listFiles().length);
                    phoneInfo.setFileName("Phone");
                    list.add(phoneInfo);
                }
            }
        }
        return list;
    }

    public static void sharedObject(ArrayList<String> paths, Context context) {
        ArrayList<Uri> uriList = new ArrayList();
        Iterator i$ = paths.iterator();
        while (i$.hasNext()) {
            getTotalUris((String) i$.next(), uriList);
        }
        shared(context, uriList);
    }

    public static ArrayList<Uri> getTotalUris(String path, ArrayList<Uri> uriArray) {
        ArrayList<Uri> uriList = uriArray;
        File vCurFile = new File(path);
        if (vCurFile != null) {
            if (vCurFile.isFile()) {
                uriList.add(Uri.fromFile(vCurFile));
                Log.d(TAG, "getTotalUris=>" + Uri.fromFile(vCurFile).toString());
            } else {
                File[] arr$ = vCurFile.listFiles();
                int len$ = arr$.length;
                for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
                    File vFile = arr$[i$];
                    if (vFile.isFile()) {
                        uriList.add(Uri.fromFile(vFile));
                        Log.d(TAG, "getTotalUris=>" + Uri.fromFile(vCurFile).toString());
                    } else if (vFile.isDirectory()) {
                        getTotalUris(vFile.getAbsolutePath(), uriList);
                    }
                }
            }
        }
        return uriList;
    }

    public static void shared(Context context, ArrayList<Uri> uriArray) {
        if (uriArray != null && uriArray.size() > 0) {
            Intent intent = new Intent("android.intent.action.SEND_MULTIPLE");
            intent.putExtra("android.intent.extra.STREAM", uriArray);
            intent.setType("*/*");
            context.startActivity(intent);
        }
    }

    public static void deleteAllFile(ArrayList<String> deletePath) {
        Iterator i$ = deletePath.iterator();
        while (i$.hasNext()) {
            deleteFile((String) i$.next());
        }
    }

    private static void deleteFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        if (file.isDirectory()) {
            deleteFoalder(path);
        } else {
            file.delete();
        }
    }

    private static void deleteFoalder(String path) {
        File file = new File(path);
        if (file.exists()) {
            File[] arr$ = file.listFiles();
            int len$ = arr$.length;
            for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
                File f = arr$[i$];
                if (file.exists()) {
                    if (f.isDirectory()) {
                        deleteFoalder(f.getAbsolutePath());
                    } else {
                        f.delete();
                    }
                }
            }
        }
        file.delete();
    }

    public static boolean isMediaFile(File file) {
        if (file.exists()) {
            String path = file.getAbsolutePath();
            String suffix = path.substring(path.lastIndexOf("."));
            Log.d(TAG, "suffix=>" + suffix);
            String[] arr$ = new String[]{".amr", ".mp3"};
            int len$ = arr$.length;
            for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
                if (suffix.equals(arr$[i$])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void sendViewIntent(File file, Context context) {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setDataAndType(Uri.fromFile(file), "audio/*");
        context.startActivity(intent);
    }

    public static ArrayList<FileInfo> getFoalderList(String path, Context context) {
        ArrayList<FileInfo> list = new ArrayList();
        File file = new File(path);
        if (file.exists()) {
            File[] fileList = file.listFiles();
            String externalPath = getExternalStoragePath();
            if (externalPath != null) {
                File f;
                if (!path.equals(new File(externalPath).getParent()) || isExistSDCard(context)) {
                    File[] arr$ = fileList;
                    int len$ = arr$.length;
                    for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
                        f = arr$[i$];
                        if (f.exists() && f.isDirectory() && !f.isHidden()) {
                            Log.d(TAG, "path=>" + f.getAbsolutePath());
                            list.add(new FileInfo(f, f.listFiles().length));
                        }
                    }
                } else {
                    f = new File(getPhonePath(context));
                    list.add(new FileInfo(f, f.listFiles().length));
                }
            }
        }
        return list;
    }

    public static boolean copyAllFile(ArrayList<String> fromPath, String toPath, boolean isCut) {
        boolean flag = false;
        Iterator i$ = fromPath.iterator();
        while (i$.hasNext()) {
            String path = (String) i$.next();
            File file = new File(path);
            if (file.exists()) {
                if (file.isDirectory()) {
                    flag = copyDirectory(path, toPath, isCut);
                } else {
                    flag = copyFile(path, toPath, isCut);
                }
            }
        }
        return flag;
    }

    private static boolean copyFile(String path, String toPath, boolean isCut) {
        File file = new File(path);
        if (!file.exists()) {
            return false;
        }
        toPath = getOkPath(path, toPath);
        try {
            InputStream fosFrom = new FileInputStream(file);
            OutputStream fosTo = new FileOutputStream(new File(toPath));
            byte[] bt = new byte[1024];
            while (true) {
                int c = fosFrom.read(bt);
                if (c <= 0) {
                    break;
                }
                fosTo.write(bt, PHONE, c);
            }
            fosTo.close();
            fosFrom.close();
            if (isCut) {
                file.delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyDirectory(String path, String toPath, boolean isCut) {
        boolean flag = false;
        String directory = getOkDirectoryPath(path, toPath);
        File dir = new File(directory);
        dir.mkdirs();
        if (dir.exists()) {
            File[] arr$ = new File(path).listFiles();
            int len$ = arr$.length;
            for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
                File f = arr$[i$];
                if (f.exists()) {
                    if (f.isDirectory()) {
                        flag = copyDirectory(f.getAbsolutePath(), directory, isCut);
                    } else {
                        flag = copyFile(f.getAbsolutePath(), directory, isCut);
                    }
                }
            }
            if (isCut) {
                new File(path).delete();
            }
        }
        return flag;
    }

    private static String getOkDirectoryPath(String path, String toPath) {
        String pathName = new File(path).getName();
        String okPath = toPath + "/" + pathName;
        if (okPath.equals(path) || isExistSimpleFile(path, toPath)) {
            int i = SDCARD;
            while (true) {
                String okName = pathName + "(" + i + ")";
                okPath = toPath + "/" + okName;
                Log.d(TAG, "textPath=>" + okPath);
                if (!pathName.equals(okName) && !isExistSimpleFile(okName, toPath)) {
                    break;
                }
                i += SDCARD;
            }
        }
        Log.d(TAG, "oKDirectoryPath=>" + okPath + "\n" + "path=>" + path + "\n" + "toPath=>" + toPath);
        return okPath;
    }

    private static boolean isExistSimpleFile(String name, String toPath) {
        File[] list = new File(toPath).listFiles();
        Log.d(TAG, "toPath=>" + toPath + "\n" + "name=>" + name);
        File[] arr$ = list;
        int len$ = arr$.length;
        for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
            if (arr$[i$].getName().equals(name)) {
                Log.d(TAG, "has file=>true");
                return true;
            }
        }
        Log.d(TAG, "has file=>false");
        return false;
    }

    private static String getOkPath(String path, String toPath) {
        int index = path.lastIndexOf("/");
        int suffixIndex = path.lastIndexOf(".");
        String name = path.substring(index + SDCARD, suffixIndex);
        String suffix = path.substring(suffixIndex);
        String pathName = new File(path).getName();
        String okPath = toPath + "/" + pathName;
        if (okPath.equals(path) || isExistSimpleFile(pathName, toPath)) {
            int i = SDCARD;
            while (true) {
                String okName = name + "(" + i + ")" + suffix;
                okPath = toPath + "/" + okName;
                Log.d(TAG, "textPath=>" + okPath);
                if (!pathName.equals(okName) && !isExistSimpleFile(okName, toPath)) {
                    break;
                }
                i += SDCARD;
            }
        }
        return okPath;
    }

    public static boolean cutAllFile(ArrayList<String> fromPath, String toPath) {
        return copyAllFile(fromPath, toPath, true);
    }

    public static boolean isAllPathEnabled(String[] pathArray) {
        boolean flag = true;
        String[] arr$ = pathArray;
        int len$ = arr$.length;
        for (int i$ = PHONE; i$ < len$; i$ += SDCARD) {
            if (!new File(arr$[i$]).exists()) {
                flag = false;
                break;
            }
        }
        Log.d(TAG, "isAllPathEnabled=>" + flag);
        return flag;
    }

    public static boolean getRecordStatuSharedPreferencese(Context context) {
        boolean flag = context.getSharedPreferences(SHARED_NAME, PHONE).getBoolean(SWITCH_STATU, true);
        Log.d(TAG, "read record statu=>" + flag);
        return flag;
    }

    public static void putRecordStatuSharedPreferencese(Context context, boolean statu) {
        Editor editor = context.getSharedPreferences(SHARED_NAME, PHONE).edit();
        editor.putBoolean(SWITCH_STATU, statu);
        editor.apply();
        Log.d(TAG, "write record statu=>" + statu);
    }

    public static int getStorageLocationSharedPreferencese(Context context) {
        int location;
        SharedPreferences sp = context.getSharedPreferences(SHARED_NAME, PHONE);
        if (isExistSDCard(context)) {
            location = sp.getInt(STORAGE_LOCATION, SDCARD);
        } else {
            location = sp.getInt(STORAGE_LOCATION, PHONE);
        }
        Log.d(TAG, "read storage location=>" + location);
        return location;
    }

    public static void putStorageLocationSharedPreferencese(Context context, int location) {
        Editor editor = context.getSharedPreferences(SHARED_NAME, PHONE).edit();
        editor.putInt(STORAGE_LOCATION, location);
        editor.apply();
        Log.d(TAG, "write storage location=>" + location);
    }

    public static boolean getFirstStartSharedPreferencese(Context context) {
        boolean isFirstStart = context.getSharedPreferences(SHARED_NAME, PHONE).getBoolean("first_start", true);
        Log.d(TAG, "read storage location=>" + isFirstStart);
        return isFirstStart;
    }

    public static void putFirstStartSharedPreferencese(Context context, boolean isFirstStart) {
        Editor editor = context.getSharedPreferences(SHARED_NAME, PHONE).edit();
        editor.putBoolean("first_start", isFirstStart);
        editor.apply();
        Log.d(TAG, "write storage location=>" + isFirstStart);
    }

    public static boolean isHasEnoughStorageSpace(Context context, long minStorageUsage) {
        boolean flag;
        StatFs stat = new StatFs(getPhonePath(context));
        long blockSize = (long) stat.getBlockSize();
        Log.d(TAG, "blockSize=>" + blockSize);
        long availableBlocks = (long) stat.getAvailableBlocks();
        Log.d(TAG, "availableBlocks=>" + availableBlocks);
        float fressStorage = (float) ((availableBlocks * blockSize) / 1048576);
        Log.d(TAG, "freeSpace=>" + fressStorage);
        if (fressStorage >= ((float) minStorageUsage)) {
            flag = true;
        } else {
            flag = false;
        }
        Log.d(TAG, "isHasEnoughStorageSpace=>" + flag);
        return flag;
    }

    public static boolean isOkRecordStorageSpace(Context context, String path, long minStorageUsage) {
        boolean flag;
        StatFs stat = new StatFs(path);
        long blockSize = (long) stat.getBlockSize();
        Log.d(TAG, "blockSize=>" + blockSize);
        long availableBlocks = (long) stat.getAvailableBlocks();
        Log.d(TAG, "availableBlocks=>" + availableBlocks);
        float fressStorage = (float) ((availableBlocks * blockSize) / 1048576);
        Log.d(TAG, "freeSpace=>" + fressStorage);
        if (fressStorage >= ((float) minStorageUsage)) {
            flag = true;
        } else {
            flag = false;
        }
        Log.d(TAG, "isHasEnoughStorageSpace=>" + flag);
        return flag;
    }
}
