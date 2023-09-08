package com.gpp.dmr_print.Usuario;


import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.gpp.dmr_print.GRUB.SeriesyMac;
import com.gpp.dmr_print.GRUB.SeriesymacAdapter;
import com.gpp.dmr_print.MainActivity;
import com.gpp.dmr_print.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */

public class Fragment_usuario extends Fragment {

    private FirebaseFirestore firebasefirestore;
    ArrayList<SeriesyMac> list = new ArrayList<>();
    private static final String TAG = "mensaje_error";
    private TextView email;
    private EditText empresa, telefono;
    private ImageView img_usaurio;
    private FirebaseAuth mAuth;
    private Context applicationContext;
    private Button guardar;
    private LinearLayout linear;
    private SeriesymacAdapter adapter;
    private String UID = null;
    MediaPlayer sonido;
private Context mContext;
    public Fragment_usuario() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View v = inflater.inflate(R.layout.fragment_fragment_usuario, container, false);
        mAuth = FirebaseAuth.getInstance();

        sonido = MediaPlayer.create(mContext,R.raw.ckickk);

        applicationContext = MainActivity.getContextOfApplication();
        linear = v.findViewById(R.id.linear);
        email = v.findViewById(R.id.TextView_email);
        empresa = v.findViewById(R.id.txt_empresa);
        telefono = v.findViewById(R.id.txt_telefono);
        img_usaurio = v.findViewById(R.id.img_Usuario);

        guardar = v.findViewById(R.id.btn_guardar);

        firebasefirestore = FirebaseFirestore.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        adapter = new SeriesymacAdapter();

        updateUI(currentUser);




        guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (UID != null) {

                    if (empresa.getText().toString().length()>0 && telefono.getText().toString().length()>0 ){
                        Usuario Usuario = new Usuario();
                        Usuario.setId(UID);
                        Usuario.setEmpresa(empresa.getText().toString());
                        Usuario.setTelefono(telefono.getText().toString());
                        Usuario.setCorreo(email.getText().toString());
                        guardardatos(Usuario);
                        sonido.start();
                    }else{
                        empresa.setError("faltan datos");
                        telefono.setError("faltan datos");
                    }

                }

            }
        });


        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.Listview_macs);
        recyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(adapter);


        return v;
    }

    private void guardardatos(Usuario usuario) {



        Map<String, Object> city = new HashMap<>();
        city.put("correo", usuario.getCorreo().toUpperCase());
        city.put("empresa", usuario.getEmpresa().toUpperCase());
        city.put("telefono", usuario.getTelefono().toUpperCase());

        firebasefirestore.collection("TABLAUSUARIO")
                .document(UID)
                .set(city)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(mContext,"Se Guardo Correctamente Gracias...",Toast.LENGTH_SHORT).show();
                    }
                });

    }

    public void actualizarReciclerView() {
        adapter.setNotes(list);
        adapter.notifyDataSetChanged();
    }



    private void updateUI(FirebaseUser currentUser) {

        if (currentUser != null) {
            email.setText(currentUser.getEmail());
            UID = currentUser.getUid();
            Mostrar_Usuario();
            Mostrar_Series();

            try {
                if (currentUser.getPhotoUrl() != null) {
                    String personPhotoUrl = currentUser.getPhotoUrl().toString();
                    Glide.with(applicationContext).load(personPhotoUrl)
                            .thumbnail(0.5f)
                            .circleCrop()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .into(img_usaurio);
                }
            } catch (Exception e) {
                Log.e("error", "no hay foto");
            }

        }

    }



    private void Mostrar_Series() {

        firebasefirestore.collection("TABLASERIALMAC")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> resultado) {

                        if (resultado.getResult().size() > 0) {

                            list.clear();
                            for (DocumentSnapshot tablaseialmac : resultado.getResult().getDocuments()) {

                                if (tablaseialmac.getString("idusuario").equals(UID)) {

                                    SeriesyMac jugada = new SeriesyMac();
                                    jugada.setId(tablaseialmac.getId());
                                    jugada.setFecha(tablaseialmac.getString("fecha"));
                                    jugada.setNserie(tablaseialmac.getString("nserie"));
                                    jugada.setIdusuario(tablaseialmac.getString("idusuario"));
                                    jugada.setNmac(tablaseialmac.getString("nmac"));
                                    jugada.setEmail(tablaseialmac.getString("email"));
                                    list.add(jugada);

                                }
                            }

                            actualizarReciclerView();

                        }else{
                            list.clear();
                        }
                    }
                });

    }

    private void Mostrar_Usuario() {

        firebasefirestore.collection("TABLAUSUARIO")
                .whereEqualTo(FieldPath.documentId(), UID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> resultado) {

                        if (resultado.getResult().size() == 1) {

                            for (QueryDocumentSnapshot document : resultado.getResult()) {

                                empresa.setText(document.getString("empresa"));
                                telefono.setText(document.getString("telefono"));

                               // Log.d(TAG, document.getId() + " => " + document.getData());

                                //mensaje_error: VYQN7prn7wV1PHLssVM2MIFM0013 => {correo=DAVID.NOCWOW@GMAIL.COM, empresa=DAVID, telefono=123456}
                            }

                        }else{

                        }

                    }
                });

    }

}
