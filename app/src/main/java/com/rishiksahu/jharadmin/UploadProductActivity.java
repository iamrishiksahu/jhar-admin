package com.rishiksahu.jharadmin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadProductActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener{

    public static int PICK_IMAGE = 1002;
    private Spinner spinner;
    private Button uploadImg, uploadBtn;
    private EditText prodIdEt, cuttedPriceEt, priceEt, productTitleEt, productSubtitleEt, productDescriptionEt;

    private String[] material = { "-Select Product Type-", "Course", "Test Series", "Notes", "Downloadable"};
    private int[] productTypeInt = {1,2,3,4};
    private int productType;
    private String tempImgUriString, productTitle, prodSubtitle,productId, cuttedPrice, productPrice, averageRating, prodImageUrl, productDescription;

    private FirebaseFirestore firebaseFirestore;
    private Uri filePath;
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_product);


        spinner = findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(this);

        prodIdEt = findViewById(R.id.productId);
        productTitleEt = findViewById(R.id.prodTitle);
        productSubtitleEt = findViewById(R.id.prodSubtitle);
        cuttedPriceEt = findViewById(R.id.cuttedPrice);
        priceEt = findViewById(R.id.salePrice);
        productDescriptionEt = findViewById(R.id.prodDesc);

        uploadImg = findViewById(R.id.uploadImage);
        uploadBtn = findViewById(R.id.uploadProduct);
        uploadBtn.setEnabled(false);

        ArrayAdapter ad = new ArrayAdapter(this, android.R.layout.simple_spinner_item, material);

        ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(ad);
        firebaseFirestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

//        uploadImg.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });

        uploadImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureTexts();

                uploadBtn.setEnabled(true);

                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, PICK_IMAGE);
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String, Object> uploadMap = new HashMap<>();
                uploadMap.put("1_star", 0);
                uploadMap.put("2_star", 0);
                uploadMap.put("3_star", 0);
                uploadMap.put("4_star", 15);
                uploadMap.put("5_star", 173);
                uploadMap.put("no_of_product_images", 1);
                uploadMap.put("average_rating", "4.9");
                uploadMap.put("prduct_image_1",prodImageUrl);
                uploadMap.put("cutted_price", cuttedPrice);
                uploadMap.put("product_description", productDescription);
                uploadMap.put("product_price", productPrice);
                uploadMap.put("product_subtitle", prodSubtitle);
                uploadMap.put("product_title", productTitle);
                uploadMap.put("product_type", productType);
                uploadMap.put("total_ratings", 188);
                uploadMap.put("use_tab_layout", false);

                firebaseFirestore.collection("PRODUCTS").document(productId).set(uploadMap)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(UploadProductActivity.this, "Upload Success!", Toast.LENGTH_LONG).show();

                                }else {
                                    Toast.makeText(UploadProductActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                                }
                            }
                        });


            }
        });


    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            productType = productTypeInt[position - 1];
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK){
            if (data != null && data.getData() != null){
                filePath = data.getData();
                uploadImage(filePath);
            }
        }
    }

    private void uploadImage(Uri filePath) {

        ProgressDialog progressDialog = new ProgressDialog(this);progressDialog.setTitle("Uploading...");
        progressDialog.show();

        // Defining the child of storageReference
        StorageReference ref = storageReference.child("testing/" + productId);

        ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressDialog.dismiss();
                        prodImageUrl = uri.toString();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(UploadProductActivity.this, "" + e.getMessage(), Toast.LENGTH_LONG).show();

                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();

            }
        }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                progressDialog.setMessage("Uploaded " + (int)progress + "%");
            }
        });

    }

    private void captureTexts(){
        try {

            productId = prodIdEt.getText().toString().trim();
            productTitle = productTitleEt.getText().toString().trim();
            prodSubtitle = productSubtitleEt.getText().toString().trim();
            cuttedPrice = cuttedPriceEt.getText().toString().trim();
            productPrice = priceEt.getText().toString().trim();
            productDescription = productDescriptionEt.getText().toString().replaceAll("\\n", "<br />");

        }catch (Exception e){
            Toast.makeText(UploadProductActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}