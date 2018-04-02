package com.example.lenovo.pathphoto;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
//    上传路径
    private String url = "http://12.16.5.3:8080/UploadDemo4/UploadFile";
    private final int IMAGE_REQUEST_IMAGE = 1;
    private final int REQUEST_IMAGE_CAPTURE = 2;
    private final int PHOTO_REQUEST_CUT = 3;

    private PopupWindow window;
    private File file;
    private Uri imageUri;
    private View view;
    private TextView up;
    private TextView upload;
    private ImageView image;
    private LinearLayout line;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = LayoutInflater.from(this).inflate(R.layout.popwindow, null);
        initView();
        initListener();
    }

    private void initListener() {
        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                window = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                window.setFocusable(true);
                window.setBackgroundDrawable(new BitmapDrawable());
                window.setAnimationStyle(R.style.popanimation);
                window.showAtLocation(line, Gravity.BOTTOM, 0, 0);

                TextView take=view.findViewById(R.id.take_photo);
                take.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent it = new Intent();
                        it.setAction(MediaStore.ACTION_IMAGE_CAPTURE);

                        file = new File(Environment.getExternalStorageDirectory().getPath(), "h3.jpg");
                        imageUri = Uri.fromFile(file);
                        // 以键值对的形式告诉系统照片保存的地址，键的名称不能随便写
                        it.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(it, REQUEST_IMAGE_CAPTURE);
                        window.dismiss();
                    }
                });
                TextView picture=view.findViewById(R.id.get_photo);
                picture.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent2 = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent2, IMAGE_REQUEST_IMAGE);
                        window.dismiss();
                    }
                });
                TextView dis=view.findViewById(R.id.finish);
                dis.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        window.dismiss();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case IMAGE_REQUEST_IMAGE://这里的requestCode是我自己设置的，就是确定返回到那个Activity的标志
                startPhotoZoom(data.getData());
                break;
            case PHOTO_REQUEST_CUT:
                Uri mImageCaptureUri = data.getData();
                String path = getBitmap(mImageCaptureUri);
                upLoadData(path);
                Bitmap photoBmp = null;
                try {
                    photoBmp = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), mImageCaptureUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                image.setImageBitmap(photoBmp);
                break;
            case REQUEST_IMAGE_CAPTURE:
                Intent intent1 = new Intent("com.android.camera.action.CROP");
                intent1.setDataAndType(imageUri, "image/*");
                intent1.putExtra("scale", true);
                //intent1.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                intent1.putExtra("return-data", false);
                startActivityForResult(intent1, 1);// 启动裁剪程序
                break;
        }
    }

    private void initView() {
        up = (TextView) findViewById(R.id.up);
        image = (ImageView) findViewById(R.id.image);
        upload = (TextView) findViewById(R.id.upload);
        up = (TextView) findViewById(R.id.up);
        upload = (TextView) findViewById(R.id.upload);
        image = (ImageView) findViewById(R.id.image);
        line = (LinearLayout) findViewById(R.id.line);
        up = (TextView) findViewById(R.id.up);
        upload = (TextView) findViewById(R.id.upload);
         image = (ImageView) findViewById(R.id.image);
         line = (LinearLayout) findViewById(R.id.line);

    }

    private void startPhotoZoom(Uri uri) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // crop为true是设置在开启的intent中设置显示的view可以剪裁
        intent.putExtra("crop", "true");

        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);

        // outputX,outputY 是剪裁图片的宽高
        intent.putExtra("outputX", 300);
        intent.putExtra("outputY", 300);
        intent.putExtra("return-data", false);
//        intent.putExtra("circleCrop", true);
        intent.putExtra("noFaceDetection", true);
        startActivityForResult(intent, PHOTO_REQUEST_CUT);
    }
    public void upLoadData(String path){
        OkHttpClient mOkHttpClent = new OkHttpClient();
        File file = new File(path);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("img", file.getName(), RequestBody.create(MediaType.parse("image/jpeg"), file));

        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Call call = mOkHttpClent.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("TAG", "onFailure: "+e );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.e("TAG", "成功"+response);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "成功", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
    public String getBitmap(Uri selectedImage){
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);//从系统表中查询指定Uri对应的照片
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        String path = cursor.getString(columnIndex);  //获取照片路径
        Log.e("TAG", "path: =======================" + path);
        cursor.close();
        return path;
    }
}
