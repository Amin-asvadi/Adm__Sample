package com.example.admmanager;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.downloader.Status;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static String dirPath;
    Button buttonTwo,
            buttonCancelTwo;
    EditText edurl;
    TextView textViewProgressTwo;

    ProgressBar progressBarTwo;
    String URL;
    int downloadIdTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        dirPath = Utils.getRootDirPath(getApplicationContext());

        init();


        onClickListenerTwo();

    }


    private void init() {
        edurl = findViewById(R.id.edurl);

        buttonTwo = findViewById(R.id.buttonTwo);


        buttonCancelTwo = findViewById(R.id.buttonCancelTwo);


        textViewProgressTwo = findViewById(R.id.textViewProgressTwo);


        progressBarTwo = findViewById(R.id.progressBarTwo);
        URL = edurl.getText().toString().trim();
    }

    public void onClickListenerTwo() {
        buttonTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://google.com")
                        .addConverterFactory(GsonConverterFactory.create());
                Retrofit retrofit = builder.build();
                RetrofitInterface downloadService = retrofit.create(RetrofitInterface.class);
                Call<ResponseBody> call = downloadService.downloadFileByUrl(URL);

                String chackformat = URL.substring(URL.lastIndexOf('.') + 1);
                if (chackformat.isEmpty()) {
                    Toast.makeText(MainActivity.this, "لینک را بررسی نمایدد", Toast.LENGTH_SHORT).show();

                } else if (chackformat.equals("mp3") || chackformat.equals("mp4") || chackformat.equals("rar")) {
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                            if (response.isSuccessful()) {

                                edurl.setText("");
                                Toast.makeText(getApplicationContext(), "در حال دانلود", Toast.LENGTH_SHORT).show();


                                if (Status.RUNNING == PRDownloader.getStatus(downloadIdTwo)) {
                                    PRDownloader.pause(downloadIdTwo);
                                    return;
                                }


                                buttonTwo.setEnabled(false);
                                progressBarTwo.setIndeterminate(true);
                                progressBarTwo.getIndeterminateDrawable().setColorFilter(
                                        Color.BLUE, android.graphics.PorterDuff.Mode.SRC_IN);


                                if (Status.PAUSED == PRDownloader.getStatus(downloadIdTwo)) {
                                    PRDownloader.resume(downloadIdTwo);
                                    return;
                                }


                                downloadIdTwo = PRDownloader.download(URL, dirPath, "wechat.apk")
                                        .build()
                                        .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                                            @Override
                                            public void onStartOrResume() {
                                                progressBarTwo.setIndeterminate(false);
                                                buttonTwo.setEnabled(true);
                                                buttonTwo.setText(R.string.pause);
                                                buttonCancelTwo.setEnabled(true);
                                                buttonCancelTwo.setText(R.string.cancel);
                                            }
                                        })
                                        .setOnPauseListener(new OnPauseListener() {
                                            @Override
                                            public void onPause() {
                                                buttonTwo.setText(R.string.resume);
                                            }
                                        })
                                        .setOnCancelListener(new OnCancelListener() {
                                            @Override
                                            public void onCancel() {
                                                downloadIdTwo = 0;
                                                buttonTwo.setText(R.string.start);
                                                buttonCancelTwo.setEnabled(false);
                                                progressBarTwo.setProgress(0);
                                                textViewProgressTwo.setText("");
                                                progressBarTwo.setIndeterminate(false);
                                            }
                                        })
                                        .setOnProgressListener(new OnProgressListener() {
                                            @Override
                                            public void onProgress(Progress progress) {
                                                long progressPercent = progress.currentBytes * 100 / progress.totalBytes;
                                                progressBarTwo.setProgress((int) progressPercent);
                                                textViewProgressTwo.setText(Utils.getProgressDisplayLine(progress.currentBytes, progress.totalBytes));
                                            }
                                        })
                                        .start(new OnDownloadListener() {
                                            @Override
                                            public void onDownloadComplete() {
                                                buttonTwo.setEnabled(false);
                                                buttonCancelTwo.setEnabled(false);
                                                buttonTwo.setText(R.string.completed);
                                            }

                                            @Override
                                            public void onError(Error error) {
                                                buttonTwo.setText(R.string.start);
                                                Toast.makeText(getApplicationContext(), getString(R.string.some_error_occurred) + " " + "2", Toast.LENGTH_SHORT).show();
                                                textViewProgressTwo.setText("");
                                                progressBarTwo.setProgress(0);
                                                downloadIdTwo = 0;
                                                buttonCancelTwo.setEnabled(false);
                                                progressBarTwo.setIndeterminate(false);
                                                buttonTwo.setEnabled(true);
                                            }
                                        });


                            } else {
                                Toast.makeText(MainActivity.this, "مشکل در برقراری ارتباط", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            t.printStackTrace();
                            Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();

                        }
                    });
                } else {
                    edurl.setText("");

                    Toast.makeText(MainActivity.this, "لینک را بررسی نمایدد", Toast.LENGTH_SHORT).show();
                }


            }
        });

        buttonCancelTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PRDownloader.cancel(downloadIdTwo);
            }
        });
    }


}