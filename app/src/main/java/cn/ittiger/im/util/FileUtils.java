package cn.ittiger.im.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Environment;

import java.io.File;

import butterknife.internal.Utils;

public class FileUtils {
    public static final String IMAGE_EXTENSION = ".jpg";
    public static final String VOICE_EXTENSION = ".3gp";
    /**
     * Get a usable cache directory (external if available, internal otherwise).
     *
     * @param context The context to use
     * @param uniqueName A unique directory name to append to the cache dir
     * @return The cache dir
     */
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = isExternalStorageWritable() || !isExternalStorageRemovable() ? getExternalCacheDir(context).getPath() :
                context.getCacheDir().getPath();

        return new File(cachePath + File.separator + uniqueName);
    }

    /**
     * Check if external storage is built-in or removable.
     *
     * @return True if external storage is removable (like an SD card), false
     *         otherwise.
     */
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    public static boolean isExternalStorageRemovable() {
        if (cn.ittiger.im.util.Utils.hasGingerbread()) {
            return Environment.isExternalStorageRemovable();
        }
        return true;
    }

    /**
     * Get the external app cache directory.
     *
     * @param context The context to use
     * @return The external cache dir
     */
    @TargetApi(Build.VERSION_CODES.FROYO)
    public static File getExternalCacheDir(Context context) {
        if (cn.ittiger.im.util.Utils.hasFroyo()) {
            return context.getExternalCacheDir();
        }

        // Before Froyo we need to construct the external cache dir ourselves
        final String cacheDir = "/Android/data/" + context.getPackageName() + "/cache/";
        return new File(Environment.getExternalStorageDirectory().getPath() + cacheDir);
    }

    public static File getSentImagesDir(Context context) {
        File dir = new File(getImagesDir(context), "sent");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }
    public static File getSentVoiceDir(Context context) {
        File dir = new File(getVoiceDir(context), "sent");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File getReceivedImagesDir(Context context) {
        File dir = new File(getImagesDir(context), "received");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    public static File getReceivedVoiceDir(Context context) {
        File dir = new File(getVoiceDir(context), "received");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        return dir;
    }

    private static File getImagesDir(Context context) {
        return isExternalStorageWritable() ? context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) : context.getDir("images", Context.MODE_PRIVATE);
    }
    private static File getVoiceDir(Context context) {
        return isExternalStorageWritable() ? context.getExternalFilesDir(Environment.DIRECTORY_MUSIC) : context.getDir("voice", Context.MODE_PRIVATE);
    }

    public static boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }
}