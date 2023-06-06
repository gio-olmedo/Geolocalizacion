package com.example.geolocalizacin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.CountDownTimer;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class Llamadas {

    Activity activity;

    Context context;

    TextView tvTitulo;

    int estadoLlamada;

    boolean alerta = false;

    String telefono;

    boolean llamadaIniciada = false;

    private FusedLocationProviderClient fusedLocationClient;

    double latitud;

    double longitud;

    public Llamadas(Activity activity, Context context,String telefono) {
        this.activity = activity;
        this.context = context;
        this.telefono = telefono;
    }

    private void cargarLlamadas() {
        PhoneCallListener phoneListener = new PhoneCallListener();

        TelephonyManager telephonyManager = (TelephonyManager) activity.getSystemService(context.TELEPHONY_SERVICE);

        telephonyManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
        print("Seguridad Iniciada");
    }

    private void monitor() {

    }

    private void bloquearPantalla() {

        new CountDownTimer(1000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                DevicePolicyManager policyManager = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
                ComponentName adminReceiver = new ComponentName(context, AdminReceiver.class);
                boolean admin = policyManager.isAdminActive(adminReceiver);

                if (admin) {
                    policyManager.lockNow();
                } else {
                    Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiver);
                    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "BIND_DEVICE_ADMIN is required for USES_POLICY_FORCE_LOCK.");
                    activity.startActivity(intent);
                }
                if (alerta) {
                    bloquearPantalla();
                }
            }
        }.start();


    }

    @SuppressLint("MissingPermission")
    private String ubicacion() {

        String url = "https://maps.google.com/?q=";

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(activity, new OnSuccessListener<Location>() {
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
        print("La llamada iniciara en 6 segundos");
        new CountDownTimer(9000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                llamar();
                mandarBloqueo();
            }
        }.start();
    }

    private void mandarBloqueo() {
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        int permiso = ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
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
            Toast.makeText(context, "No tienes permiso de llamada", Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(activity,
                    new String[]{Manifest.permission.CALL_PHONE}, 255);
        }
    }

    private void mandarSMS() {

        print("Mensajeeeeee");
        /*try {
            //asigna ambos valores
            String messageToSend =  ubicacion();
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
                SmsManager.getDefault().sendTextMessage(NUMBER, null, mensaje, null, null);

            }

            Toast.makeText(context, "Mensaje Enviado!", Toast.LENGTH_LONG).show();

        } catch ( Exception e) {

            Toast.makeText(context, "Fallo el envio!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }*/
    }

    private void mandarAlerta() {
        Toast.makeText(context, "Se lanzará la alerta", Toast.LENGTH_SHORT).show();
        alerta = true;
        repetir();
        mandarLlamada();
    }

    private void llamar() {

        String inicio = "tel: " + telefono;
        Intent i = new Intent(Intent.ACTION_CALL);
        i.setData(Uri.parse(inicio));
        activity.startActivity(i);

    }

    private void repetir() {
        mandarSMS();
        CountDownTimer timer = new CountDownTimer(300000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (estadoLlamada == TelephonyManager.CALL_STATE_OFFHOOK) {
                    alerta = false;
                }
                if (alerta) {
                    repetir();
                }
            }
        }.start();
    }

    private void print(String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    private void espera() {
        Toast.makeText(context, "El contacto de confianza está llamando", Toast.LENGTH_SHORT).show();

        new CountDownTimer(18000, 1000) {
            @Override
            public void onTick(long l) {

            }

            @Override
            public void onFinish() {
                if (estadoLlamada != TelephonyManager.CALL_STATE_OFFHOOK) {
                    mandarAlerta();
                }
            }
        }.start();
    }

    public class PhoneCallListener extends PhoneStateListener {

        @Override
        public void onCallStateChanged(int state, String incomingNumber) {

            String LOG_TAG = "llamada detectada";
            estadoLlamada = state;
            Log.i(LOG_TAG, "El estado de la llamada es: " + estadoLlamada);

            if (TelephonyManager.CALL_STATE_RINGING == state) {
                // phone ringing
                Log.i(LOG_TAG, "RINGING, number: " + incomingNumber);
                print("Llamada entrante de: " + incomingNumber);

                System.out.println("numero de confianza: " + telefono);
                System.out.println("Numero entrante: " + incomingNumber);
                if (incomingNumber.equals(telefono)) {
                    espera();

                }

            }

            if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
                // active
                Log.i(LOG_TAG, "Llamada Contestada");
                Log.i(LOG_TAG, "el numero es: " + incomingNumber);

                //isPhoneCalling = true;
            }

            if (TelephonyManager.CALL_STATE_IDLE == state) {
                // run when class initial and phone call ended, need detect flag
                // from CALL_STATE_OFFHOOK
                Log.i(LOG_TAG, "IDLE number");
                Log.i(LOG_TAG, "el numero es: " + incomingNumber);


            }
        }
    }
}
