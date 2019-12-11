package com.boss.qna.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.boss.qna.Adapters.AnswerAdapter;
import com.boss.qna.Adapters.QuestionAdapter;
import com.boss.qna.Models.Answer;
import com.boss.qna.Models.Question;
import com.boss.qna.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.nbsp.materialfilepicker.MaterialFilePicker;
import com.nbsp.materialfilepicker.ui.FilePickerActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String IP = "http://192.168.1.4:3000";

    private static final String ANSWER_URL = IP + "/getAnswers";
    private static final String PDF_CONTENT_URL = IP + "/uploadPDFContentAndroid";
    private static final String TXT_CONTENT_URL = IP + "/uploadTXTContentAndroid";
    private static final String DOCX_CONTENT_URL = IP + "/uploadDOCXContentAndroid";
    private static final String MANUAL_QUESTION_URL = IP + "/uploadManualQuestionAndroid";
    private static final String TXT_QUESTION_URL = IP + "/uploadTXTQuestionAndroid";
    private static final String DOCX_QUESTION_URL = IP + "/uploadDOCXQuestionAndroid";

    private static final int CONTENT_CODE = 111;
    private static final int QUESTION_CODE = 333;
    Toolbar toolbar;
    Button answer, content, question, clear;
    TextView textContent, textQuestion;
    CardView chooseFile, addManually;
    TextView chooseFileText, addManuallyText;
    RecyclerView recyclerView;
    FloatingActionButton floatingActionButton;
    ProgressDialog progressDialog;
    QuestionAdapter questionAdapter;
    AnswerAdapter answerAdapter;
    ArrayList<Question> questionsArrayList;
    ArrayList<Answer> answerArrayList;
    AlertDialog dialogChooser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.mytoolbar);
        // Do this to make the menu options visible in custom toolbar
        content = findViewById(R.id.content);
        question = findViewById(R.id.question);
        answer = findViewById(R.id.answer);
        clear = findViewById(R.id.clear);
        textContent = findViewById(R.id.contentText);
        textQuestion = findViewById(R.id.questionText);
        // Init arraylist
        questionsArrayList = new ArrayList<>();
        answerArrayList = new ArrayList<>();
        // Change Fonts
        final Typeface tf = Typeface.createFromAsset(getAssets(), "Montserrat-Bold.otf");
        Typeface tfText = Typeface.createFromAsset(getAssets(), "ColabLig.otf");
        content.setTypeface(tf);
        question.setTypeface(tf);
        answer.setTypeface(tf);
        textContent.setTypeface(tfText);
        textQuestion.setTypeface(tfText);
        // Work when content button is clicked
        content.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    // Toast.makeText(MainActivity.this, "Granted Already", Toast.LENGTH_SHORT).show();
                    upload("content");
                } else {
                    requestStoragePermission(CONTENT_CODE);
                }
            }
        });

        // Work when question button is clicked
        question.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (textQuestion.getText().toString().equals("No File Chosen")) {
                    // Open Dialog
                    final View dialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null, false);
                    chooseFile = dialogView.findViewById(R.id.chooseFile);
                    addManually = dialogView.findViewById(R.id.addManually);
                    chooseFileText = dialogView.findViewById(R.id.chooseFileText);
                    addManuallyText = dialogView.findViewById(R.id.addManuallyText);
                    // Set Type of TextViews inside the dialogView
                    addManuallyText.setTypeface(tf);
                    chooseFileText.setTypeface(tf);
                    AlertDialog.Builder alertChooser = new AlertDialog.Builder(MainActivity.this);
                    // this is set the view from XML inside AlertDialog
                    alertChooser.setView(dialogView);
                    // disallow cancel of AlertDialog on click of back button and outside touch
                    alertChooser.setCancelable(false);
                    alertChooser.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });

                    dialogChooser = alertChooser.create();
                    dialogChooser.show();

                    addManually.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialogChooser.dismiss();
                            final View addQuestionView = getLayoutInflater().inflate(R.layout.add_question, null, false);
                            recyclerView = addQuestionView.findViewById(R.id.rv);
                            floatingActionButton = addQuestionView.findViewById(R.id.fab);
                            // question arraylist is empty initially
                            questionAdapter = new QuestionAdapter(questionsArrayList, MainActivity.this);
                            recyclerView.setAdapter(questionAdapter);
                            recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));

                            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // open another dialog box to take in the question
                                    final View enterQuestionView = getLayoutInflater().inflate(R.layout.question_dialog_layout, null, false);
                                    final EditText editQuestionText = enterQuestionView.findViewById(R.id.editQuestion);
                                    final AlertDialog.Builder alertEnterQuestion = new AlertDialog.Builder(MainActivity.this);
                                    // this is set the view from XML inside AlertDialog
                                    alertEnterQuestion.setView(enterQuestionView);
                                    // disallow cancel of AlertDialog on click of back button and outside touch
                                    alertEnterQuestion.setCancelable(false);
                                    alertEnterQuestion.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                                    alertEnterQuestion.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Add question to arraylist and update the recycler view, also dismiss this dialog
                                            questionsArrayList.add(new Question(editQuestionText.getText().toString()));
                                            questionAdapter = new QuestionAdapter(questionsArrayList, MainActivity.this);
                                            recyclerView.setAdapter(questionAdapter);
                                            dialog.dismiss();
                                        }
                                    });
                                    final AlertDialog dialogEnterQuestion = alertEnterQuestion.create();
                                    dialogEnterQuestion.show();
                                }
                            });

                            final AlertDialog.Builder alertQuestion = new AlertDialog.Builder(MainActivity.this);
                            // this is set the view from XML inside AlertDialog
                            alertQuestion.setView(addQuestionView);
                            alertQuestion.setTitle("Add Question");
                            // disallow cancel of AlertDialog on click of back button and outside touch
                            alertQuestion.setCancelable(false);
                            alertQuestion.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Cancel implies user doesn't want to add questions
                                    questionsArrayList.clear();
                                    Toast.makeText(MainActivity.this, "No questions added", Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });
                            alertQuestion.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // Cancel implies user doesn't want to add ANY MORE questions
                                    if (questionsArrayList.size() >= 1) {
                                        Toast.makeText(MainActivity.this, questionsArrayList.size() + " questions added", Toast.LENGTH_LONG).show();
                                        textQuestion.setText("Upload " + questionsArrayList.size() + " question(s)");
                                    } else {
                                        Toast.makeText(MainActivity.this, "No questions added", Toast.LENGTH_LONG).show();
                                    }
                                    dialog.dismiss();
                                }
                            });
                            final AlertDialog dialogQuestion = alertQuestion.create();
                            dialogQuestion.show();
                        }
                    });

                    chooseFile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request storage permission if not asked already else upload the questions
                            if (ContextCompat.checkSelfPermission(MainActivity.this,
                                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                upload("questionChosen");
                            } else {
                                requestStoragePermission(QUESTION_CODE);
                            }
                        }
                    });
                } else {
                    upload("questionManual");
                }
            }
        });

        // Work when get answers button is clicked
        answer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (textContent.getText().toString().equals("No File Chosen") || textQuestion.getText().toString().equals("No File Chosen")) {
                    Toast.makeText(MainActivity.this, "Upload all files", Toast.LENGTH_SHORT).show();
                } else {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog = new ProgressDialog(MainActivity.this);
                                    progressDialog.setTitle("Processing");
                                    progressDialog.setMessage("Please Wait ...");
                                    progressDialog.setCancelable(false);
                                    progressDialog.show();

                                    OkHttpClient.Builder builder = new OkHttpClient.Builder();
                                    builder.connectTimeout(10, TimeUnit.MINUTES) // connect timeout
                                            .writeTimeout(10, TimeUnit.MINUTES) // write timeout
                                            .readTimeout(10, TimeUnit.MINUTES); // read timeout

                                    OkHttpClient client = builder.build();

                                    Request request = new Request.Builder()
                                            .url(ANSWER_URL)
                                            .build();

                                    client.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                            // Hide Progress
                                            progressDialog.dismiss();
                                            Log.e("KEY", String.valueOf(e));
                                            call.cancel();
                                        }

                                        @Override
                                        public void onResponse(Call call, final Response response) throws IOException {
                                            // Hide Progress
                                            progressDialog.dismiss();
                                            final String myResponse = response.body().string();
                                            Log.e("KEY", myResponse);
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    // JSON Response
                                                    try {
                                                        JSONObject jsonObject = new JSONObject(myResponse);
                                                        Log.e("JS0N", jsonObject.length() + "");
                                                        // update answers arraylist
                                                        for (int i = 1; i < jsonObject.length(); i += 2) {
                                                            answerArrayList.add(new Answer((String) jsonObject.get(i + "")));
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    // update the adapter
                                                    answerAdapter = new AnswerAdapter(answerArrayList);
                                                    // inflate dialog box
                                                    final View answerView = getLayoutInflater().inflate(R.layout.answer_display_layout, null, false);
                                                    RecyclerView answerRecyclerView = answerView.findViewById(R.id.rvans);
                                                    answerRecyclerView.setAdapter(answerAdapter);
                                                    answerRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));
                                                    // Alert Dialog Box
                                                    final AlertDialog.Builder alertAnswer = new AlertDialog.Builder(MainActivity.this);
                                                    // this is set the view from XML inside AlertDialog
                                                    alertAnswer.setView(answerView);
                                                    alertAnswer.setTitle("Answers");
                                                    // disallow cancel of AlertDialog on click of back button and outside touch
                                                    alertAnswer.setCancelable(false);
                                                    alertAnswer.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            // Cancel implies user doesn't want to add questions
                                                            answerArrayList.clear();
                                                        }
                                                    });
                                                    final AlertDialog dialogAnswer = alertAnswer.create();
                                                    dialogAnswer.show();
                                                }
                                            });
                                        }
                                    });
                                }
                            });
                        }
                    });
                    thread.start();
                }
            }
        });

        // Work when reset button is clicked
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Resetting Form", Toast.LENGTH_SHORT).show();
                // Reset Content Button and Text
                content.setText("UPLOAD CONTENT FILE");
                content.setClickable(true);
                content.setAlpha(1f);
                textContent.setText("No File Chosen");
                // Reset Question Button and Text
                question.setText("UPLOAD QUESTION FILE");
                question.setClickable(true);
                question.setAlpha(1f);
                textQuestion.setText("No File Chosen");
                // Empty the arraylist
                questionsArrayList = new ArrayList<>();
                answerArrayList = new ArrayList<>();
            }
        });
    }

    // Request permissions on runtime
    private void requestStoragePermission(final int inputCode) {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("These permission are needed to upload the files.")
                    .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, inputCode);
                        }
                    })
                    .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        } else {
            // request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, inputCode);
        }
    }

    // Action when permission has been granted
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == CONTENT_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                upload("content");
            } else {
                // Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == QUESTION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "Permission Granted.", Toast.LENGTH_SHORT).show();
                upload("question");
            } else {
                // Toast.makeText(this, "Permission Denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Upload function which handles the Upload Logic
    private void upload(String type) {

        if (type.equals("content")) {
            new MaterialFilePicker()
                    .withActivity(MainActivity.this)
                    .withRequestCode(CONTENT_CODE)
                    .start();
        } else if (type.equals("questionManual")) {
            // Upload PDF file using a background thread
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressDialog = new ProgressDialog(MainActivity.this);
                            progressDialog.setTitle("Uploading Question File");
                            progressDialog.setMessage("Please Wait ...");
                            progressDialog.setCancelable(false);
                            progressDialog.show();
                        }
                    });

                    OkHttpClient.Builder okhttpBuilder = new OkHttpClient.Builder();
                    okhttpBuilder.connectTimeout(10, TimeUnit.MINUTES) // connect timeout
                            .writeTimeout(10, TimeUnit.MINUTES) // write timeout
                            .readTimeout(10, TimeUnit.MINUTES); // read timeout

                    OkHttpClient client = okhttpBuilder.build();

                    // Initialize Builder (not RequestBody)
                    FormBody.Builder builder = new FormBody.Builder();

                    // loop while Adding Params to Builder
                    int index = 0;
                    for (Question question : questionsArrayList) {
                        builder.add("" + index, question.ques);
                        index++;
                    }

                    // Create RequestBody
                    RequestBody requestBody = builder.build();

                    Request request = new Request.Builder()
                            .url(MANUAL_QUESTION_URL)
                            .post(requestBody)
                            .build();

                    client.newCall(request).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            Log.e("ERROR OKHTTP", String.valueOf(e));
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onResponse(Call call, final Response response) throws IOException {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(MainActivity.this, "File Uploaded Successfully", Toast.LENGTH_SHORT).show();
                                }
                            });
                            final String myResponse = response.body().string();
                            Log.e("RESPONSE OKHTTP", myResponse);
                            progressDialog.dismiss();
                            textQuestion.setText("Uploaded " + questionsArrayList.size() + " question(s)");
                            question.setText("QUESTIONS UPLOADED");
                            question.setClickable(false);
                            question.setAlpha(.5f);
                        }
                    });
                }
            });
            thread.start();
        } else if (type.equals("questionChosen")) {
            new MaterialFilePicker()
                    .withActivity(MainActivity.this)
                    .withRequestCode(QUESTION_CODE)
                    .start();
        }
    }

    // Implements Upload Logic for Content File
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable final Intent data) {
        if (requestCode == CONTENT_CODE && resultCode == RESULT_OK) {

            // Upload Content file using a background thread
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {

                    File file = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
                    // String content_type = getMimeType(file.getPath());

                    final String file_path = file.getAbsolutePath();
                    Log.e("PATH", file_path);
                    String extension = file_path.substring(file_path.length() - 3);
                    Log.e("PATH", extension);
                    if (extension.equals("pdf") || extension.equals("PDF")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setTitle("Uploading Content File");
                                progressDialog.setMessage("Please Wait ...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                            }
                        });

                        // Concatenate white spaces otherwise error will prompt and app will crash
                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        builder.connectTimeout(10, TimeUnit.MINUTES) // connect timeout
                                .writeTimeout(10, TimeUnit.MINUTES) // write timeout
                                .readTimeout(10, TimeUnit.MINUTES); // read timeout

                        OkHttpClient client = builder.build();
                        RequestBody file_body = RequestBody.create(MediaType.parse("application/pdf"), file);

                        Log.e("File Name", file_path.substring(file_path.lastIndexOf("/") + 1));

                        RequestBody request_body = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("type", "application/pdf")
                                .addFormDataPart("file-name", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                                .build();

                        Request request = new Request.Builder()
                                .url(PDF_CONTENT_URL)
                                .post(request_body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("ERROR OKHTTP", String.valueOf(e));
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "PDF File Uploaded Successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                                final String myResponse = response.body().string();
                                Log.e("RESPONSE OKHTTP", myResponse);
                                progressDialog.dismiss();
                                // Change Content TextView, change Button Text and Disable it
                                textContent.setText(file_path.substring(file_path.lastIndexOf("/") + 1));
                                content.setText("CONTENT UPLOADED");
                                content.setClickable(false);
                                content.setAlpha(.5f);
                            }
                        });
                    } else if (extension.equals("txt") || extension.equals("TXT")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setTitle("Uploading Content File");
                                progressDialog.setMessage("Please Wait ...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                            }
                        });

                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        builder.connectTimeout(10, TimeUnit.MINUTES) // connect timeout
                                .writeTimeout(10, TimeUnit.MINUTES) // write timeout
                                .readTimeout(10, TimeUnit.MINUTES); // read timeout

                        OkHttpClient client = builder.build();
                        RequestBody file_body = RequestBody.create(MediaType.parse("text/plain"), file);

                        Log.e("File Name", file_path.substring(file_path.lastIndexOf("/") + 1));

                        RequestBody request_body = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("type", "text/plain")
                                .addFormDataPart("file-name", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                                .build();

                        Request request = new Request.Builder()
                                .url(TXT_CONTENT_URL)
                                .post(request_body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("ERROR OKHTTP", String.valueOf(e));
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Text File Uploaded Successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                                final String myResponse = response.body().string();
                                Log.e("RESPONSE OKHTTP", myResponse);
                                progressDialog.dismiss();
                                // Change Content TextView, change Button Text and Disable it
                                textContent.setText(file_path.substring(file_path.lastIndexOf("/") + 1));
                                content.setText("CONTENT UPLOADED");
                                content.setClickable(false);
                                content.setAlpha(.5f);
                            }
                        });
                    } else if (extension.equals("ocx") || extension.equals("OCX")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressDialog = new ProgressDialog(MainActivity.this);
                                progressDialog.setTitle("Uploading Content File");
                                progressDialog.setMessage("Please Wait ...");
                                progressDialog.setCancelable(false);
                                progressDialog.show();
                            }
                        });

                        OkHttpClient.Builder builder = new OkHttpClient.Builder();
                        builder.connectTimeout(10, TimeUnit.MINUTES) // connect timeout
                                .writeTimeout(10, TimeUnit.MINUTES) // write timeout
                                .readTimeout(10, TimeUnit.MINUTES); // read timeout

                        OkHttpClient client = builder.build();
                        RequestBody file_body = RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), file);

                        Log.e("File Name", file_path.substring(file_path.lastIndexOf("/") + 1));

                        RequestBody request_body = new MultipartBody.Builder()
                                .setType(MultipartBody.FORM)
                                .addFormDataPart("type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                                .addFormDataPart("file-name", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                                .build();

                        Request request = new Request.Builder()
                                .url(DOCX_CONTENT_URL)
                                .post(request_body)
                                .build();

                        client.newCall(request).enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.e("ERROR OKHTTP", String.valueOf(e));
                                progressDialog.dismiss();
                            }

                            @Override
                            public void onResponse(Call call, final Response response) throws IOException {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "Docx File Uploaded Successfully", Toast.LENGTH_LONG).show();
                                    }
                                });
                                final String myResponse = response.body().string();
                                Log.e("RESPONSE OKHTTP", myResponse);
                                progressDialog.dismiss();
                                // Change Content TextView, change Button Text and Disable it
                                textContent.setText(file_path.substring(file_path.lastIndexOf("/") + 1));
                                content.setText("CONTENT UPLOADED");
                                content.setClickable(false);
                                content.setAlpha(.5f);
                            }
                        });
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "File Format Not Supported", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            });
            thread.start();
        } else if (requestCode == QUESTION_CODE && resultCode == RESULT_OK) {
            File file = new File(data.getStringExtra(FilePickerActivity.RESULT_FILE_PATH));
            // String content_type = getMimeType(file.getPath());

            final String file_path = file.getAbsolutePath();
            Log.e("PATH", file_path);
            String extension = file_path.substring(file_path.length() - 3);
            Log.e("PATH", extension);
            if (extension.equals("txt") || extension.equals("TXT")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setTitle("Uploading Content File");
                        progressDialog.setMessage("Please Wait ...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(10, TimeUnit.MINUTES) // connect timeout
                        .writeTimeout(10, TimeUnit.MINUTES) // write timeout
                        .readTimeout(10, TimeUnit.MINUTES); // read timeout

                OkHttpClient client = builder.build();
                RequestBody file_body = RequestBody.create(MediaType.parse("text/plain"), file);

                Log.e("File Name", file_path.substring(file_path.lastIndexOf("/") + 1));

                RequestBody request_body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", "text/plain")
                        .addFormDataPart("file-name", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                        .build();

                Request request = new Request.Builder()
                        .url(TXT_QUESTION_URL)
                        .post(request_body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("ERROR OKHTTP", String.valueOf(e));
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Text File Uploaded Successfully", Toast.LENGTH_LONG).show();
                            }
                        });
                        final String myResponse = response.body().string();
                        Log.e("RESPONSE OKHTTP", myResponse);
                        progressDialog.dismiss();
                        textQuestion.setText(file_path.substring(file_path.lastIndexOf("/") + 1));
                        question.setText("QUESTIONS UPLOADED");
                        question.setClickable(false);
                        question.setAlpha(.5f);
                        // Also dismiss the chooser option dialog
                        dialogChooser.dismiss();
                    }
                });
            } else if (extension.equals("ocx") || extension.equals("OCX")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setTitle("Uploading Content File");
                        progressDialog.setMessage("Please Wait ...");
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                    }
                });

                OkHttpClient.Builder builder = new OkHttpClient.Builder();
                builder.connectTimeout(10, TimeUnit.MINUTES) // connect timeout
                        .writeTimeout(10, TimeUnit.MINUTES) // write timeout
                        .readTimeout(10, TimeUnit.MINUTES); // read timeout

                OkHttpClient client = builder.build();
                RequestBody file_body = RequestBody.create(MediaType.parse("application/vnd.openxmlformats-officedocument.wordprocessingml.document"), file);

                Log.e("File Name", file_path.substring(file_path.lastIndexOf("/") + 1));

                RequestBody request_body = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("type", "application/vnd.openxmlformats-officedocument.wordprocessingml.document")
                        .addFormDataPart("file-name", file_path.substring(file_path.lastIndexOf("/") + 1), file_body)
                        .build();

                Request request = new Request.Builder()
                        .url(DOCX_QUESTION_URL)
                        .post(request_body)
                        .build();

                client.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.e("ERROR OKHTTP", String.valueOf(e));
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onResponse(Call call, final Response response) throws IOException {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(MainActivity.this, "Docx File Uploaded Successfully", Toast.LENGTH_LONG).show();
                            }
                        });
                        final String myResponse = response.body().string();
                        Log.e("RESPONSE OKHTTP", myResponse);
                        progressDialog.dismiss();
                        textQuestion.setText(file_path.substring(file_path.lastIndexOf("/") + 1));
                        question.setText("QUESTIONS UPLOADED");
                        question.setClickable(false);
                        question.setAlpha(.5f);
                        // Also dismiss the chooser option dialog
                        dialogChooser.dismiss();
                    }
                });
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "File Format Not Supported", Toast.LENGTH_LONG).show();
                    }
                });
            }
        }
    }


    private String getMimeType(String path) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(path);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }
}
