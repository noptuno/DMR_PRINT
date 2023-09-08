package com.gpp.dmr_print;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.gpp.dmr_print.GRUB.Registrar_Series;
import com.gpp.dmr_print.Usuario.InicioSesion;


public class InicioFash extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio_fash);

        new Handler().postDelayed(new Runnable(){
            @Override
            public void run() {

                Intent mainIntent = new Intent(InicioFash.this, MainActivity.class);
                InicioFash.this.startActivity(mainIntent);
                InicioFash.this.finish();

/*
                pref = getSharedPreferences("LOGIN", Context.MODE_PRIVATE);
                String estado_login = pref.getString("ESTADO_LOGIN", "NO");
                if (estado_login.equals("SI")) {

                    Intent mainIntent = new Intent(InicioFash.this, MainActivity.class);
                    InicioFash.this.startActivity(mainIntent);
                    InicioFash.this.finish();

                } else {

                    Intent mainIntent = new Intent(InicioFash.this, InicioSesion.class);
                    InicioFash.this.startActivity(mainIntent);
                    InicioFash.this.finish();
                }
*/
            }


        }, 1500);

    }
}
