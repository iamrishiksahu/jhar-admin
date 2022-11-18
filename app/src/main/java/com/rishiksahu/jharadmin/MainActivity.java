package com.rishiksahu.jharadmin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    private Button uploadQuestions, uploadProd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uploadQuestions = findViewById(R.id.questBtn);
        uploadProd = findViewById(R.id.upProdBtn);

        FirebaseAuth.getInstance().signInWithEmailAndPassword("rishikkumar23@gmail.com", "rishik").addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(MainActivity.this, "" + "Login Success!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        uploadQuestions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent questIntent = new Intent(MainActivity.this, QuestionUploadActivity.class);
                startActivity(questIntent);
            }
        });

        uploadProd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent prodIntent = new Intent(MainActivity.this, UploadProductActivity.class);
                startActivity(prodIntent);
            }
        });
    }
}