package com.gpp.dmr_print.Folder;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;

import com.gpp.dmr_print.MainActivity;
import com.gpp.dmr_print.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

public class Fragmen_folder extends Fragment {

    private ImageView img_folder;
    private GridView gv;

private Context context;
    public Fragmen_folder() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v =  inflater.inflate(R.layout.fragment_fragmen_folder, container, false);

        context = v.getContext();
        gv = (GridView) v.findViewById(R.id.gv);
        cargarlistview();



        return  v;

    }

    public void cargarlistview() {

        if( ((MainActivity) getActivity()).get_permisos()){

            gv.setAdapter(new CustomAdapter(context,getPDFs()));
        }


    }

    private ArrayList<PDFDoc> getPDFs() {

        ArrayList<PDFDoc> pdfDocs=new ArrayList<>();
        //TARGET FOLDER
        File downloadsFolder= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        PDFDoc pdfDoc;

        if(downloadsFolder.exists())
        {
            //GET ALL FILES IN DOWNLOAD FOLDER
            File[] files=downloadsFolder.listFiles();

            //LOOP THRU THOSE FILES GETTING NAME AND URI
            try {
                for (int i=0;i<files.length;i++)
                {
                    File file=files[i];

                    if(file.getPath().endsWith(".pdf"))
                    {
                        pdfDoc=new PDFDoc();
                        pdfDoc.setName(file.getName());
                        pdfDoc.setPath(file.getAbsolutePath());
                        pdfDocs.add(pdfDoc);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return pdfDocs;
    }



}






