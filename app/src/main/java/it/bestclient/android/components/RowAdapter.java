package it.bestclient.android.components;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import it.bestclient.android.ContactsActivity;
import it.bestclient.android.HomeActivity;
import it.bestclient.android.R;
import it.bestclient.android.RatingModel.RatingLocal;
import it.bestclient.android.Utils;

public class RowAdapter extends RecyclerView.Adapter<RowAdapter.MyViewHolder> {
    Context context;
    String[] field1;    //Phone number / Contact name
    String[] field2;    //Date / Phone number
    String[] field3;    //Comment
    String[] field4;    //Current Rating
    String[] field5;    //AVG rating

    public RowAdapter(Context context, String[] field1, String[] field2, String[] field3, String[] field4, String[] field5){
        //super(context, R.layout.rows,R.id.field1, field1);
        this.context = context;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
        this.field4 = field4;
        this.field5 = field5;
    }

    /*@RequiresApi(api = Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater layoutInflater = (LayoutInflater) context.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        @SuppressLint("ViewHolder")
        View row = layoutInflater.inflate(R.layout.rows, parent, false);
        TextView field1View = row.findViewById(R.id.field1);
        TextView field2View = row.findViewById(R.id.field2);
        TextView field3View = row.findViewById(R.id.field3);
        TextView field4View = row.findViewById(R.id.field4);
        TextView field5View = row.findViewById(R.id.field5);

        String actualNumber = field1[position];

        for (Contact c : HomeActivity.contacts){
            //scorro tutti i contatti che sono riuscito a leggere dalla rubrica
            Log.d("CONTACTS: ","confronto :'"+c.getPhone()+"'=='"+actualNumber+"'");
            if(c.getPhone().equals(actualNumber)){
                //ho trovato un numero in rubrica !
                //scrivo il nome e non il numero!
                actualNumber = c.getName();
                Log.d("CONTACTS: ","scrivo :'"+c.getName()+"'");
            }
        }



        field1View.setText(actualNumber);
        field2View.setText(field2[position]);
        if(field3[position].equals("")){
            field3View.setText("No comment");
        }
        else field3View.setText(field3[position]);

        if(field4[position].equals("-1.0") || field4[position].equals(""))
            field4View.setText("Rating assegnato: -");
        else
            field4View.setText("Rating assegnato: "+field4[position]);

        if(field5[position] == null || field5[position].equals(""))
            field5View.setText("Rating Medio: -");
        else
            field5View.setText("Rating Medio: "+field5[position]);

        return row;
    }*/

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
            Log.d("CONTACTS: ","confronto :'"+c.getPhone()+"'=='"+actualNumber+"'");
            if(c.getPhone().equals(actualNumber)){
                //ho trovato un numero in rubrica !
                //scrivo il nome e non il numero!
                actualNumber = c.getName();
                Log.d("CONTACTS: ","scrivo :'"+c.getName()+"'");
            }
        }

        holder.field1View.setText(actualNumber);
        holder.field2View.setText(field2[position]);
        if(field3[position].equals("")){
            text = "No comment";
            holder.field3View.setText(text);
        }
        else holder.field3View.setText(field3[position]);

        if(field4[position].equals("-1.0") || field4[position].equals("")){
            text = "Rating assegnato: -";
        }
        else{
            text = "Rating assegnato: "+field4[position];
        }
        holder.field4View.setText(text);


        if(field5[position] == null || field5[position].equals("")){
            text = "Rating Medio: -";
        }
        else{
            text = "Rating Medio: "+field5[position];
        }
        holder.field5View.setText(text);

        holder.mainLayout.setOnClickListener(v -> {
            String className = context.getClass().getSimpleName();
            if(className.equals("HomeActivity")){
                RatingLocal r = HomeActivity.ratings.get(position);
                Utils.showDialog(context, 2, r);
            }
            else{
                RatingLocal r = ContactsActivity.ratings.get(position);
                Utils.showDialog(context, 3, r);
            }

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
        TextView field4View;
        TextView field5View;
        LinearLayout mainLayout;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            field1View = itemView.findViewById(R.id.field1);
            field2View = itemView.findViewById(R.id.field2);
            field3View = itemView.findViewById(R.id.field3);
            field4View = itemView.findViewById(R.id.field4);
            field5View = itemView.findViewById(R.id.field5);
            mainLayout = itemView.findViewById(R.id.mainLayout);
        }
    }
}
