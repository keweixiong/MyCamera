package com.example.mycamera;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_NOURI = 100;
    private static final int REQUEST_CODE_URI = 200;
    Uri imgUri ;
    ImageView iv, iv2;
    TextView  tv;


    // 本程序是照相示范， 1 NO URI 仅照相，相片不保存在手机上，
    // // 2 URI ， 保存在手机上， 需要访问 外部存储授权， 需要生成 URI 。
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

         iv = findViewById(R.id.imageView);
         iv2 = findViewById(R.id.imageView2);
         tv = findViewById(R.id.textView);

    }

    public void clkbtnNoUri(View view) {
        Intent myIntent = new  Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(myIntent, REQUEST_CODE_NOURI);

    }
    public void clkbtnUri(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_URI);
        }
        else {
            takePhotoAndSave();
        }
    }

    // 用户授权的结果返回，回调此处
    public void onRequestPermissionsResult ( int requestCode, String[] permissions, int[] grantResults) {
        if ( requestCode == REQUEST_CODE_URI) {
            if ( grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e ( "PHOTO", "  granted ");
                takePhotoAndSave();
            }
            else {
                Log.e ( "PHOTO", " not granted ");
                Toast.makeText(this, "need grant permission", Toast.LENGTH_SHORT).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        Log.e("onActivityResult", " to process result code ");
        if ( resultCode  != Activity.RESULT_OK) {
            Log.e("onActivityResult", " not RESULT OK ");
            Toast.makeText(this, "take photo  RESULT not ok", Toast.LENGTH_SHORT).show();
            return;
        }

        //使用with uri, 和 no uri方式，在处理返回结果的处理方式不一样。 如果用了uri，就不能够get("data")了，此时data是空的。
        if (requestCode == REQUEST_CODE_NOURI) {
            Log.e("NOURI", " RESULT CODE OK ");

            // Toast.makeText(this, "ok photo taken", Toast.LENGTH_SHORT).show();

            if ((data != null) && data.getExtras().containsKey("data")) {
                // show in imageview1
                Bundle myBundle = data.getExtras();
                Bitmap bmp = (Bitmap) myBundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                iv.setImageBitmap((bmp));

            } else {
                Log.e("NOURI", " data is null ");

            }
        }
        else if ( requestCode == REQUEST_CODE_URI) {
             //   Log.e("onActivityResult", "after setImageBitmap bmp1");
                Log.e("URI imgUri ", imgUri.toString());    //此文件名，是照相的时候已经获得的。
                // if getData() is null, show in imageview2
                Bitmap bmp2 = null;

                try {
                    bmp2 = BitmapFactory.decodeStream(
                            getContentResolver().openInputStream(imgUri), null, null
                    );
                } catch ( IOException e) {
                    Toast.makeText(this, " cannot resolve photo", Toast.LENGTH_SHORT).show();
                }

                Log.e("onActivityResult", "before setImageBitmap bmp2");
                iv2.setImageBitmap(bmp2);
        }
        else {
            Log.e("onActivityResult", "should not come here , something wrong ");

        }

    }

    private void takePhotoAndSave () {

        // 此处生成imgUri, 本应用通知照相应用，把相片保存在此文件名。

         imgUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues()   );
    //    Intent myIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        Intent myIntent = new  Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        myIntent.putExtra( MediaStore.EXTRA_OUTPUT, imgUri);


        String strFullPath= getRealFilePath(getApplicationContext(), imgUri);  // from URI address -> normal filepath
        String strFileName = strFullPath.substring(strFullPath.lastIndexOf("/") + 1, strFullPath.length());
        // Toast.makeText(getApplicationContext(),  "fullpath:"+strFullPath+" filename:" + strFileName, Toast.LENGTH_LONG).show();
        tv.setText(" Fullpath:"+strFullPath+" \n filename:" + strFileName ) ;

        startActivityForResult(myIntent, REQUEST_CODE_URI);
    }

    // get the real path of Uri,
    public static String getRealFilePath(final Context context, final Uri uri ) {
        Log.e("getpath",  uri.toString());
 //       Toast.makeText(context,  uri.toString(), Toast.LENGTH_LONG).show();

        if ( null == uri ) return null;
        final String scheme = uri.getScheme();
        String data = null;
        if ( scheme == null )
            data = uri.getPath();
        else if ( ContentResolver.SCHEME_FILE.equals( scheme ) ) {
            data = uri.getPath();
        } else if ( ContentResolver.SCHEME_CONTENT.equals( scheme ) ) {
            Cursor cursor = context.getContentResolver().query( uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null );
            if ( null != cursor ) {
                if ( cursor.moveToFirst() ) {
                    int index = cursor.getColumnIndex( MediaStore.Images.ImageColumns.DATA );
                    if ( index > -1 ) {
                        data = cursor.getString( index );
                    }
                }
                cursor.close();
            }
        }
        return data;
    }
}