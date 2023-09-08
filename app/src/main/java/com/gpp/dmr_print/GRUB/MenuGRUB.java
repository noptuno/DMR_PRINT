package com.gpp.dmr_print.GRUB;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gpp.dmr_print.MainActivity;
import com.gpp.dmr_print.Pdf_view;
import com.gpp.dmr_print.R;

public class MenuGRUB extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_grub);
    }

    public void series(View view){

        Intent I = new Intent(MenuGRUB.this, Registrar_Series.class);
        startActivity(I);
    }

    public void series_y_mac(View view){

        Intent I = new Intent(MenuGRUB.this, Registrar_series_mac.class);
        startActivity(I);

    }
}
