package com.missile.daemon;


import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Command {
    private static final String TAG = "Command";

    /**
     * copy file to destination
     *
     * @param file
     * @param is
     * @param mode
     */
    private static void copyFile(File file, InputStream is, String mode) {
        try {
            final String abspath = file.getAbsolutePath();
            final FileOutputStream out = new FileOutputStream(file);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            out.close();
            is.close();

            Runtime.getRuntime().exec("chmod " + mode + " " + abspath).waitFor();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * copy file in assets into destination file
     *
     * @param context
     * @param assetsFilename
     * @param file
     * @param mode
     */
    public static void copyAssets(Context context, String assetsFilename, File file, String mode) {
        AssetManager manager = context.getAssets();

        try {
            final InputStream is;
            is = manager.open(assetsFilename);
            copyFile(file, is, mode);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String install(Context context, String destDir, String filename) {
        String binaryDir = "armeabi";

        String abi = Build.CPU_ABI;
        if (abi.startsWith("armeabi-v7a")) {
            binaryDir = "armeabi-v7a";
        } else if (abi.startsWith("x86")) {
            binaryDir = "x86";
        }

		/* for different platform */
        String assetfilename = binaryDir + File.separator + filename;

        try {
            File f = new File(context.getDir(destDir, Context.MODE_PRIVATE), filename);
            Log.d(TAG, "file position: " + f.getAbsolutePath());
            if (f.exists()) {
                Log.d(TAG, "binary has existed");
                f.delete();
                f = new File(context.getDir(destDir, Context.MODE_PRIVATE), filename);
            }

            copyAssets(context, assetfilename, f, "0755");
            return f.getAbsolutePath();
        } catch (Exception e) {
            Log.e(TAG, "installBinary failed: " + e.getMessage());
            return null;
        }
    }


}
