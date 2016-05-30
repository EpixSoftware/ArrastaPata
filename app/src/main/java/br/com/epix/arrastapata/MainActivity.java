package br.com.epix.arrastapata;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import java.util.ArrayList;
import java.util.List;

import br.com.epix.arrastapata.db.ConvitesDbHelper;
import me.dm7.barcodescanner.zbar.BarcodeFormat;
import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

public class MainActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler {
    private ZBarScannerView mScannerView;
    private static String TAG = MainActivity.class.getCanonicalName();
    ToggleButton toggle;
    TextView numero;
    private boolean mState = false;
    ViewGroup frame;
    SQLiteDatabase db;
    ConvitesDbHelper dbHelper;


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.lista_convites:
                Intent intent = new Intent(this, ListActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreate(final Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        frame = (ViewGroup) findViewById(R.id.frame);
        numero = (TextView) findViewById(R.id.numero);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view


        List<BarcodeFormat> formatos = new ArrayList<>();
        formatos.add(BarcodeFormat.QRCODE);
        mScannerView.setFormats(formatos);
        frame.addView(mScannerView);

        dbHelper = new ConvitesDbHelper(this);
        db = dbHelper.getWritableDatabase();

        toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setTextOff("Capturar código");
        toggle.setTextOn("Encerrar captura");
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    numero.setTextColor(Color.rgb(255, 255, 255));
                    numero.setText("- - -");
                    mScannerView.setVisibility(View.VISIBLE);
                    mScannerView.setResultHandler(MainActivity.this); // Register ourselves as a handler for scan results.
                    mScannerView.startCamera();          // Start camera on resume

                } else {
                    mScannerView.setVisibility(View.INVISIBLE);
                    mScannerView.stopCamera();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mState = false;
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();
        toggle.setChecked(false);
    }

    @Override
    public void handleResult(Result rawResult) {
        Log.v(TAG, rawResult.getContents()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)
        // If you would like to resume scanning, call this method below:
        //mScannerView.resumeCameraPreview(this);
        toggle.setChecked(false);
        String codigo = rawResult.getContents().substring(0,3);
        String hex = rawResult.getContents().substring(3,6);
        String md5Hex = new String(Hex.encodeHex(DigestUtils.md5(codigo+"chelonia!")));
        String calcHex = md5Hex.substring(md5Hex.length()-3, md5Hex.length());

        Log.d(TAG, hex+"/"+calcHex);

        if (calcHex.compareTo(hex)!=0) {
            numero.setTextColor(Color.rgb(255, 0, 0));
            numero.setText("ERRO");
            Log.d(TAG, "FALHOU MD5");
            return;
        }

        ContentValues values = new ContentValues();
        values.put("numero", codigo);
        values.put("sorteado", false);
        long rowId;
        try {
            rowId = db.insertOrThrow(ConvitesDbHelper.TABLE_NAME, null, values);
            numero.setTextColor(Color.rgb(32,255,32));
        } catch (SQLiteConstraintException ex) {
            numero.setTextColor(Color.rgb(255,32,32));
            Toast.makeText(MainActivity.this, "Convite já cadastrado", Toast.LENGTH_LONG).show();
        }




        numero.setText(codigo);

    }
}
