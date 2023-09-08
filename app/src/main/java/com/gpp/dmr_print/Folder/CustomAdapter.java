package com.gpp.dmr_print.Folder;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.gpp.dmr_print.MainActivity;
import com.gpp.dmr_print.Pdf_view;
import com.gpp.dmr_print.R;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Oclemy on 7/28/2016 for ProgrammingWizards Channel and http://www.camposha.com.
 */

public class CustomAdapter extends BaseAdapter {

    Context c;
    ArrayList<PDFDoc> pdfDocs;

    public CustomAdapter(Context c, ArrayList<PDFDoc> pdfDocs) {
        this.c = c;
        this.pdfDocs = pdfDocs;
    }

    @Override
    public int getCount() {
        return pdfDocs.size();
    }

    @Override
    public Object getItem(int i) {
        return pdfDocs.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if(view==null)
        {
            //INFLATE CUSTOM LAYOUT
            view= LayoutInflater.from(c).inflate(R.layout.model,viewGroup,false);
        }

        final PDFDoc pdfDoc= (PDFDoc) this.getItem(i);

        TextView nameTxt= (TextView) view.findViewById(R.id.nameTxt);
        ImageView img= (ImageView) view.findViewById(R.id.pdfImage);

        //BIND DATA
        nameTxt.setText(pdfDoc.getName());
        img.setImageResource(R.drawable.file_pdf);

        //VIEW ITEM CLICK
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               openPDFView(pdfDoc.getPath());
            }
        });

        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                //Mostrar un mensaje de confirmación antes de realizar el test

                AlertDialog.Builder alertDialog = new AlertDialog.Builder(c);
                alertDialog.setMessage("¿Desea eliminar el Documento?");
                alertDialog.setTitle("Eliminar Pdf");
                alertDialog.setIcon(android.R.drawable.ic_dialog_alert);
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton("Sí", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        eliminarpdf(pdfDoc.getPath());

                    }
                });
                alertDialog.setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which)
                    {
                        //código java si se ha pulsado no
                    }
                });
                alertDialog.show();

                return true;
            }
        });
        return view;
    }

    private void eliminarpdf(String path)
    {
            File archivo = new File(path);
            archivo.delete();
    }

    //OPEN PDF VIEW
    private void openPDFView(String path)
    {
        Intent i=new Intent(c, Pdf_view.class);
        i.setAction("ENVIANDO_FOLDER");
        i.putExtra("PATH",path);
        c.startActivity(i);


    }
}
