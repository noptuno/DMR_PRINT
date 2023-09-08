package com.gpp.dmr_print.GRUB;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.model.DocumentData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.gpp.dmr_print.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registrar_Series extends AppCompatActivity {

    private static final String TAG = "mENSAJE";
    private FirebaseAuth firebaseauth;
    private FirebaseFirestore firebasefirestore;
    private FirebaseUser firebaseuser;
    private String UID;
    ArrayList<Series> list = new ArrayList<>();
    private SeriesAdapter adapter;

    private TextView fecha, usuario, idtablaserie;
    private EditText numeroserie;
    private Handler m_handler = new Handler();
    private Button registrar, nuevo, eliminar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar__series);

        fecha = findViewById(R.id.txt_fecha);
        usuario = findViewById(R.id.txt_usuario);
        numeroserie = findViewById(R.id.txt_nserie);
        idtablaserie = findViewById(R.id.txt_idtablaserie);

        registrar = findViewById(R.id.btn_registrar);
        nuevo = findViewById(R.id.btn_nuevo);
        eliminar = findViewById(R.id.btn_eliminar);

        initFireBase();

        Mostrar_Series();

        adapter = new SeriesAdapter();
        adapter.setOnNoteSelectedListener(new SeriesAdapter.OnNoteSelectedListener() {
            @Override
            public void onClick(Series note) {

                numeroserie.setEnabled(false);

                registrar.setEnabled(false);
                nuevo.setEnabled(true);
                eliminar.setEnabled(true);

                idtablaserie.setText(note.getId());
                numeroserie.setText(note.getNserie());
            }

        });


        numeroserie.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (numeroserie.getText().length()>0){
                    filtrar(numeroserie.getText().toString().trim());
                }else{
                    actualizarReciclerView();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        registrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (numeroserie.getText().length()>0 && !fecha.getText().equals("") && !usuario.getText().equals("")){
                    Series tablaserie = new Series();
                    tablaserie.setIdusuario(usuario.getText().toString().trim());
                    tablaserie.setFecha(fecha.getText().toString().trim());
                    tablaserie.setNserie(numeroserie.getText().toString().trim());
                    validarSerialTablaFirebase(tablaserie);
                }else{
                    maketest("Faltan datos");
                }


            }
        });

        nuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NuevoRegistro();


            }
        });

        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Eliminar();


            }
        });


        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.reciclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

    }

    private void filtrar(String s) {

                 List<Series> seriesfilter = new ArrayList<>();
                 List<Series> seriesfilter2 = new ArrayList<>();

                    for (Series item : list) {

                        if (item.getNserie().toLowerCase().equals(s)) {

                            seriesfilter.add(item);

                            adapter.setNotes(seriesfilter);
                            adapter.notifyDataSetChanged();
                            break;

                        }else{

                            adapter.setNotes(seriesfilter2);
                            adapter.notifyDataSetChanged();
                        }
                        
                    }

    }


    private void initFireBase() {

        firebaseauth = FirebaseAuth.getInstance();
        firebasefirestore = FirebaseFirestore.getInstance();
        firebaseuser = firebaseauth.getCurrentUser();

        if (firebaseuser != null) {

            String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

            UID = firebaseuser.getUid();//no es necesario en esta tabla

            usuario.setText(firebaseuser.getEmail());
            fecha.setText(date);

        } else {
            Log.w("ERROR:", "No existe un UID");
        }


    }

    public void NuevoRegistro() {

        restablecer();
        registrar.setEnabled(true);
        numeroserie.requestFocus();

    }

    public void Eliminar() {

        if (!idtablaserie.getText().toString().equals("")){
            firebasefirestore.collection("TABLASERIES").document(idtablaserie.getText().toString())
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            Log.d(TAG, "DocumentSnapshot successfully deleted!");

                            Mostrar_Series();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Error deleting document", e);
                        }
                    });
        }else{
            maketest("Debe seleccionar algun registro");
        }


        restablecer();

    }

    public void Registrar(Series tablaserie) {

        String[] toppings = {"Cheese", "Pepperoni", "Black Olives"};


        Map<String, Object> city = new HashMap<>();

        city.put("fecha", tablaserie.getFecha());
        city.put("idusuario", tablaserie.getIdusuario().toUpperCase());
        city.put("nserie", tablaserie.getNserie().toUpperCase());


        firebasefirestore.collection("TABLASERIES")
                .add(city)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());
                        maketest("Registro Exitoso");
                        Mostrar_Series();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });

        restablecer();

    }





    public void restablecer() {

        numeroserie.setEnabled(true);
        registrar.setEnabled(true);
        nuevo.setEnabled(true);
        eliminar.setEnabled(false);

        numeroserie.setText("");
        idtablaserie.setText("");
    }

    private void validarSerialTablaFirebase(final Series tablaserie) {

        firebasefirestore.collection("TABLASERIES")
                .whereEqualTo("nserie", tablaserie.getNserie().trim())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> resultado) {

                        if (resultado.getResult().size() > 0) {

                         maketest("El numero de Serie ya se encuentra registrado");

                        } else {

                            Registrar(tablaserie);
                        }
                    }
                });
    }


    public void actualizarReciclerView() {
        adapter.setNotes(list);
        adapter.notifyDataSetChanged();
    }

    private void Mostrar_Series() {

        firebasefirestore.collection("TABLASERIES")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> resultado) {

                        if (resultado.getResult().size() > 0) {

                            list.clear();

                            for (DocumentSnapshot tablaseries : resultado.getResult().getDocuments()) {

                                Series jugada = new Series();
                                jugada.setId(tablaseries.getId());
                                jugada.setFecha(tablaseries.getString("fecha"));
                                jugada.setNserie(tablaseries.getString("nserie"));
                                jugada.setIdusuario(tablaseries.getString("idusuario"));
                                list.add(jugada);

                            }

                            actualizarReciclerView();

                        }
                    }
                });

    }

    public void maketest(final String msj){

                Toast.makeText(Registrar_Series.this, msj, Toast.LENGTH_SHORT).show();

    }

}
