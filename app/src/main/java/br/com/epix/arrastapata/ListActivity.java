package br.com.epix.arrastapata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.util.StringBuilderPrinter;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import br.com.epix.arrastapata.db.ConvitesDbHelper;

public class ListActivity extends Activity {

    private static String TAG = ListActivity.class.getCanonicalName();
    private Button sortear;
    private Button reset;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        sortear = (Button) findViewById(R.id.sortear);
        reset = (Button) findViewById(R.id.reset);
        textView = (TextView) findViewById(R.id.textView);

        final ConviteAdapter adapter = new ConviteAdapter(this, android.R.layout.simple_list_item_1);

        ConvitesDbHelper dbHelper = new ConvitesDbHelper(this);
        final SQLiteDatabase db = dbHelper.getWritableDatabase();

        Cursor cursor = db.query("convites", new String[] {"numero", "sorteado"}, null, null, null, null, "numero");

        while (cursor.moveToNext())
            adapter.add(new Convite(cursor.getString(0), cursor.getInt(1)));

        GridView listView = (GridView) findViewById(R.id.listView);

        listView.setAdapter(adapter);

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder adb=new AlertDialog.Builder(ListActivity.this);
                adb.setTitle("Apagar?");
                adb.setMessage("Remover convite número: " + parent.getItemAtPosition(position));
                final int positionToRemove = position;
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            String opt = adapter.getItem(position).getNumero();
                            db.execSQL("DELETE FROM convites WHERE numero = ?", new Object[] {opt});
                            adapter.remove(adapter.getItem(position));
                            adapter.notifyDataSetChanged();
                        } catch (Exception ex) {

                        }

                    }});
                adb.show();
                return true;
            }
        });

        sortear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<Convite, Integer> mapa = new HashMap<>();
                Log.d(TAG, "sorteando!");
                ArrayList<Convite> numeros = new ArrayList<>();
                Random random = new Random(System.currentTimeMillis());

                for(int i=0; i<adapter.getCount(); i++) {
                    if (adapter.getItem(i).getSorteado()==0)
                        mapa.put(adapter.getItem(i), i);
                }

                if (mapa.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ListActivity.this, "Nada para sortear", Toast.LENGTH_LONG).show();
                        }
                    });
                    Log.d(TAG, "Não há números...");
                    return;
                }

                Log.d(TAG, "Há "+mapa.size()+" números.");
                int idx = random.nextInt(mapa.size());
//                AlertDialog.Builder adb=new AlertDialog.Builder(ListActivity.this);
//                adb.setTitle("Número sorteado:");
                ArrayList<Convite> convites = new ArrayList<Convite>(mapa.keySet());
//                adb.setMessage(convites.get(idx).getNumero());
//                adb.setPositiveButton("OK", null);
//                adb.show();
                textView.setText(convites.get(idx).getNumero());
                db.execSQL("UPDATE convites SET sorteado = 1 WHERE numero = "+convites.get(idx).getNumero());
                int pos = mapa.get(convites.get(idx));
                adapter.getItem(pos).setSorteado(1);
                adapter.notifyDataSetChanged();

            }
        });


        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder adb=new AlertDialog.Builder(ListActivity.this);
                adb.setTitle("Reset?");
                adb.setMessage("Reiniciar sequência de sorteio?");
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        db.execSQL("UPDATE convites SET sorteado = 0");
                        for(int i=0; i<adapter.getCount(); i++)
                            adapter.getItem(i).setSorteado(0);
                        adapter.notifyDataSetChanged();
                        textView.setText("- -");
                    }});
                adb.show();
            }
        });
    }


}
