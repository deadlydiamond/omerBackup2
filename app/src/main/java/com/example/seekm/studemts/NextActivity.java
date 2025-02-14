package com.example.seekm.studemts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.regex.Pattern;

public class NextActivity extends AppCompatActivity implements View.OnClickListener {


    CShowProgress p1;
    FloatingActionButton signup_float;

    int Image_upload_checker=0;

    SharedPreferences Profile_preferences ;

    public int Next_Activity=0;

    EditText First_name;
    EditText Last_name;
    ImageButton plus;
    ImageView add;
    EditText Email;


    EditText DateOfBirth;

    RadioGroup radioGroup_gender;

    RadioButton radioButton_male;
    RadioButton radioButton_female;

    RadioButton gender_1;

    ImageView signup_profile_image;
    String ProfileImageUrl="https://firebasestorage.googleapis.com/v0/b/my-project-1523714006398.appspot.com/o/DefaultPlaceHolder%2Fdp_placeholder.png?alt=media&token=e4d8ef33-90bc-4d17-8151-afc8824f9e77";
    String ProfileUrl="https://firebasestorage.googleapis.com/v0/b/my-project-1523714006398.appspot.com/o/DefaultPlaceHolder%2Fdp_placeholder.png?alt=media&token=e4d8ef33-90bc-4d17-8151-afc8824f9e77";
    Uri uriProfileImage;

    String first_name = "";
    String last_name = "";

    String email = "";

    String dateOfBirth = null;


    String Gender="";

    private static final int CHOOSE_IMAGE = 101;

    StorageReference profileImageRef;




    public static final Pattern VALID_EMAIL_ADDRESS_REGEX = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);


        //p1=findViewById(R.id.next_progres_bar);
        First_name = findViewById(R.id.fName);




        Last_name = findViewById(R.id.lName);

        Email = findViewById(R.id.email);


        DateOfBirth = findViewById(R.id.dob);
        DateOfBirth.setLongClickable(false);

        radioGroup_gender = findViewById(R.id.radio_group_gender);

        radioButton_male = findViewById(R.id.radio_btn_male);
        radioButton_female = findViewById(R.id.radio_btn_female);

        signup_float = findViewById(R.id.floatingActionButton3);

        signup_float.setOnClickListener(this);

        signup_profile_image=findViewById(R.id.profilePicture);

        signup_profile_image.setOnClickListener(this);


        DateOfBirth.setShowSoftInputOnFocus(false);

        DateOfBirth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                DateDialog dialog = new DateDialog((v));


                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                dialog.show(ft, "DatePicker");


            }
        });

        DateOfBirth.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                DateOfBirth.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });




//        SharedPreferences signup_data
    }


    public void onStart() {
        super.onStart();


    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.floatingActionButton3:

                TakeUserData();

                break;
            case R.id.profilePicture:

                showImageChooser();
                break;

        }
    }

    private void showImageChooser() {

        Intent  intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Image"),CHOOSE_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==CHOOSE_IMAGE && resultCode == RESULT_OK && data!=null && data.getData()!=null){

            Image_upload_checker=1;
            plus = (ImageButton)findViewById(R.id.addButton);
            plus.setVisibility(View.GONE);
            uriProfileImage  = data.getData();
            signup_profile_image.setImageDrawable(null);
            signup_profile_image.setBackgroundResource(0);
            signup_profile_image.setImageURI(uriProfileImage);
        }
    }

    private void TakeUserData() {


        first_name = First_name.getText().toString();
        last_name = Last_name.getText().toString();
        email = Email.getText().toString();
        dateOfBirth = DateOfBirth.getText().toString();

        int selectId =radioGroup_gender.getCheckedRadioButtonId();

        gender_1=findViewById(selectId);

        Gender = gender_1.getText().toString();


        Next_Activity+= validateName(first_name, First_name,"First Name");
        Next_Activity+= validateName(last_name, Last_name,"Last Name");

        Next_Activity+= validateEmail(email,Email);

        Next_Activity+= validateDateOfbirth(dateOfBirth,DateOfBirth);

        if(Next_Activity==4){


            p1 = CShowProgress.getInstance();
            p1.showProgress(NextActivity.this);


            Profile_preferences = getApplicationContext().getSharedPreferences("Profile_Preferecens",0);


            if(Image_upload_checker==1){

                uploadImageToFirebase();


            }
            else
            {

                dothisNow();
            }






        }

        else {

            Next_Activity=0;
        }


    }

    private void uploadImageToFirebase() {

        profileImageRef=FirebaseStorage.getInstance().getReference().child("ProfilePicture/"+System.currentTimeMillis()+".jpg");
        if(uriProfileImage!=null){
            profileImageRef.putFile(uriProfileImage).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    profileImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            ProfileImageUrl=uri.toString();
                            ProfileUrl=ProfileImageUrl;
                            dothisNow();
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }

    private void dothisNow() {

//        new Handler().postDelayed(new Runnable() {
//            @Override
//            public void run() {


        SharedPreferences.Editor editor = Profile_preferences.edit();
        editor.putString("First_Name",first_name);
        editor.putString("Last_Name",last_name);
        editor.putString("Email",email);
        editor.putString("Date_Of_Birth",dateOfBirth);
        editor.putString("Gender",Gender);
        editor.putString("Profile_Image_Url",ProfileUrl);
        editor.apply();
        p1.hideProgress();


        GoToProfileBuilder();
//            }
//        }, 10000);
    }

    private void GoToProfileBuilder() {

        startActivity(new Intent(NextActivity.this,ProfileBuilder.class));
    }

    private int validateDateOfbirth(String dateOfBirth, EditText dateOfBirth1) {



        if(dateOfBirth.length()==0){

            dateOfBirth1.setError("Date Of Birth is required");
            dateOfBirth1.requestFocus();

            return 0;
        }

        return 1;

    }


    private int validateEmail(String email, EditText email1) {

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){

            email1.setError("Enter Valid Email");
            email1.requestFocus();

            return 0;

        }

        if(email.length() == 0){

            email1.setError("Email can not be empty");

            return 0;

        }

        return 1;

    }

    private int  validateName(String AnyName, EditText AnyEditText , String Datatypes) {

        if (!AnyName.matches("^[A-Za-z\\s]{1,}[\\.]{0,1}[A-Za-z\\s]{0,}$") || AnyName.length()< 2) {

            AnyEditText.setError("Please Enter Valid Name");
            AnyEditText.requestFocus();

            return 0;



        }

        if (AnyName.length() == 0) {

            AnyEditText.setError(Datatypes + " is Required");
            AnyEditText.requestFocus();

            return 0;

        }

        return 1;

    }



}