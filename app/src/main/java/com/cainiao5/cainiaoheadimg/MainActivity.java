package com.cainiao5.cainiaoheadimg;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import java.io.File;

public class MainActivity extends AppCompatActivity {
    /** 指定拍摄图片文件位置避免获取到缩略图 */
    private File outFile;
    /** 标记是拍照还是相册0 是拍照1是相册 */
    private int cameraorpic;
    /** 选择头像相册选取 */
    private static final int REQUESTCODE_PICK = 1;
    /** 裁剪好头像-设置头像 */
    private static final int REQUESTCODE_CUTTING = 2;
    /** 选择头像拍照选取 */
    private static final int PHOTO_REQUEST_TAKEPHOTO = 3;
    /** 裁剪好的头像的Bitmap */
    private Bitmap currentBitmap;
    /** 圆形图片的Imageview */

    private  CircleImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        iv= (CircleImageView) findViewById(R.id.iv);


        iv.setBorderWidth(5);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ActionSheetDialog(MainActivity.this).Builder()

                        .addSheetItem("拍照", ActionSheetDialog.SheetItemColor.BULE, new ActionSheetDialog.OnSheetItemClickListener() {
                            @Override
                            public void onClick(int witch) {
                                cameraorpic = 1;
                                openCamera();
                            }
                        }).addSheetItem("打开相册",ActionSheetDialog.SheetItemColor.BULE, new ActionSheetDialog.OnSheetItemClickListener() {
                    @Override
                    public void onClick(int witch) {
                        cameraorpic = 0;
                        openPic();
                    }
                }).show();
            }
        });
    }

    /**
     * 打开相册
     */
    private void openPic() {
        Intent pickIntent = new Intent(Intent.ACTION_PICK, null);
        pickIntent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
        startActivityForResult(pickIntent, REQUESTCODE_PICK);

    }

    /**
     * 打开相机
     */
    private void openCamera() {
        String state = Environment.getExternalStorageState();
        if (state.equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File outDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            if (!outDir.exists()) {
                outDir.mkdirs();
            }
            outFile = new File(outDir, System.currentTimeMillis() + ".jpg");
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(outFile));
            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(intent, PHOTO_REQUEST_TAKEPHOTO);
        } else {
            Log.e("CAMERA", "请确认已经插入SD卡");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  进行判断是那个操作跳转回来的，如果是裁剪跳转回来的这块就要把图片现实到View上，其他两种的话都把数据带入裁剪界面
        switch (requestCode) {
            //相册
            case REQUESTCODE_PICK:
                if (data == null || data.getData() == null) {
                    return;
                }
                startPhotoZoom(data.getData());
                break;
            //裁剪
            case REQUESTCODE_CUTTING:
                if (data != null) {
                    setPicToView(data);
                }
                break;
            //拍照
            case PHOTO_REQUEST_TAKEPHOTO:
                startPhotoZoom(Uri.fromFile(outFile));
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 把裁剪好的图片设置到View上或者上传到网络
     * @param data
     */
    private void setPicToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            /** 可用于图像上传 */
            currentBitmap = extras.getParcelable("data");

            iv.setImageBitmap(currentBitmap);
        }
    }

    /**
     * 调用系统的图片裁剪
     * @param data
     */
    private void startPhotoZoom(Uri data) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(data, "image/*");
        intent.putExtra("crop", true);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("scale", true);//黑边
        intent.putExtra("scaleUpIfNeeded", true);//黑边
        intent.putExtra("return-data", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, REQUESTCODE_CUTTING);

    }
}
