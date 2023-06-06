package com.example.geolocalizacin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Inicio extends AppCompatActivity {

    Button btnregistrar;
    EditText etnombre, etparentesco, ettelefono;

    Context con;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        //Contexto de la apliación
        con = this;

        //Archivo preference para almacenar información del contacto de confianza
        SharedPreferences sharedPreferences= getSharedPreferences("ArchGuardar",con.MODE_PRIVATE);

        //Verifica si hay algun registro guardado.
        SharedPreferences cond = getPreferences(con.MODE_PRIVATE);
        String condi = cond.getString("telefono", "NoHay");

        if(!condi.equals("NoHay")){
            //En caso de que exista, obtiene el nombre y lo manda como Extra a la sig Actividad
            Intent sig= new Intent(Inicio.this, MainActivity.class );
            sig.putExtra("DATA_NAME_KEY",condi);
            //Dirije directamente a la nueva actividad
            startActivity(sig);
            finish();
        }else{
            //En caso opuesto, informa al usuario
            Toast.makeText(getApplicationContext(), "No existe registro", Toast.LENGTH_LONG).show();

        }

        etnombre = (EditText) findViewById(R.id.etnombre);
        etparentesco = (EditText) findViewById(R.id.etparentesco);
        ettelefono = (EditText) findViewById(R.id.etnumero);
        btnregistrar = (Button) findViewById(R.id.btnregistrar);

        btnregistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Obtiene la informacion de los formulario y la guarda en el archivo preference
                SharedPreferences shared= getPreferences(con.MODE_PRIVATE);
                SharedPreferences.Editor edit= shared.edit();

                edit.putString("nombre", etnombre.getText().toString());
                edit.putString("parentesco", etparentesco.getText().toString());
                edit.putString("telefono", ettelefono.getText().toString());
                edit.commit();

                SharedPreferences share = getPreferences(con.MODE_PRIVATE);

                Toast.makeText(getApplicationContext(), "El contacto : "+ etnombre.getText().toString() + " se registró correctamente", Toast.LENGTH_LONG).show();

                Intent sig= new Intent(Inicio.this, MainActivity.class );
                sig.putExtra("DATA_NAME_KEY",ettelefono.getText().toString());
                startActivity(sig);

                finish();

            }
        });



    }

    private void print(String s) {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }
}