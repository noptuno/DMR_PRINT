package com.gpp.dmr_print.Usuario;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.barteksc.pdfviewer.PDFView;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.gpp.dmr_print.InicioFash;
import com.gpp.dmr_print.MainActivity;
import com.gpp.dmr_print.R;

public class InicioSesion extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    Button iniciar_sesion,btngoogle;
    TextView olvidecontrasena, mensaje;
    EditText txtemail;
    EditText txtpassword;
    SharedPreferences pref;
    private ProgressDialog progressDialog;
   // private SignInButton btngoogle;
    private static final int RC_SIGN_IN = 1;
    private static final String TAG = "INICIOSESIONACRIVITY";
    private ProgressDialog dialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_sesion_new);
        btngoogle = findViewById(R.id.btngoogle);

        iniciar_sesion = findViewById(R.id.iniciar_sesion);
        txtemail = findViewById(R.id.et_correo);
        txtpassword = findViewById(R.id.et_contrasena);



        progressDialog = new ProgressDialog(this);

        dialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);




        iniciar_sesion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = txtemail.getText().toString();
                String password = txtpassword.getText().toString();

                if (email.length() > 0 && password.length() > 0) {
                    progressDialog.setMessage("Iniciando Sesion en linea...");
                    progressDialog.show();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(InicioSesion.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        FirebaseUser user = mAuth.getCurrentUser();

                                        updateUI(user);



                                    } else {
                                        Toast.makeText(InicioSesion.this, "invalido", Toast.LENGTH_LONG).show();
                                    }
                                    progressDialog.dismiss();
                                }

                            });
                } else {
                    Toast.makeText(InicioSesion.this, "Datos Inválidos", Toast.LENGTH_LONG).show();
                }

            }
        });


        btngoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });
    }


    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        updateUI(currentUser);
    }
    // [END on_start_check_user]

    // [START onactivityresult]

    // [START signin]
    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    // [END signin]

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {


            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);

            try {


                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);

            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                // [START_EXCLUDE]
                updateUI(null);
                // [END_EXCLUDE]
            }
        }
    }
    // [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        showProgressDialog();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_google]



    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(null);
                    }
                });
    }

    private void updateUI(FirebaseUser user) {

        hideProgressDialog();
        if (user != null) {

            Intent intent = new Intent(InicioSesion.this, MainActivity.class);
            startActivity(intent);
            SharedPreferences pref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("EMAIL", user.getEmail());
            editor.putString("ESTADO_LOGIN", "SI");
            editor.apply();
            finish();
        }else{

            SharedPreferences pref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("EMAIL", "");
            editor.putString("ESTADO_LOGIN", "NO");
            editor.apply();
        }
    }

    private void showProgressDialog() {


        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void hideProgressDialog() {

        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.hide();
    }



/*
    private void updateUI(String user) {

        if (!user.equals("")) {
            String email = user;
            Intent intent = new Intent(InicioSesion.this, MainActivity.class);
            startActivity(intent);
            SharedPreferences pref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("EMAIL", email);
            editor.putString("ESTADO_LOGIN", "SI");
            editor.apply();
            finish();
        } else {
            Toast.makeText(InicioSesion.this, "Usuario Incorrecto" + txtemail.getText(), Toast.LENGTH_LONG).show();
        }
    }
*/
}
