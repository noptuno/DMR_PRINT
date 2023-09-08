package com.gpp.dmr_print.GRUB;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.gpp.dmr_print.R;

import java.util.ArrayList;
import java.util.List;

public class SeriesymacAdapter extends RecyclerView.Adapter<SeriesymacAdapter.NoteViewHolder> {

    private List<SeriesyMac> notes;
    private List<SeriesyMac> notes2;
    private OnNoteSelectedListener onNoteSelectedListener;

    private Context contex;

    public SeriesymacAdapter() {
        this.notes = new ArrayList<>();
    }

    public SeriesymacAdapter(List<SeriesyMac> notes) {
        this.notes = notes;
    }


    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View elementoTitular = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lista_seriesymac, parent, false);

        contex = elementoTitular.getContext();

        return new NoteViewHolder(elementoTitular);
    }

    @Override
    public void onBindViewHolder(NoteViewHolder view, int pos) {
        view.bind(notes.get(pos));
    }


    @Override
    public int getItemCount() {
        return notes.size();
    }


    public List<SeriesyMac> getNotes() {
        return notes;
    }

    public void setNotes(List<SeriesyMac> notes) {
        this.notes = notes;
    }

    public void setOnNoteSelectedListener(OnNoteSelectedListener onNoteSelectedListener) {
        this.onNoteSelectedListener = onNoteSelectedListener;
    }


    public interface OnNoteSelectedListener {
        void onClick(SeriesyMac note);
    }



    public class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView codigo;
        private TextView fecha;
        private TextView usaurio;
        private TextView seriee;
        private  TextView txtnmac,txtemail;
        private LinearLayout linear;

        public NoteViewHolder(View item) {
            super(item);

            codigo = (TextView) item.findViewById(R.id.txt_idseries);
            fecha = (TextView) item.findViewById(R.id.txt_fecha);
            usaurio = (TextView) item.findViewById(R.id.txt_idusuario);
            seriee = (TextView) item.findViewById(R.id.txt_numeroserie);
            txtnmac= (TextView) item.findViewById(R.id.txt_nmac);
            txtemail= (TextView) item.findViewById(R.id.txt_email);
            linear = item.findViewById(R.id.linear);
        }

        public void bind(final SeriesyMac serie) {

            codigo.setText(serie.getId());
            fecha.setText(serie.getFecha().toString());
            usaurio.setText(serie.getIdusuario().toString());
            seriee.setText(serie.getNserie().toString());
            txtnmac.setText(serie.getNmac().toString());
            txtemail.setText(serie.getEmail().toString());
            linear.setBackgroundColor(0xFF3B5998);

          //  descripcion.setText("mal");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onNoteSelectedListener != null) {
                        linear.setBackgroundColor(0xffff8800);
                        onNoteSelectedListener.onClick(serie);
                    }
                }
            });

        }


    }
}
