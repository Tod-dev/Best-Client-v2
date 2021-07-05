package it.bestclient.android.components;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;

import it.bestclient.android.HomeActivity;
import it.bestclient.android.R;

public class RowAdapter extends ArrayAdapter<String> {
    Context context;
    String[] field1;    //Phone number / Contact name
    String[] field2;    //Date / Phone number
    String[] field3;    //Comment
    String[] field4;    //Current Rating
    String[] field5;    //AVG rating

    public RowAdapter(Context context, String[] field1, String[] field2, String[] field3, String[] field4, String[] field5){
        super(context, R.layout.rows,R.id.field1, field1);
        this.context = context;
        this.field1 = field1;
        this.field2 = field2;
        this.field3 = field3;
        this.field4 = field4;
        this.field5 = field5;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
            field4View.setText("Current rating: -");
        else
            field4View.setText("Current rating: "+field4[position]);

        if(field5[position] == null || field5[position].equals(""))
            field5View.setText("AVG rating: -");
        else
            field5View.setText("AVG rating: "+field5[position]);

        return row;
    }
}
