package com.example.geolocalizacin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.*;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;


public class MainActivity extends AppCompatActivity {

    TextView tvTitulo;

    ImageView ivCandado;

    Context con;

    int estadoLlamada;

    boolean alerta = false;

    boolean telefonoB = false;

    String telefono;

    String telefonoEntrante="";

    boolean llamadaIniciada = false;

    private FusedLocationProviderClient fusedLocationClient;

    double latitud;

    double longitud;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivCandado = (ImageView) findViewById(R.id.ivCandado);
        tvTitulo = (TextView) findViewById(R.id.tvTitulo);

        //Se obtiene la variable del numero.
        telefono = getIntent().getExtras().getString("DATA_NAME_KEY").trim();
        con = getApplicationContext();

        ;


        //Solicitar permisos para ejecución
        permisos();
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminReceiver = new ComponentName(con, AdminReceiver.class);
        boolean admin = policyManager.isAdminActive(adminReceiver);
        if (!admin) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "BIND_DEVICE_ADMIN is required for USES_POLICY_FORCE_LOCK.");
            startActivity(intent);
        }

        //Metodo para cargar el metodo de control de llamadas
        cargarLlamadas();
        //Metodo para cargar la ubicación
        ubicacion();

    }


    private void bloquearPantalla() {
        //Cuando el dispositivo active la llamada, cada segundo se va a bloquear hasta finalizar la llamada
        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }
            @Override
            public void onFinish() {

                DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName adminReceiver = new ComponentName(con, AdminReceiver.class);
                boolean admin = policyManager.isAdminActive(adminReceiver);

                if (admin) {
                    //Metodo para bloquear pantalla
                    policyManager.lockNow();
                } else {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "BIND_DEVICE_ADMIN is required for USES_POLICY_FORCE_LOCK.");
                    startActivity(intent);
                }
                if (telefonoB) {
                    cargarLlamadas();
                    bloquearPantalla();
                }
            }
        }.start();


    }

    @SuppressLint("MissingPermission")
    private String ubicacion() {
        //Obtiene la ubicación y la manda por URL
        String url = "https://maps.google.com/?q=";

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            latitud = location.getLatitude();
                            longitud = location.getLongitude();
                        }
                    }
                });

        url = url + latitud + "," + longitud;
        System.out.println(url);
        return url;
    }

    private void mandarLlamada() {
        //Contador a 6 segundos para mandar a llamada una vez finalizada la entrante
        print("La llamada iniciara en 6 segundos");
        new CountDownTimer(9000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                //Finalizados los 6 segundos, entra la llamada y bloquea la pantalla
                llamar();
                mandarBloqueo();
            }
        }.start();
    }

    private void mandarBloqueo() {
        //Pasados 3 segundos, definitivamente bloquea la pantalla
        new CountDownTimer(3000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                bloquearPantalla();
            }
        }.start();
    }

    private void permisos() {
        //Metodo que solicita permisos de ubicacion y mensajes.
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        int permiso = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        if (permiso != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "No tienes permiso de llamada", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.CALL_PHONE}, 255);
        }
    }

    private void cargarLlamadas() {
        //Constructor para el metodo de llamadas
        PhoneCallListener phoneListener = new PhoneCallListener();

        TelephonyManager telephonyManager = (TelephonyManager) this
                .getSystemService(con.TELEPHONY_SERVICE);

        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        print("Seguridad Iniciada");

        //Hasta que la llamada esté colgada y el numero sea de confianza, desactivara la llamada
        if((estadoLlamada == TelephonyManager.CALL_STATE_IDLE) && (telefonoEntrante.equals(telefono))){
            telefonoB = false;
        }

    }

    private void mandarSMS() {
        //Metodo para mandar el mensaje
        try {
            String messageToSend="";
            //Obtiene el valor de la ubicacion y lo concatena con un mensaje de auxilio
            for (int i = 0;i<3;i++) messageToSend =  ubicacion();
            String messageToSend2 = "¡Ayuda estoy en peligro! ";
            String mensaje = messageToSend2 + messageToSend;
            System.out.println(mensaje);

            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                        Manifest.permission.SEND_SMS)) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS}, 1);
                } else {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.SEND_SMS}, 1);
                }
            }else {
                SmsManager.getDefault().sendTextMessage(telefono, null, mensaje, null, null);

            }

            Toast.makeText(getApplicationContext(), "Mensaje Enviado!", Toast.LENGTH_LONG).show();

        } catch ( Exception e) {

            Toast.makeText(getApplicationContext(), "Fallo el envio!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }


    private void mandarAlerta() {
        //Cuando se detecta la llamada sin contestar, se lanza la alerta activando las variables
        Toast.makeText(getApplicationContext(), "Se lanzará la alerta", Toast.LENGTH_SHORT).show();
        alerta = true;
        tvTitulo.setText("Alerta: ACTIVADA");
        tvTitulo.setTextColor(Color.parseColor("#ff0036"));
        telefonoB = true;
        repetir(); //Manda el mensaje de forma recurrente al contacto de confianza
        mandarLlamada(); //Metodo para llamar al contacto de confianza
    }

    private void llamar() {
        //Actividad que manda a llamar
        String inicio = "tel: " + telefono;
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse(inicio));
        startActivity(i);

    }

    private void repetir() {
        //Cada 5 min manda mensaje al usuario con la ubicación
        mandarSMS();
        CountDownTimer timer = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                //Si al cabo de los 5 min, el usuario regresa la llamada, se desactiva la alarma
                if ((estadoLlamada == TelephonyManager.CALL_STATE_OFFHOOK)
                        && telefono.equals(telefonoEntrante)) {
                    alerta = false;
                    tvTitulo.setText("Alerta: DESACTIVADA");
                    tvTitulo.setTextColor(Color.parseColor("#99ff33"));
                }
                if (alerta) {
                    repetir();
                }
            }
        }.start();
    }

    private void print(String msg) {
        //Metodo para imprimir toasts
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private  void espera() {
        //Metodo para medir si el contacto de confianza llama sin contestar al menos 18 seg
        Toast.makeText(con.getApplicationContext(), "El contacto de confianza está llamando", Toast.LENGTH_SHORT).show();

        new CountDownTimer(18000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                //Si al final de llamadas, este no responde, manda la alerta
                if (estadoLlamada != TelephonyManager.CALL_STATE_OFFHOOK) {
                    mandarAlerta();
                }
            }
        }.start();
    }

    public class PhoneCallListener extends PhoneStateListener {
        //Metodo para monitorear llamadas
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            String LOG_TAG = "llamada detectada";
            estadoLlamada = state;

            Log.i(LOG_TAG, "El estado de la llamada es: " + estadoLlamada);
            telefonoEntrante = incomingNumber;

            //Desicion cuando el estado de la llamada es entrante
            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                print("Llamada entrante de: "+incomingNumber);

                System.out.println("numero de confianza: "+ telefono);
                System.out.println("Numero entrante: "+incomingNumber);
                if (incomingNumber.equals(telefono)) {
                    espera();
                }

            }

            //Desición cuando la llamada está ocurriendo
            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "Llamada Contestada");
                Log.i(LOG_TAG, "el numero es: " + incomingNumber);

                //isPhoneCalling = true;
            }

            //Desicion cuando el estado de llamada es inactivo
            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended, need detect flag
                // from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE number");
                Log.i(LOG_TAG, "el numero es: " + incomingNumber);


            }
        }

    }
}