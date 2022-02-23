package com.joaquin.visorcansatepatia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * @author Joaquín Alcázar Carrasco
 *
 * Clase principal. Se carga al iniciar la aplicación
 */
public class MainActivity extends AppCompatActivity {

    //Atributos de clase
    TextView txtvlongitud, txtvLatitud, txtvTemperatura, txtvPresion, txtvHumedad, txtvCuentaAtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        //vinculación con controles de la vista
        txtvlongitud = (TextView) findViewById(R.id.txtvLongitud);
        txtvLatitud = (TextView) findViewById(R.id.txtvLatitud);
        txtvTemperatura = (TextView) findViewById(R.id.txtvTemperatura);
        txtvPresion= (TextView) findViewById(R.id.txtvPresion);
        txtvHumedad = (TextView) findViewById(R.id.txtvHumedad);
        txtvCuentaAtras = (TextView) findViewById(R.id.txtvCuentaAtras);

        //Array para almacenar los controles
        //TextView[] controles = {txtvCuentaAtras, txtvlongitud, txtvLatitud, txtvTemperatura, txtvPresion, txtvHumedad};


        //cuenta atrás --- ver cómo tomar fecha para calcular la cuenta atrás en días, horas y minutos
        new CountDownTimer(30000, 1000){


            @Override
            public void onTick(long l) {
                txtvCuentaAtras.post(new Runnable() {
                    @Override
                    public void run() {
                        txtvCuentaAtras.setText("" + l/1000);
                    }
                });
            }

            @Override
            public void onFinish() {
                txtvCuentaAtras.post(new Runnable() {
                    @Override
                    public void run() {

                        txtvCuentaAtras.setText("Despegue...");

                    }
                });
            }
        }.start();


        //Creo un  hilo secundario para realizar la conexión al servidor web dónde está la info que quiero pintar en mi app
        new Thread(new Runnable() {
            @Override
            public void run() {

                Timer timer = new Timer();


               // LecturaJSON lecturaJson = new LecturaJSON(respuestaConexion);

               // timer.schedule(lecturaJson, 1000,500);

                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {

                        //Creo instancia de la clase que creamos para realizar la conexión asíncrona en segundo plano
                        Conexion conexion = new Conexion();

                        //Ejecutamos la conexión pasando la url como parámetro
                        String respuestaConexion = null;//necesito una url que contenga los datos en JSON

                        try {

                            respuestaConexion = conexion.execute("https://joaquinalcazarcarrasco.com/pruebaMoviles/registroActual.php").get();
                            //Si la respuesta es diferente de null
                            if(respuestaConexion!=null) {

                                /////leemos el código JSON

                                /*
                                 * Creamos Array para almacenar lo que leamos en el JSON. Le pasamos como parámetro la respuesta que
                                 * obtuvimos al conectarnos con nuestro objeto de ConexionAsincrona
                                 **/
                                //JSONArray jsonArray = new JSONArray(respuestaConexion);

                                /*
                                 * Instancia de JSONObject para ir obteniendo, en este caso, el objeto en la posciión 0 del array.
                                 * Se podría usar un mapa para obtenerlos todos, en el caso de que hubiese más registros
                                 **/
                                //JSONObject jsonObject = jsonArray.getJSONObject(0);


                                /*
                                 * Instancia de JSONObject para almacenar el registro que se lee en la url a la que nos hemos conectado
                                 */
                                JSONObject jsonObject = null;

                                jsonObject = new JSONObject(respuestaConexion);

                                //Recuperamos los datos y los almacenamos en variables
                                String cuentraAtras = jsonObject.getString("realtime");
                                Double longitud = Double.parseDouble(jsonObject.getString("location"));
                                Double latitud = Double.parseDouble(jsonObject.getString("height"));
                                Double temperatura = Double.parseDouble(jsonObject.getString("temperature"));
                                Double presion = Double.parseDouble(jsonObject.getString("pressure"));
                                Double humedad = Double.parseDouble(jsonObject.getString("humidity"));

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        //Los mostramos en la vista
                                        //txtvCuentaAtras.setText(cuentraAtras);
                                        txtvlongitud.setText("" +  longitud);
                                        txtvLatitud.setText("" + latitud);
                                        txtvTemperatura.setText("" + temperatura);
                                        txtvPresion.setText("" + presion);
                                        txtvHumedad.setText("" + humedad);
                                    }
                                });

                            }

                        } catch (ExecutionException e) {
                            e.printStackTrace();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }catch (JSONException e) {

                                Log.e("ERROR", "Fallo al leer el JSON");
                                e.printStackTrace();
                            }

                        }

                    }, 10, 1000);

            }

        }).start();



    }
}