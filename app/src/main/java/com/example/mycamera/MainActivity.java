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

import org.w3c.dom.Text;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    Uri imgUri ;
    ImageView iv, iv2;
    TextView  tv;

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
        startActivityForResult(myIntent,100);

    }
    public void clkbtnUri(View view) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            // ask for permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 200);
        }
        else {
            takePhotoAndSave();
        }
    }

    public void onRequestPermissionsResult ( int requestCode, String[] permissions, int[] grantResults) {
        Log.e ( "PHOTO", " check request permission result" ) ;
        if ( requestCode == 200 ) {
            Log.e ( "PHOTO", " checking now , request permission result" ) ;
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

    protected void onActivityResult(int reqCode, int resCode, Intent data){
        super.onActivityResult(reqCode, resCode, data);

        Log.e("onActivityResult", " here ");
        if (( resCode == Activity.RESULT_OK) && (reqCode == 100 ))  {
            Log.e("onActivityResult", " RESULT OK ");

            Toast.makeText(this, "ok photo taken", Toast.LENGTH_SHORT).show();


            if ((data!= null) && data.getExtras().containsKey("data")) {
                // show in imageview1
                Bundle myBundle = data.getExtras();
                Bitmap bmp = ( Bitmap) myBundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
                iv.setImageBitmap((bmp));

            }
            else {
             //   Log.e("onActivityResult", "after setImageBitmap bmp1");
                Log.e("imgUri ", imgUri.toString());
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
        }
        else {
            Log.e("onActivityResult", " not RESULT OK ");
            Toast.makeText(this, "no photo taken", Toast.LENGTH_SHORT).show();
        }

    }

    private void takePhotoAndSave () {
         imgUri = getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, new ContentValues()   );
    //    Intent myIntent = new Intent("android.media.action.IMAGE_CAPTURE");
        Intent myIntent = new  Intent( MediaStore.ACTION_IMAGE_CAPTURE);
        myIntent.putExtra( MediaStore.EXTRA_OUTPUT, imgUri);

        String strFullPath= getRealFilePath(getApplicationContext(), imgUri);
        String strFileName = strFullPath.substring(strFullPath.lastIndexOf("/") + 1, strFullPath.length());
        Toast.makeText(getApplicationContext(),  "fullpath:"+strFullPath+" filename:" + strFileName, Toast.LENGTH_LONG).show();
        tv.setText(" Fullpath:"+strFullPath+" \n filename:" + strFileName ) ;

        startActivityForResult(myIntent,100);
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