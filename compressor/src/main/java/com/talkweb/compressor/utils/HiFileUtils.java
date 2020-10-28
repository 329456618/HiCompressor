package com.talkweb.compressor.utils;

import ohos.app.Context;
import ohos.global.icu.text.DecimalFormat;
import ohos.global.resource.Resource;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.UUID;

public class HiFileUtils {


    /**
     * 从资源目录读取图片保存到缓存目录
     *
     * @param _this
     * @param filename
     * @return
     */
    public static File opendRawFile(Context _this, String filename) {
        try {
            ohos.global.resource.ResourceManager resManager = _this.getResourceManager();
            ohos.global.resource.RawFileEntry rawFileEntry = resManager.getRawFileEntry("resources/rawfile/" + filename);
            Resource resource = rawFileEntry.openRawFile();
            byte[] buffer = new byte[resource.available()];
            resource.read(buffer);
            File file = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
            OutputStream outStream = new FileOutputStream(file);
            outStream.write(buffer);
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 文件转为PixeMap
     *
     * @param file
     * @return
     */
    public static PixelMap fileToPixelMap(File file) {
        try {
            //加载图片
            ImageSource.SourceOptions opts = new ImageSource.SourceOptions();
            ImageSource imageSource = ImageSource.create(file, opts);
            ImageSource.DecodingOptions opt = new ImageSource.DecodingOptions();
            PixelMap map = imageSource.createPixelmap(opt);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 格式化文件大小显示
     *
     * @param fileSize
     * @return
     */
    public static String getReadableFileSize(Long fileSize) {
        if (fileSize <= 0) {
            return "0";
        }
        String[] arr = {"Bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        float srcsize = Float.valueOf(fileSize);
        int index = (int) (Math.floor(Math.log(srcsize) / Math.log(1024)));
        double size = srcsize / Math.pow(1024, index);
        size = Double.valueOf(new DecimalFormat("#.00").format(size));
        return size + arr[index];
    }

    public static void copyFile(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }
}
