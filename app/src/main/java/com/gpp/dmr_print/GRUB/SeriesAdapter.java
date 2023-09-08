package com.gpp.dmr_print.GRUB;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Filter;
import androidx.recyclerview.widget.RecyclerView;

import com.gpp.dmr_print.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class SeriesAdapter extends RecyclerView.Adapter<SeriesAdapter.NoteViewHolder> {

    private List<Series> notes;
    private List<Series> notes2;
    private OnNoteSelectedListener onNoteSelectedListener;

    private Context contex;

    public SeriesAdapter() {
        this.notes = new ArrayList<>();
    }

    public SeriesAdapter(List<Series> notes) {
        this.notes = notes;
    }


    @Override
    public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View elementoTitular = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.lista_series, parent, false);

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


    public List<Series> getNotes() {
        return notes;
    }

    public void setNotes(List<Series> notes) {
        this.notes = notes;
    }

    public void setOnNoteSelectedListener(OnNoteSelectedListener onNoteSelectedListener) {
        this.onNoteSelectedListener = onNoteSelectedListener;
    }


    public interface OnNoteSelectedListener {
        void onClick(Series note);
    }



    public class NoteViewHolder extends RecyclerView.ViewHolder {

        private TextView codigo;
        private TextView fecha;
        private TextView usaurio;
        private TextView seriee;

        public NoteViewHolder(View item) {
            super(item);

            codigo = (TextView) item.findViewById(R.id.txt_idseries);
            fecha = (TextView) item.findViewById(R.id.txt_fecha);
            usaurio = (TextView) item.findViewById(R.id.txt_idusuario);
            seriee = (TextView) item.findViewById(R.id.txt_numeroserie);

        }

        public void bind(final Series serie) {

            codigo.setText(serie.getId());
            fecha.setText(serie.getFecha().toString());
            usaurio.setText(serie.getIdusuario().toString());
            seriee.setText(serie.getNserie().toString());
          //  descripcion.setText("mal");

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (onNoteSelectedListener != null) {
                        onNoteSelectedListener.onClick(serie);
                    }
                }
            });

        }


    }
}
