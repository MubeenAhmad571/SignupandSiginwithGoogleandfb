package com.example.signupandsiginwithgoogleandfb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {
    private EditText emailET,pwET;
    private Button signUpBtn,signInBtn;
    FirebaseAuth mAuth;
    TextView id1,name1,email1;
    ImageView image;

    private ProgressBar objectProgressBar;
    private FirebaseAuth objectFirebaseAuth;
    static final int GOOGLE_SIGN=123;
    Button btn_login,signOutGoogleBtm;
    GoogleSignInClient mGoogleSignInClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        objectFirebaseAuth=FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();


        setContentView(R.layout.activity_main);
        id1=findViewById(R.id.idTV);
        name1=findViewById(R.id.nameTV);
        email1=findViewById(R.id.useremailTV);
        image = findViewById(R.id.userImageIV);
        btn_login=findViewById(R.id.signGoogle);
        signOutGoogleBtm=findViewById(R.id.sigOutGoogle);
        signOutGoogleBtm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseAuth.getInstance().signOut();
                mGoogleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "SingOUT", Toast.LENGTH_SHORT).show();
                        email1.setText("Default User");
                        objectProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                objectProgressBar.setVisibility(View.VISIBLE);
                FirebaseUser user=objectFirebaseAuth.getCurrentUser();

                Intent signIntent=mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signIntent,GOOGLE_SIGN);
            }
        });


        connectXMLObjects();
        GoogleSignInOptions googleSignInOptions=new GoogleSignInOptions
                .Builder().requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient= GoogleSignIn.getClient(this,googleSignInOptions);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == GOOGLE_SIGN){
            Task<GoogleSignInAccount> task = GoogleSignIn.
                    getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account=task.getResult(ApiException.class);
                if(account != null){
                    firebaseAuthWithGoogle(account);
                }
            }catch (ApiException e){
                e.printStackTrace();
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount account) {
        Log.d("Tag","fireBaseAuthWithGoogle  :"+account.getId());
        AuthCredential credential= GoogleAuthProvider.getCredential(account.getIdToken(),null);
        objectFirebaseAuth.signInWithCredential(credential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Toast.makeText(MainActivity.this, "Successfully Logged In", Toast.LENGTH_LONG).show();
                if (task.isSuccessful()) {
                    objectProgressBar.setVisibility(View.INVISIBLE);

                    Log.d("TAG", "signInWithCredential:success");

                    FirebaseUser user = objectFirebaseAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    objectProgressBar.setVisibility(View.INVISIBLE);

                    Log.w("TAG", "signInWithCredential:failure", task.getException());

                    Toast.makeText(MainActivity.this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show();
                    updateUI(null);
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "FAILED TO LOGIN", Toast.LENGTH_SHORT).show();
            }
        });


    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();
            String photo = String.valueOf(user.getPhotoUrl());

            // Picasso.with(MainActivity.this).load(photo).into(image);
            email1.setText(email);
            name1.setText(name);


        } else {
            //  Picasso.with(MainActivity.this).load(R.drawable.firebase_logo).into(image);
            id1.setText("Firebase Login \n");


        }
    }

    private  void sigIn(){


        objectFirebaseAuth.signInWithEmailAndPassword(emailET.getText().toString(), pwET.getText().toString())
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Toast.makeText(MainActivity.this, "You are Logged In", Toast.LENGTH_LONG).show();


                        } else {
                            Toast.makeText(MainActivity.this, "You are Failed To login", Toast.LENGTH_LONG).show();

                        }

                    }
                });}

    private void checkIfUserExists()
    {
        try
        {
            if(!emailET.getText().toString().isEmpty())
            {
                if(objectFirebaseAuth!=null)
                {
                    objectProgressBar.setVisibility(View.VISIBLE);
                    signUpBtn.setEnabled(false);

                    objectFirebaseAuth.fetchSignInMethodsForEmail(emailET.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
                                @Override
                                public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                                    boolean check=task.getResult().getSignInMethods().isEmpty();
                                    if(!check)
                                    {
                                        signUpBtn.setEnabled(true);
                                        objectProgressBar.setVisibility(View.INVISIBLE);

                                        Toast.makeText(MainActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
                                    }
                                    else if(check)
                                    {

                                        signupUser(); //Step 6
                                    }
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    signUpBtn.setEnabled(true);
                                    objectProgressBar.setVisibility(View.INVISIBLE);

                                    Toast.makeText(MainActivity.this, "Fails to check if user exists:"
                                            +e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
            else
            {
                emailET.requestFocus();
                Toast.makeText(this, "Please enter the email", Toast.LENGTH_SHORT).show();
            }
        }
        catch (Exception e)
        {
            signUpBtn.setEnabled(true);
            objectProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "checkIfUserExists:"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void connectXMLObjects(){

        try {
            emailET=findViewById(R.id.emailET);
            pwET=findViewById(R.id.passwordET);

            signUpBtn =findViewById(R.id.signUpBtn);
            signInBtn=findViewById(R.id.signInBtn);
            objectProgressBar=findViewById(R.id.signUpProgressBar);
            signInBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    sigIn();
                }
            });
            signUpBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // signupUser();
                    checkIfUserExists();

                }
            });
        }catch (Exception e )
        {

            Toast.makeText(this, "connectXNLObjects"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }


    }

    private  void signupUser(){

        try {

            if (!emailET.getText().toString().isEmpty() &&
                    !pwET.getText().toString().isEmpty()
            ){
                if(objectFirebaseAuth!=null){
                    objectProgressBar.setVisibility(View.VISIBLE);
                    signUpBtn.setEnabled(false);

                    objectFirebaseAuth.createUserWithEmailAndPassword(emailET.getText().toString(),pwET.getText().toString()).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {

                            objectProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "SuccessFullyCreated", Toast.LENGTH_SHORT).show();
                            if(authResult.getUser()!=null){
                                objectFirebaseAuth.signOut();
                                emailET.setText("");
                                pwET.setText("");

                                signUpBtn.setEnabled(true);
                            }

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                            objectProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(MainActivity.this, "Failed to Add"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            signUpBtn.setEnabled(true);
                            emailET.requestFocus();
                            objectProgressBar.setVisibility(View.INVISIBLE);
                        }
                    });

                }
            }
            else if(emailET.getText().toString().isEmpty()){


                objectProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "Enter The Email", Toast.LENGTH_SHORT).show();
                emailET.requestFocus();
            }
            else if(pwET.getText().toString().isEmpty()){
                pwET.requestFocus();
                Toast.makeText(this, "Enter The Password", Toast.LENGTH_SHORT).show();
            }



        }catch (Exception e)
        {

            objectProgressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "signUpUser"+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
