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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Registrar_series_mac extends AppCompatActivity {

    private static final String TAG = "mENSAJEmacserie";
    private FirebaseFirestore firebasefirestore;
    ArrayList<SeriesyMac> list = new ArrayList<>();
    private SeriesymacAdapter adapter;

    private TextView fecha, usuario, idtablaserie,nmac,numeroserie;
    private EditText filtrar;
    private Handler m_handler = new Handler();
    private Button eliminar , limpiar;
    private Spinner filtro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_series_mac);

        fecha = findViewById(R.id.txt_fecha_seriemac);
        usuario = findViewById(R.id.txt_usuario_seriemac);
        numeroserie = findViewById(R.id.txt_nserie);
        idtablaserie = findViewById(R.id.txt_idtabla_seriemac);
        nmac = findViewById(R.id.txt_nmac);
        filtrar = findViewById(R.id.txtfiltrar);
        filtro = findViewById(R.id.txt_spiner);
        limpiar = findViewById(R.id.btn_limpiar);
        eliminar = findViewById(R.id.btn_eliminar_seriemac);

        initFireBase();

        Mostrar_Series();



        adapter = new SeriesymacAdapter();
        adapter.setOnNoteSelectedListener(new SeriesymacAdapter.OnNoteSelectedListener() {
            @Override
            public void onClick(SeriesyMac note) {

                filtrar.setEnabled(false);
                eliminar.setEnabled(true);

                idtablaserie.setText(note.getId());
                numeroserie.setText(note.getNserie());
                nmac.setText(note.getNmac());
                usuario.setText(note.getIdusuario());
                fecha.setText(note.getFecha());
            }

        });


        filtrar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (filtrar.getText().length() > 0) {
                    filtrar(filtrar.getText().toString().trim());
                } else {
                    actualizarReciclerView();
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });



        eliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!idtablaserie.getText().equals("")){
                    Eliminar();
                }




            }
        });

        limpiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                restablecer();


            }
        });



        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.reciclerview_seriemac);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);

    }

    private void filtrar(String s) {

        List<SeriesyMac> seriesfilter = new ArrayList<>();
        List<SeriesyMac> seriesfilter2 = new ArrayList<>();

        seriesfilter.clear();

        for (SeriesyMac item : list) {

            if (filtro.getSelectedItem().toString().equals("SERIE")){

                if (item.getNserie().toUpperCase().contains(s.toUpperCase())) {
                    seriesfilter.add(item);
                }

            }else if (filtro.getSelectedItem().toString().equals("EMAIL")){

                if (item.getEmail().toUpperCase().contains(s.toUpperCase())) {

                    seriesfilter.add(item);
                }
            }else{
                if (item.getNmac().toUpperCase().contains(s.toUpperCase())) {

                    seriesfilter.add(item);
                }
            }

            adapter.setNotes(seriesfilter);
            adapter.notifyDataSetChanged();

        }

    }


    private void initFireBase() {

        firebasefirestore = FirebaseFirestore.getInstance();


    }


    public void Eliminar() {

        if (!idtablaserie.getText().toString().equals("")){
            firebasefirestore.collection("TABLASERIALMAC").document(idtablaserie.getText().toString())
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

    }


    public void restablecer() {

        filtrar.setEnabled(true);
        eliminar.setEnabled(false);
        nmac.setText("");
        numeroserie.setText("");
        idtablaserie.setText("");
        usuario.setText("");
        fecha.setText("");
        actualizarReciclerView();
    }



    public void actualizarReciclerView() {
        adapter.setNotes(list);
        adapter.notifyDataSetChanged();
    }

    private void Mostrar_Series() {

        firebasefirestore.collection("TABLASERIALMAC")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> resultado) {

                        if (resultado.getResult().size() > 0) {

                            list.clear();

                            for (DocumentSnapshot tablaseriesmac : resultado.getResult().getDocuments()) {

                                SeriesyMac jugada = new SeriesyMac();
                                jugada.setId(tablaseriesmac.getId());
                                jugada.setFecha(tablaseriesmac.getString("fecha"));
                                jugada.setNserie(tablaseriesmac.getString("nserie"));
                                jugada.setIdusuario(tablaseriesmac.getString("idusuario"));
                                jugada.setNmac(tablaseriesmac.getString("nmac"));
                                jugada.setEmail(tablaseriesmac.getString("email"));
                                list.add(jugada);

                            }

                            restablecer();

                        }else{
                            list.clear();
                        }
                    }
                });

    }

    public void maketest(final String msj) {

        Toast.makeText(Registrar_series_mac.this, msj, Toast.LENGTH_SHORT).show();

    }
}