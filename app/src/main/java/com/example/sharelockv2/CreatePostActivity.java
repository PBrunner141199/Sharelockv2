package com.example.sharelockv2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.example.sharelockv2.Helperclasses.Model;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public class CreatePostActivity extends AppCompatActivity {

    ImageView postimage;
    Button createbtn;
    RadioButton radioButton;
    RadioGroup radioGroup;
    FloatingActionButton camerbtn;
    EditText title, desc;
    private Bitmap image;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_post);

        camerbtn= findViewById(R.id.camerabtn);
        postimage= findViewById(R.id.postImage);
        createbtn=findViewById(R.id.createbtn);
        radioGroup = findViewById(R.id.radioGroup);



        mStorageRef = FirebaseStorage.getInstance().getReference("PostPictures");
        mDatabase = FirebaseDatabase.getInstance().getReference("Posts");
        mAuth= FirebaseAuth.getInstance();



        camerbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera= new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(camera, 0);
            }
        });
        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upload();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode ==0 && resultCode== RESULT_OK){
            image = (Bitmap) data.getExtras().get("data");
            postimage.setImageBitmap(image);
        }
    }
    private void upload() {
        final ProgressBar p = findViewById(R.id.progressBar);

        p.setVisibility(View.VISIBLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);

        final String random = UUID.randomUUID().toString();
        StorageReference imageRef = mStorageRef.child("image" + random);

        byte[] b = stream.toByteArray();
        imageRef.putBytes(b)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        p.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                final EditText title = findViewById(R.id.postTitle);
                                String postTitle = title.getText().toString();
                                final EditText desc = findViewById(R.id.postDesc);
                                String postDesc = desc.getText().toString();
                                FirebaseUser user = mAuth.getCurrentUser();
                                String uID = user.getUid();
                                String name = user.getDisplayName();
                                Uri downloadUri = uri;

                               //String angonach= checkRadio();


                                Model model = new Model(postDesc,postTitle,uID,name, uri.toString()/*,angonach*/);
                                String modelid = mDatabase.push().getKey();
                                mDatabase.child(name).child(modelid).setValue(model);
                            }
                        });

                        Toast.makeText(CreatePostActivity.this, "Post wurde erfolgreich erstellt.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(CreatePostActivity.this,MarketplaceActivity_.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        p.setVisibility(View.GONE);
                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                        Toast.makeText(CreatePostActivity.this, "Leider fehlgeschlagen. Bitte versuchen sie es erneut.", Toast.LENGTH_SHORT).show();
                    }
                });


        }
       /* public String checkRadio() {
            int radioId = radioGroup.getCheckedRadioButtonId();
            String aoderN =
            if (radioId == 0) {
                aoderN = "Nachfrage";

            } else if (radioId == 1) {
                aoderN = "Angebot";

            } else {
                Toast.makeText(CreatePostActivity.this, "Sie müssen Angebot oder Nachfrage auswählen.", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(CreatePostActivity.this, CreatePostActivity.class));
            }
            return aoderN;*/
        //}

    }

