package it.bestclient.android.components;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import it.bestclient.android.HomeActivity;
import it.bestclient.android.R;
import it.bestclient.android.RatingActivity;
import it.bestclient.android.RatingModel.Rating;
import it.bestclient.android.Utils;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.MyViewHolder> {
    Context context;
    String[] field1;    //Phone number / Contact name
    String[] field2;    //Rating assegnato
    String[] field3;    //Rating medio

    public RowAdapter(Context context, String[] field1, String[] field2, String[] field3){
        //super(context, R.layout.rows,R.id.field1, field1);
        this.context = context;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.rows, parent, false);
        return new MyViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String actualNumber = field1[position];
        String text;

        for (Contact c : HomeActivity.contacts){
            //scorro tutti i contatti che sono riuscito a leggere dalla rubrica
            if(c.getPhone().equals(actualNumber)){
                //ho trovato un numero in rubrica !
                //scrivo il nome e non il numero!
                actualNumber = c.getName();
            }
        }

        holder.field1View.setText(actualNumber);

        if(field2[position].equals("-1.0") || field2[position].equals("")){
            text = "Rating assegnato: -";
        }
        else{
            text = "Rating assegnato: "+field2[position];
        }
        holder.field2View.setText(text);


        if(field3[position] == null || field3[position].equals("")){
            text = "Rating Medio: -";
        }
        else{
            text = "Rating Medio: "+field3[position];
        }
        holder.field3View.setText(text);


        holder.mainLayout.setOnClickListener(v -> {

            Rating r = HomeActivity.ratings.get(position);

            Intent intent = new Intent(context, RatingActivity.class);
            intent.putExtra(RatingActivity.NUMBER,r.getNumero());
            intent.putExtra(RatingActivity.VOTO,r.getVoto());
            intent.putExtra(RatingActivity.COMMENT,r.getCommento());
            intent.putExtra(RatingActivity.MEDIO,r.getVoto_medio());

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return field1.length;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView field1View;
        TextView field2View;
        TextView field3View;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            field1View = itemView.findViewById(R.id.field1);
            field2View = itemView.findViewById(R.id.field2);
            field3View = itemView.findViewById(R.id.field3);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
