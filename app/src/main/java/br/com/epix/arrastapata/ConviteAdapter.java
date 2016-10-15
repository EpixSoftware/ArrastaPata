package br.com.epix.arrastapata;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by martelli on 5/29/16.
 */
public class ConviteAdapter extends ArrayAdapter<Convite> {

    private static LayoutInflater inflater = null;
    Convite[] data;
    Context context;

    public ConviteAdapter(Context context, int resource) {
        super(context, resource);
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;
        if (vi == null)
            vi = inflater.inflate(R.layout.item, null);
        TextView text = (TextView) vi.findViewById(R.id.text);
        text.setText(getItem(position).getNumero());
        if (getItem(position).getSorteado()==1)
            text.setTextColor(Color.rgb(255, 0, 0));
        else
            text.setTextColor(Color.rgb(255, 255, 255));
        return vi;
    }
}
