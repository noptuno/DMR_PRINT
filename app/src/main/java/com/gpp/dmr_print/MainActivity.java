package com.gpp.dmr_print;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.gpp.dmr_print.ConfiguracionS.Configuracion_Impresora;
import com.gpp.dmr_print.ConfiguracionS.ConnectionSettingsActivity;
import com.gpp.dmr_print.ConfiguracionS.DMRPrintSettings;
import com.gpp.dmr_print.Folder.Fragmen_folder;
import com.gpp.dmr_print.GRUB.MenuGRUB;
import com.gpp.dmr_print.Usuario.Fragment_usuario;
import com.gpp.dmr_print.Usuario.InicioSesion;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    public static Context contextOfApplication;
    public static final int STORAGE_PERMISSION_REQUEST_CODE = 1;
    final FragmentManager fragmentmanager = this.getSupportFragmentManager();
    final Fragment fragment_usuario = new Fragment_usuario();
    final Fragment fragment_folder = new Fragmen_folder();
    static final String nomb_fragment_folder = "Buscardor de Archivos";
    static final String nomb_fragment_usuario = "Datos del usuario";
    private boolean backPressedToExitOnce = false;
    private Toast toast = null;
    private Fragment active = fragment_folder;
    private Toolbar toolbar;
    private BottomNavigationView navigation;
    public String fileName;
    private File tempFile;
    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
    String ApplicationConfigFilename = "applicationconfigg.dat";

    private Boolean permisosaceptados = false;
    private static final String RECIBIR = "ENVIANDO";


    private SharedPreferences pref;
    private String estado_login;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private String estado = "NO";

    static {
        try {
            System.loadLibrary("bxl_common");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        contextOfApplication = getApplicationContext();

        pref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
        estado = pref.getString("ESTADO_LOGIN", "NO");

//.requestIdToken(getString(R.string.default_web_client_id))
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("dmrprint-3b30a")
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();


        final int ANDROID_NOUGAT = 24;

        if(Build.VERSION.SDK_INT >= ANDROID_NOUGAT)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        //todo permiso de escritura
        pedir_permiso_escritura();


        //todo inflo todos los fragments
        navigation = (BottomNavigationView) findViewById(R.id.navigationgrav);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        fragmentmanager.beginTransaction().add(R.id.container_fragment, fragment_usuario, "fragment_usuario").hide(fragment_usuario).commit();
        fragmentmanager.beginTransaction().add(R.id.container_fragment, fragment_folder, "fragment_folder").commit();
        fragmentmanager.addOnBackStackChangedListener(this);


        handleIntent(getIntent());

    }


    public static Context getContextOfApplication() {
        return contextOfApplication;
    }


    private void pedir_permiso_escritura() {

        int readExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int writeExternalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (writeExternalPermission != PackageManager.PERMISSION_GRANTED || readExternalPermission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_REQUEST_CODE);
        } else {
            permisosaceptados = true;
        }

        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.BLUETOOTH_CONNECT}, 2);

                return;
            }
        }



    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case STORAGE_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permisosaceptados = true;
                    recargarFolderaceptandopermisos();

                    if (tempFile != null && tempFile.length() > 0) {
                        Abrir_fragment_pdf(tempFile);
                    }

                } else {
                    permisosaceptados = false;
                }
                return;
            }
        }
    }

    private void recargarFolderaceptandopermisos() {

        if (fragment_folder instanceof Fragmen_folder) {
            Fragmen_folder headlinesFragment = (Fragmen_folder) fragment_folder;
            headlinesFragment.cargarlistview();
        }

    }


    public Boolean get_permisos() {
        return permisosaceptados;

    }


    @Override
    protected void onPostResume() {
        super.onPostResume();
        Cargar_Datos_Configuracion();


    }

/*
//CO NESTO PUEDO DECIRLE AL FRAGMENT QUE CARGUE MI CONTEXTO Y NO TENER QUE HACERLO CON EL ON ATACH EN EL CONTEXTO DIRECTAMENTE
//CON ESTO PUEDO APRENDER A COMUNICAR DESDE EL ACTIVITY AL FRAGMENT

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof Fragment_pdf) {
            Fragment_pdf headlinesFragment = (Fragment_pdf) fragment;
            headlinesFragment.llamar_interfaz_fragmentpdf(this);
        }
    }
*/


    private void Abrir_fragment_pdf(File TEMPfILE) {

        if (!permisosaceptados) {
            showToast("Debe aceptar los permisos para continuar");
            pedir_permiso_escritura();

        } else {

            Intent I = new Intent(MainActivity.this, Pdf_view.class);
            Bundle b = new Bundle();
            I.setAction("ENVIANDO_INTENT");
            b.putSerializable("MY_FILE", TEMPfILE);
            I.putExtras(b);
            startActivity(I);

        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {

        if (estado.equals("SI")) {

            String action = intent.getAction();
            String type = intent.getType();
            if (intent.ACTION_VIEW.equals(action) && type.endsWith("pdf")) {
                Uri file_uri = intent.getData();
                if (file_uri != null) {
                    try {
                        InputStream inputStream = null;
                        try {
                            inputStream = MainActivity.this.getContentResolver().openInputStream(file_uri);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        fileName = getFileName(MainActivity.this, file_uri);
                        String[] splitName = splitFileName(fileName);
                        tempFile = null;
                        tempFile = File.createTempFile(splitName[0], splitName[1]);
                        tempFile = rename(tempFile, fileName);
                        tempFile.deleteOnExit();
                        FileOutputStream out = null;
                        out = new FileOutputStream(tempFile);

                        if (inputStream != null) {
                            copy(inputStream, out);
                            inputStream.close();
                        }
                        if (out != null) {
                            out.close();
                        }
                        Abrir_fragment_pdf(tempFile);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                navigation.setSelectedItemId(R.id.navigation_folder);
            }

        }else{

            Intent ints = new Intent(MainActivity.this, InicioSesion.class);
            startActivity(ints);
            MainActivity.this.finish();
        }



    }


    //todo METODOS PARA PDF URI
    private static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf(".");
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int a =  cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    result = cursor.getString(a);

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static File rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        if (!newFile.equals(file)) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old " + newName + " file");
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName);
            }
        }
        return newFile;
    }

    private static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }


    @Override
    public boolean onSupportNavigateUp() {
        //TODO CONTROLAR EL BACKPRESED DEL TOOLBAR
        //Destruye el backstar (el fragment cargado)
        fragmentmanager.popBackStack();
        return true;
    }


    public void shouldDisplayHomeUp() {

        boolean canGoBack = fragmentmanager.getBackStackEntryCount() > 0;


        //deberia validar que el fragment que estoy borrando esta en el contenedor 3
        //porque este metodo identifica todos fragments del contenedor para saber cuando se elimina un fragment o se apreta el boton de atras
        //este funciona porque solo el contenedor 3 tiene mas fragment con backstack
/*
        if (canGoBack) {
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
                toolbar.setTitle(nomb_fragment_pdf);
        } else {
            if (getSupportActionBar() != null)
                getSupportActionBar().setDisplayHomeAsUpEnabled(canGoBack);
                 toolbar.setTitle(nomb_fragment_folder);
        }

*/
    }


    @Override
    public void onBackPressed() {

        if (fragment_folder.isHidden()) {
            // shouldDisplayHomeUp();
            navigation.setSelectedItemId(R.id.navigation_folder);

        } else {

            if (backPressedToExitOnce) {
                //TODO cierro la app
                super.onBackPressed();

                //TODO  si el contenedor no está en el fragment 2 lo mando al fragment 2 principal
            }

            //TODO  si el contenedor se encuentra en el fragment principal hay que preisonar 2 veces para cerrar
            this.backPressedToExitOnce = true;
            showToast("Presione nuevamente para cerrar");
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    backPressedToExitOnce = false;
                }
            }, 2000);
        }


        //TODO  si el contenedor esta en el fragment 4 ejecuto el backstack
/*
        if (contenedor_folder.isVisible() && toolbar.getTitle().equals(nomb_fragment_pdf)) {
            shouldDisplayHomeUp();
            super.onBackPressed();
        } else {

            if (backPressedToExitOnce) {
                //TODO cierro la app
                finish();

                //TODO  si el contenedor no está en el fragment 2 lo mando al fragment 2 principal
            } else if (navigation.getSelectedItemId() != R.id.navigation_folder) {

                navigation.setSelectedItemId(R.id.navigation_folder);
                //active = contenedor_folder;
                // toolbar.setTitle(nomb_fragment_folder);

            } else {

                //TODO  si el contenedor se encuentra en el fragment principal hay que preisonar 2 veces para cerrar
                this.backPressedToExitOnce = true;
                showToast("Presione nuevamente para cerrar");
                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        backPressedToExitOnce = false;
                    }
                }, 2000);
            }
        }
*/

    }

    @Override
    protected void onPause() {
        killToast();
        super.onPause();
    }

    private void killToast() {
        if (this.toast != null) {
            this.toast.cancel();
        }
    }

    private void showToast(String message) {
        if (this.toast == null) {
            // Create toast if found null, it would he the case of first call only
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else if (this.toast.getView() == null) {
            // Toast not showing, so create new one
            this.toast = Toast.makeText(this, message, Toast.LENGTH_SHORT);

        } else {
            // Updating toast message is showing
            this.toast.setText(message);
        }

        // Showing toast finally
        this.toast.show();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {

                case R.id.navigation_sesion:
                    fragmentmanager.beginTransaction().hide(active).show(fragment_usuario).commit();
                    active = fragment_usuario;
                    toolbar.setTitle(nomb_fragment_usuario);
                    // getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                    return true;


                case R.id.navigation_folder:
                    fragmentmanager.beginTransaction().hide(active).show(fragment_folder).commit();
                    active = fragment_folder;
                    toolbar.setTitle(nomb_fragment_folder);
                    // shouldDisplayHomeUp();
                    return true;

            }
            return false;
        }
    };


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void revokeAccess() {
        // Firebase sign out
        mAuth.signOut();

        // Google revoke access
        mGoogleSignInClient.revokeAccess().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        Intent ints = new Intent(MainActivity.this, InicioSesion.class);
                        startActivity(ints);
                        SharedPreferences pref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = pref.edit();
                        editor.putString("ESTADO_LOGIN", "NO");
                        editor.apply();
                        MainActivity.this.finish();

                    }
                });
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();


        if (id == R.id.action_settings) {

            Intent v = new Intent(MainActivity.this, Configuracion_Impresora.class);
            startActivity(v);
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            return true;
        } else if (id == R.id.action_sesion) {

            AlertDialog.Builder build = new AlertDialog.Builder(MainActivity.this);
            build.setMessage("Desea Cerrar Sesión").setPositiveButton("Si", new DialogInterface.OnClickListener() {


                @Override
                public void onClick(DialogInterface dialog, int which) {

                    revokeAccess();

                }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            AlertDialog alertDialog = build.create();
            alertDialog.show();
            return true;

        }else if (id == R.id.about) {

            if (mAuth.getCurrentUser().getEmail().equals("desarrollo@dmr.com.uy")){
                Intent mainIntent = new Intent(MainActivity.this, MenuGRUB.class);
                MainActivity.this.startActivity(mainIntent);
            }else{
                DisplayAboutDialog();
            }

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    void DisplayAboutDialog() {
        final AppCompatDialog about = new AppCompatDialog(MainActivity.this);
        about.setContentView(R.layout.doabout);
        about.setCancelable(true);
        about.setTitle(R.string.about);

        // get version of the application.
        PackageInfo pinfo;
        try
        {
            pinfo = getPackageManager().getPackageInfo(getPackageName(), 0);

            if (pinfo != null) {


                TextView descTextView = (TextView) about.findViewById(R.id.AboutDescription);


                String descString = " " + getString(R.string.app_name) + "\n"
                        + " Version Code:"
                        + String.valueOf(pinfo.versionCode) + "\n"
                        + " Version Name:" + pinfo.versionName+"\n"
                        + " Copyright: 2018" + "\n"
                        + " Lipiner S.A (DMR mil rollos)" + "\r\n"
                        + " Dirección: Convenio 828"  + "\r\n"
                        + " Teléfono: (+598) 2209 19 21"  + "\r\n"
                        + " Contacto: desarrollo@dmr.com.uy";

                if(descTextView != null)
                    descTextView.setText(descString);

                // set up the image view
                ImageView AboutImgView = (ImageView) about
                        .findViewById(R.id.AboutImageView);

                if (AboutImgView != null)
                    AboutImgView.setImageResource(R.mipmap.dmr_print_logos_tamanos_circulos);

                // set up button
                Button closeButton = (Button) about.findViewById(R.id.AboutCloseButton);
                if (closeButton != null) {
                    closeButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            about.dismiss();
                        }
                    });
                }

                about.show();
            }
        }
        catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
    }


    @Override
    public void onBackStackChanged() {
        shouldDisplayHomeUp();
    }

    private void Cargar_Datos_Configuracion() {


        DMRPrintSettings appSettings = ReadApplicationSettingFromFile();
        if (appSettings == null) {
            showToast("No se ha Configurado la impresora");
        }
    }

    DMRPrintSettings ReadApplicationSettingFromFile() {
        DMRPrintSettings ret = null;
        InputStream instream;
        try {
            instream = openFileInput(ApplicationConfigFilename);
        } catch (FileNotFoundException e) {
            showToast("Cargar su Configuracion...");
            return null;
        }
        try {
            ObjectInputStream ois = new ObjectInputStream(instream);
            try {
                ret = (DMRPrintSettings) ois.readObject();
            } catch (ClassNotFoundException e) {
                ret = null;
            }
        } catch (Exception e) {
            ret = null;
        } finally {
            try {
                if (instream != null)
                    instream.close();
            } catch (IOException ignored) {
            }
        }
        return ret;
    }




}
