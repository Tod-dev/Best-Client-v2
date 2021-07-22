package it.bestclient.android.components;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import it.bestclient.android.HomeActivity;
import it.bestclient.android.R;
import it.bestclient.android.RatingActivity;
import it.bestclient.android.RatingModel.Rating;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.MyViewHolder> {
    Activity myActivity;
    Context context;
    String[] field1;    //Phone number / Contact name
    double[] field2;    //Rating assegnato
    double[] field3;    //Rating medio

    List<Rating> filteredRatings;

    public RowAdapter(Activity myActivity, Context context, String[] field1, double[] field2, double[] field3){
        //super(context, R.layout.rows,R.id.field1, field1);
        this.myActivity = myActivity;
        this.context = context;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
        this.filteredRatings = new ArrayList<>();
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

        if(field2[position] == -1.0){
            holder.logoView.setVisibility(View.INVISIBLE);
        }

        String actualNumber = field1[position];

        for (Contact c : HomeActivity.contacts){
            //scorro tutti i contatti che sono riuscito a leggere dalla rubrica
            if(c.getPhone().equals(actualNumber)){
                //ho trovato un numero in rubrica !
                //scrivo il nome e non il numero!
                actualNumber = c.getName();
            }
        }

        holder.field1View.setText(actualNumber);

        if(field2[position] >= 0){
            holder.votoAssegnato.setRating((float) field2[position]);
        }

        if(field3[position] >= 0){
            holder.votoMedio.setRating((float) field3[position]);
        }

        holder.mainLayout.setOnClickListener(v -> {

            String number = field1[position];
            Rating clicked = null;

            for(Rating r: HomeActivity.ratings){
                if(r.getNumero().equals(number)){
                    clicked = r;
                    break;
                }
            }

            Intent intent = new Intent(context, RatingActivity.class);
            intent.putExtra(RatingActivity.NUMBER,clicked.getNumero());
            intent.putExtra(RatingActivity.VOTO,clicked.getVoto());
            intent.putExtra(RatingActivity.COMMENT,clicked.getCommento());
            intent.putExtra(RatingActivity.MEDIO,clicked.getVoto_medio());
            intent.putExtra(RatingActivity.PUBBLICA,clicked.getPubblica());

            context.startActivity(intent);
            myActivity.overridePendingTransition(R.anim.to_right_in, R.anim.to_left_out);
        });

    }

    @Override
    public int getItemCount() {
        return field1.length;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void filter(String text, Context context){
        filteredRatings = new ArrayList<>();
        for(Rating r: HomeActivity.ratings){
            if(r.getNumero().contains(text) || r.getNome().contains(text)){
                filteredRatings.add(r);
            }
        }

        /* INSERT ALL THE RATINGS IN THE LISTVIEW */
        HomeActivity.showRatings(context, filteredRatings);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        RatingBar votoAssegnato;
        RatingBar votoMedio;
        ImageView logoView;
        TextView field1View;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            votoAssegnato = itemView.findViewById(R.id.ratingBarAssegnato);
            votoMedio = itemView.findViewById(R.id.ratingBarMedio);
            logoView = itemView.findViewById(R.id.logoRating);
            field1View = itemView.findViewById(R.id.field1);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
