package com.feedback.handler;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonElement;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ViewScreenShot_Activity extends AppCompatActivity {

    static String Link;
    Toolbar toolbar;
    ImageView ivDrawImg;
    ScrollViewCustome scrollView;
    File file;
    TextInputEditText note;
    String Error_Product = "";
    String Error_Customer = "Android";
    RelativeLayout progress_circular, editpic;
    private String localClassName, localFragmentClassName;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //    DrawingView mDrawingView=new DrawingView(this);

        setContentView(R.layout.activity_view_screen_shot);
        Error_Product = getApplicationName(this);
        //   DrawingView  mDrawingView =findViewById(R.id.pic);
        note = findViewById(R.id.note);

        if (getSupportActionBar() == null) {
            toolbar = findViewById(R.id.toolbar);
            //     toolbar.setNavigationIcon(getResources().getDrawable(R.drawable.ic_baseline_arrow_back_24));
            toolbar.setTitleTextColor(getResources().getColor(R.color.white));
            setSupportActionBar(toolbar);
           /* toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });*/
            toolbar.setVisibility(View.VISIBLE);
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24);


        progress_circular = findViewById(R.id.progress_circular);
        file = (File) getIntent().getExtras().get("picture");
        localClassName = (String) getIntent().getStringExtra("localClassName");
        localFragmentClassName = (String) getIntent().getStringExtra("localFragmentClassName");
        userId = (String) getIntent().getStringExtra("userId");

        /*if (file.exists()) {
            String fp = file.getAbsolutePath();
            Drawable d = Drawable.createFromPath(file.getAbsolutePath());
            mDrawingView.setBackground(d);
        } else {
            System.out.println("File Not Found");
        }*/


        ivDrawImg = findViewById(R.id.iv_draw);
        editpic = findViewById(R.id.editpic);
        editpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewScreenShot_Activity.this, PhotoEdit.class);
                intent.putExtra("picture", file);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivityForResult(intent, 101);
            }
        });
        scrollView = findViewById(R.id.scrollView);
        Picasso.get().load(file).into(ivDrawImg);
        Button btnSave = (Button) findViewById(R.id.btn_save);
        Button btnResume = (Button) findViewById(R.id.btn_resume);


        btnSave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // Intent intent = new Intent();
                // setResult(Activity.RESULT_OK, intent);
                // intent.putExtra("picture", file);
                // intent.putExtra("localClassName", localClassName);
                //intent.putExtra("localFragmentClassName", localFragmentClassName);
                String notetext = note.getText().toString();
                if (notetext.isEmpty()) {
                    return;
                }
                localClassName = localClassName == null ? "" : localClassName;
                localFragmentClassName = localFragmentClassName == null ? "" : localFragmentClassName;
                notetext = notetext == null ? "" : notetext;
                SendFeedback(file, localClassName, localFragmentClassName, notetext);

                // saveImg(null);

            }
        });

        btnResume.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });


    }

    public String getApplicationName(Context context) {
        ApplicationInfo applicationInfo = context.getApplicationInfo();
        int stringId = applicationInfo.labelRes;
        return stringId == 0 ? applicationInfo.nonLocalizedLabel.toString() : context.getString(stringId);
    }

    private void SendFeedback(File file, String localClassName, String localFragmentClassName, String notetext) {
        progress_circular.setVisibility(View.VISIBLE);
        //String Error_Product = "OfferSwiper";
        String Error_Customer = "Android";
        MultipartBody.Part part = null;


        RequestBody requestImageFile = RequestBody.create(MediaType.parse("image/*"), file);
        part = MultipartBody.Part.createFormData("any_name_for_the_part", file.getName(), requestImageFile);

        Retrofit retrofit = RetrofitClientInstanceWithLink.getRetrofitInstance(Link);
        ErrorHandler_Client errorHandler_client = retrofit.create(ErrorHandler_Client.class);
        Call<JsonElement> call = errorHandler_client.SendFeedback(part,
                localClassName,
                localFragmentClassName,
                notetext,
                Error_Product,
                Error_Customer,
                userId);

        call.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                //  progress_circular.setVisibility(View.GONE);
                finish();
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                progress_circular.setVisibility(View.GONE);
                Toast.makeText(ViewScreenShot_Activity.this, "Not Sent Try Again", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    private void saveImg(Bitmap image) {

        if (image == null) {
            image = Bitmap.createBitmap(ivDrawImg.getWidth(), ivDrawImg.getHeight(), Bitmap.Config.RGB_565);
        } else {

        }
        ivDrawImg.draw(new Canvas(image));

        String uri = MediaStore.Images.Media.insertImage(getContentResolver(), image, "title", null);

        Log.e("uri", uri);


        try {
            // Save the image to the SD card.

            File folder = new File(Environment.getExternalStorageDirectory() + "/DrawTextOnImg");

            if (!folder.exists()) {
                folder.mkdir();
                //folder.mkdirs();  //For creating multiple directories
            }

            File file = new File(Environment.getExternalStorageDirectory() + "/DrawTextOnImg/tempImg.png");

            FileOutputStream stream = new FileOutputStream(file);
            image.compress(Bitmap.CompressFormat.PNG, 100, stream);
            Toast.makeText(this, "Picture saved", Toast.LENGTH_SHORT).show();

            // Android equipment Gallery application will only at boot time scanning system folder
            // The simulation of a media loading broadcast, for the preservation of images can be viewed in Gallery

            /*Intent intent = new Intent();
            intent.setAction(Intent.ACTION_MEDIA_MOUNTED);
            intent.setData(Uri.fromFile(Environment.getExternalStorageDirectory()));
            sendBroadcast(intent);*/

        } catch (Exception e) {
            Toast.makeText(this, "Save failed", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101) {
            if (resultCode == Activity.RESULT_OK) {
                File file = (File) data.getExtras().get("result");
                this.file = file;
                Picasso.get().load(file).into(ivDrawImg);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult
}

