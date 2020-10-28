package com.talkweb.compressor;

import com.talkweb.compressor.utils.HiFileUtils;
import com.talkweb.compressor.utils.Preconditions;
import ohos.app.Context;
import ohos.media.image.ImagePacker;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;
import ohos.media.image.common.ImageInfo;
import ohos.media.image.common.Size;

import java.io.*;
import java.util.UUID;


/**
 * 缩放工具
 */
public class HiCompressor {
    private Context _this;
    private File sourceImgFile;//原文件
    private Size imgSize;//缩放大小
    private Float scale;//缩放比例大小
    private Integer quality;//质量
    private Long maxFileSize;//返回文件最大值
    private Integer rotateDegrees;//旋转度


    public static HiCompressor getInstance() {
        HiCompressor _hicompressor = new HiCompressor();
        return _hicompressor;
    }

    /**
     * 设置压缩原文件
     *
     * @param _this
     * @param sourceImgFile
     * @return
     */
    public HiCompressor compressor(Context _this, File sourceImgFile) {
        Preconditions.checkNotNull(_this, "Context Can't be empty");
        Preconditions.checkNotNull(sourceImgFile, "sourceImgFile Can't be empty");
        Preconditions.checkArgument(sourceImgFile.isFile(), "sourceImgFile there is no");
        this._this = _this;
        this.sourceImgFile = sourceImgFile;
        return this;
    }


    /**
     * 缩放
     *
     * @param width
     * @param height
     * @return
     */
    public HiCompressor resolution(int width, int height) {
        Preconditions.checkArgument(width > 0, "width must be > 0");
        Preconditions.checkArgument(height > 0, "height must be > 0");
        imgSize = new Size();
        imgSize.height = height;
        imgSize.width = width;
        return this;
    }

    /**
     * 旋转度
     *
     * @param rotateDegrees
     * @return
     */
    public HiCompressor rotateDegrees(int rotateDegrees) {
        this.rotateDegrees = rotateDegrees;
        return this;
    }

    /**
     * 等比缩放
     *
     * @param scale
     * @return
     */
    public HiCompressor resolution(float scale) {
        Preconditions.checkArgument(scale > 0, "scale must be > 0");
        this.scale = scale;
        return this;
    }

    /**
     * 质量
     *
     * @param quality
     * @return
     */
    public HiCompressor quality(int quality) {
        Preconditions.checkArgument(quality > 0, "quality must be > 0");
        Preconditions.checkArgument(quality < 100, "quality must be < 100");
        this.quality = quality;
        return this;
    }

    /**
     * 缩放到文件大小
     *
     * @param maxFileSize
     * @return
     */
    public HiCompressor size(long maxFileSize) {
        Preconditions.checkArgument(maxFileSize > 0, "maxFileSize must be > 0");
        this.maxFileSize = maxFileSize;
        return this;
    }


    /**
     * 返回压缩后的文件
     *
     * @return
     */
    public File build() throws Exception {
        Preconditions.checkNotNull(_this, "Context Can't be empty");
        Preconditions.checkNotNull(sourceImgFile, "sourceImgFile Can't be empty");
        Preconditions.checkArgument(sourceImgFile.isFile(), "sourceImgFile there is no");

        //复制文件
        File srcFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
        HiFileUtils.copyFile(sourceImgFile, srcFile);

        ImageSource imageSource = ImageSource.create(srcFile, new ImageSource.SourceOptions());

        PixelMap pixelMap = imageSource.createPixelmap(new ImageSource.DecodingOptions());

        ImageSource.DecodingOptions pixelMapOpts = null;

        //等比缩放
        if (this.scale != null) {
            if (pixelMapOpts == null) {
                pixelMapOpts = new ImageSource.DecodingOptions();
            }
            ImageInfo imageInfo = pixelMap.getImageInfo();
            int width = (int) (imageInfo.size.width * this.scale);
            int height = (int) (imageInfo.size.height * this.scale);
            pixelMapOpts = new ImageSource.DecodingOptions();
            pixelMapOpts.desiredSize = new Size(width, height);
        } else {
            //缩放后大小
            if (this.imgSize != null) {
                if (pixelMapOpts == null) {
                    pixelMapOpts = new ImageSource.DecodingOptions();
                }
                pixelMapOpts.desiredSize = this.imgSize;
            }
        }


        //旋转度
        if (this.rotateDegrees != null) {
            if (pixelMapOpts == null) {
                pixelMapOpts = new ImageSource.DecodingOptions();
            }
            pixelMapOpts.rotateDegrees = this.rotateDegrees;
        }

        if (pixelMapOpts != null) {
            pixelMap = imageSource.createPixelmap(pixelMapOpts);
        }


        //返回文件
        File descFile = File.createTempFile(UUID.randomUUID().toString(), ".jpg");
        descFile = finalizePacking(pixelMap, descFile, this.quality == null ? 90 : this.quality);

        //压缩图片到指定大小
        if (maxFileSize != null) {
            while (true) {
                long length = descFile.length();
                //System.out.println("当前文件大小:"+  HiFileUtils.getReadableFileSize(length)+"  准备压缩到:"+  HiFileUtils.getReadableFileSize(maxFileSize)+"   this.quality:"+ this.quality);
                if (descFile.length() >= maxFileSize) {
                    if(this.quality == null){
                        this.quality = 90;
                    }else{
                        this.quality = this.quality - 5;
                    }
                    if(this.quality <= 10){
                        break;
                    }
                    descFile = finalizePacking(pixelMap, descFile, this.quality);
                } else {
                    break;
                }
            }
        }

        //删除文件
        srcFile.delete();


        return descFile;
    }

    /**
     * 压缩文件
     *
     * @param srcFile
     * @param quality
     * @return
     */
    private File finalizePacking(PixelMap pixelMap, File srcFile, int quality) throws FileNotFoundException {
        OutputStream outStream = new FileOutputStream(srcFile);
        ohos.media.image.ImagePacker.PackingOptions opts = new ohos.media.image.ImagePacker.PackingOptions();
        opts.quality = quality;
        ImagePacker imagePacker = ImagePacker.create();
        imagePacker.initializePacking(outStream, opts);
        imagePacker.addImage(pixelMap);
        imagePacker.finalizePacking();
        return srcFile;
    }

}
