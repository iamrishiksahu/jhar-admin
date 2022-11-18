package com.rishiksahu.jharadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;

public class QuestionUploadActivity extends AppCompatActivity {

    private RadioGroup radioGroup;
    private Button captureDataBtn, chooseFileButton;
    private EditText prodIdEt, setNoEt, maxMarksEt, maxTimeEt;
    private String productId, language;
    private int setNo, maxMarks, maxTime;
    public static int PICK_JSON = 1001;
    private QuestionsModel questionsModel;
    private FirebaseDatabase database;
    private DatabaseReference reference;

    private Dialog loadingDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_upload);

        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setTitle("Upload Questions");

        chooseFileButton = findViewById(R.id.chooseBtn);
        captureDataBtn = (Button)findViewById(R.id.captureBtn);
        radioGroup = (RadioGroup)findViewById(R.id.groupradio);
        prodIdEt = findViewById(R.id.prodIdEt);
        maxMarksEt = findViewById(R.id.maxMarksEt);
        maxTimeEt = findViewById(R.id.maxTimeEt);
        setNoEt = findViewById(R.id.setNoEt);

        chooseFileButton.setEnabled(false);

        ///loading Dialog
        loadingDialog = new Dialog(QuestionUploadActivity.this);
        loadingDialog.setContentView(R.layout.loading_progress_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.slider_background));
        loadingDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        /////end loading dialog

        questionsModel = new QuestionsModel();

        database = FirebaseDatabase.getInstance();
        reference = database.getReference();

        // Uncheck or reset the radio buttons initially
        radioGroup.clearCheck();

        // Add the Listener to the RadioGroup
        radioGroup.setOnCheckedChangeListener(
                new RadioGroup
                        .OnCheckedChangeListener() {
                    @Override

                    // The flow will come here when
                    // any of the radio buttons in the radioGroup
                    // has been clicked

                    // Check which radio button has been clicked
                    public void onCheckedChanged(RadioGroup group,
                                                 int checkedId)
                    {

                        // Get the selected Radio Button
                        RadioButton
                                radioButton
                                = (RadioButton)group
                                .findViewById(checkedId);
                    }
                });

        // Add the Listener to the Submit Button
        captureDataBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v)
            {

                // When submit button is clicked,
                // Ge the Radio Button which is set
                // If no Radio Button is set, -1 will be returned
                chooseFileButton.setEnabled(true);
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == -1) {
                    Toast.makeText(QuestionUploadActivity.this,
                            "Please Select The Language",
                            Toast.LENGTH_SHORT)
                            .show();
                }
                else {

                    RadioButton radioButton
                            = (RadioButton)radioGroup
                            .findViewById(selectedId);

                    // Now display the value of selected item
                    // by the Toast message
                    Toast.makeText(QuestionUploadActivity.this,
                            radioButton.getText(),
                            Toast.LENGTH_SHORT)
                            .show();

                    try {
                        language = radioButton.getText().toString().toLowerCase();
                        productId = prodIdEt.getText().toString();
                        maxMarks = Integer.parseInt(maxMarksEt.getText().toString());
                        setNo = Integer.parseInt(setNoEt.getText().toString());
                        maxTime = Integer.parseInt(maxTimeEt.getText().toString());
                    }catch (Exception e){
                        Toast.makeText(QuestionUploadActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        chooseFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("*/*");
                startActivityForResult(intent, PICK_JSON);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_JSON && resultCode == Activity.RESULT_OK){
            Uri uri = null;
            if (data != null){
                uri = data.getData();

                getJson(uri);
            }else {
                Toast.makeText(this, "URI Data returned null", Toast.LENGTH_SHORT).show();
            }


        }else {
            Toast.makeText(this, "Result Code is Not OK! (File not selected!)", Toast.LENGTH_SHORT).show();
        }
    }

    private void getJson(Uri contentUri){

        loadingDialog.show();

        String json =null;
        try {
            InputStream is = getContentResolver().openInputStream(contentUri);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();


            json = new String(buffer, "UTF-8");
            JSONArray jsonArray = new JSONArray(json);

            for (int i=0; i<jsonArray.length() ; i++){
                JSONObject obj = jsonArray.getJSONObject(i);
                questionsModel.setQ(obj.getString("q"));
                questionsModel.setA(obj.getString("a"));
                questionsModel.setB(obj.getString("b"));
                questionsModel.setC(obj.getString("c"));
                questionsModel.setD(obj.getString("d"));
                questionsModel.setCorrect(obj.getString("correct"));
                questionsModel.setPositiveGrade(obj.getLong("positiveGrade"));
                questionsModel.setNegativeGrade(obj.getDouble("negativeGrade"));

                reference.child("QuestionsData").child(productId).child("set" + setNo)
                        .child(language).push().setValue(questionsModel);


            }
            reference.child("QuestionsData").child(productId).child("set" + setNo).child("maxMarks").child("marks").setValue(maxMarks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            reference.child("QuestionsData").child(productId).child("set" + setNo).child("maxTime").child("time").setValue(maxTime)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(QuestionUploadActivity.this, "Upload Success!", Toast.LENGTH_SHORT).show();
                                                clearTextViews();
                                                loadingDialog.dismiss();
                                            } else {
                                                loadingDialog.dismiss();

                                                Toast.makeText(QuestionUploadActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }else {
                            loadingDialog.dismiss();
                            Toast.makeText(QuestionUploadActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    }
                });

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "File Not Found! " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "error1 " + e.getMessage(), Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "error2 (JSONArray Problem) : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }
    }

    private void clearTextViews() {
        prodIdEt.setText("");
        setNoEt.setText("");
        maxTimeEt.setText("");
        maxMarksEt.setText("");
        radioGroup.clearCheck();
    }
}