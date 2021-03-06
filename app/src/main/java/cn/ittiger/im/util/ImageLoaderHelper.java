package cn.ittiger.im.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * 图片参数帮助类
 *
 */
public class ImageLoaderHelper {

    /*
    String imageUri = "http://site.com/image.png"; // 网络图片
    String imageUri = "file:///mnt/sdcard/image.png"; // sd卡图片
    String imageUri = "content://media/external/audio/albumart/13"; //  content provider
    String imageUri = "assets://image.png"; // assets文件夹图片
    String imageUri = "drawable://" + R.drawable.image; // drawable图片
    */
//    private static volatile DisplayImageOptions sImageOptions;
//
//    public static DisplayImageOptions getChatImageOptions() {
//
//        if(sImageOptions == null) {
//            synchronized (ImageLoaderHelper.class) {
//                if(sImageOptions == null) {
//                    sImageOptions = new DisplayImageOptions.Builder()
//                            .cacheOnDisk(true)//图片下载后是否缓存到SDCard
//                            .cacheInMemory(true)//图片下载后是否缓存到内存
//                            .bitmapConfig(Bitmap.Config.RGB_565)//图片解码类型，推荐此种方式，减少OOM
//                            .considerExifParams(true)//是否考虑JPEG图像EXIF参数（旋转，翻转）
//                            .resetViewBeforeLoading(true)//设置图片在下载前是否重置，复位
//                            .showImageOnFail(R.drawable.vector_default_image)//图片加载失败后显示的图片
//                            .showImageOnLoading(R.drawable.vector_default_image)
//                            .build();
//                }
//            }
//        }
//        return sImageOptions;
//    }

    public static void displayImage(Context content, ImageView imageView, String url) {
        Glide.with(content).
                load(url)
                .into(imageView);

//        displayImage(imageView, url, null);
    }

//    public static void displayImage(ImageView imageView, String url, ImageLoadingListener imageLoadingListener) {
//
//        ImageLoader.getInstance().displayImage(url, imageView, getChatImageOptions(), imageLoadingListener);
//    }
}
