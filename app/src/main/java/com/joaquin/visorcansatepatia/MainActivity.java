package com.joaquin.visorcansatepatia;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

/**
 * @author Joaquín Alcázar Carrasco
 *
 * Clase principal. Se carga al iniciar la aplicación
 */
public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    //Atributos de clase
    TextView txtvlongitud, txtvLatitud, txtvTemperatura, txtvPresion, txtvHumedad, txtvCuentaAtras;

    //Para mapa
    private GoogleMap mMap;
    private Marker marcador;
    double lat = 40.440780034097564;
    double lon = -3.711073901762065;

    //para cuenta atrás
    private Date fechaLanzamiento, fechaActual;
    private Calendar calendar;
    private long tiempoRestante;

    /**
     *
     * Método que carga la actividad y la vista.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        //Obtenemos handle del fragmento insertado en la vista para el mapa
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //vinculación con controles de la vista
        txtvlongitud = (TextView) findViewById(R.id.txtvLongitud);
        txtvLatitud = (TextView) findViewById(R.id.txtvLatitud);
        txtvTemperatura = (TextView) findViewById(R.id.txtvTemperatura);
        txtvPresion= (TextView) findViewById(R.id.txtvPresion);
        txtvHumedad = (TextView) findViewById(R.id.txtvHumedad);
        txtvCuentaAtras = (TextView) findViewById(R.id.txtvCuentaAtras);

        /**
         *
         * TODO: Fecha de lanzamiento. COLOCAR LA FECHA REQUERIDA
         *
         */
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2022);
        calendar.set(Calendar.MONTH, Calendar.MAY);
        calendar.set(Calendar.DAY_OF_MONTH, 13);
        calendar.set(Calendar.HOUR_OF_DAY, 14);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        //Inicializamos objetos Date
        fechaLanzamiento = calendar.getTime();
        fechaActual = new Date();

        //Diferencia entre fechas
        tiempoRestante = fechaLanzamiento.getTime() - fechaActual.getTime();


        /**
         *
         * CUENTA ATRÁS. Se pasa la diferencia entre la fecha actual y la fecha del lanzamiento en milisegundos
         * Se le indica también el intervalo en milisegundos.
         *
         */
        new CountDownTimer(tiempoRestante, 1000){


            /**
             *
             * Método en el que se indica lo que se ejecuta cada intervalo definido en el constructor de CountDownTimer
             *
             * @param l - el tiempo en milisegundos recibido en el constructor de CountDownTimer (tiempoRestante)
             */
            @Override
            public void onTick(long l) {
                /**
                 *
                 * Se usa post para poder modificar controles de la actividad principal desde un hilo secundario
                 *
                 */
                txtvCuentaAtras.post(new Runnable() {
                    @Override
                    public void run() {

                        /**
                         * Se obtiene la equivalencia de la diferencia en segundos, minutos, horas y días
                         *
                         * Explicación:
                         * segundos = (milisegundos / 1 segundo en milisegundos) mod 60 segundos de un minuto
                         */
                        int dias = (int) ((l / 1000) / 86400);
                        int horas = (int) (((l / 1000) - (dias * 86400)) / 3600);
                        int minutos = (int) (((l / 1000) - (dias * 86400) - (horas * 3600)) / 60);
                        int segundos = (int) ((l / 1000) % 60);

                        /**
                         *
                         * TODO: Formateamos el resultado y almacenamos en un String // Formatear al gusto
                         *
                         */
                        String countDownText = String.format("%2d días %02dh %02d' %02d\" ", dias, horas, minutos, segundos);

                        //lo mostramos en el TextView de la vista
                        txtvCuentaAtras.setText(countDownText);
                    }
                });
            }

            /**
             *
             * Qué se realiza cuando se acaba la cuenta atrás
             *
             */
            @Override
            public void onFinish() {
                /**
                 *
                 * Se usa post para poder modificar controles de la actividad principal desde un hilo secundario
                 *
                 */
                txtvCuentaAtras.post(new Runnable() {
                    @Override
                    public void run() {

                        /**
                         * Se actualiza texto de la cuenta atrás en la vista
                         * TODO: Colocar mensaje al acabar la cuenta atrás                         *
                         */

                        txtvCuentaAtras.setText("Despegue...");

                    }
                });
            }
        }.start();


        /**
         *
         * Creo un  hilo secundario para realizar la conexión al servidor web dónde está la info que quiero pintar en mi app
         *
         */
        new Thread(new Runnable() {

            /**
             *
             * Método run que contiene lo que se ejecuta cuando se inicia el hilo con el método start()
             * Se creará un objeto de tipo Timer para que cada segundo realice una acción con TimerTask
             * indicándole el tiempo que se tomará en empezar y el intervalo en el que repetirá la acción
             *
             */
            @Override
            public void run() {

                //Objeto de tipo Timer
                Timer timer = new Timer();

               // LecturaJSON lecturaJson = new LecturaJSON(respuestaConexion);

               // timer.schedule(lecturaJson, 1000,500);

                /**
                 *
                 * Nueva tarea de tipo TimerTask
                 *
                 */
                timer.schedule(new TimerTask() {

                    /**
                     * Se establecerá conexión con el servidor web, se leerá archivo JSON y se modificarán diferentes controles en la vista
                     */
                    @Override
                    public void run() {

                        //Creo instancia de la clase que creamos para realizar la conexión asíncrona en segundo plano
                        Conexion conexion = new Conexion();

                        //Ejecutamos la conexión pasando la url como parámetro
                        String respuestaConexion = null;//necesito una url que contenga los datos en JSON

                        try {

                            /**
                             *
                             * TODO: sustituir por la url correcta.
                             *
                             */
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


                                /**
                                 * Instancia de JSONObject para almacenar el registro que se lee en la url a la que nos hemos conectado
                                 */
                                JSONObject jsonObject = null;

                                jsonObject = new JSONObject(respuestaConexion);

                                /**
                                 *
                                 * Recuperamos los datos y los almacenamos en variables
                                 * TODO: Modificar por los datos correctos cuando se tengan latitud, longitud, etc.
                                 *
                                 */
                                String realTime = jsonObject.getString("realtime");
                                Double longitud = Double.parseDouble(jsonObject.getString("location"));
                                Double latitud = Double.parseDouble(jsonObject.getString("height"));
                                Double temperatura = Double.parseDouble(jsonObject.getString("temperature"));
                                Double presion = Double.parseDouble(jsonObject.getString("pressure"));
                                Double humedad = Double.parseDouble(jsonObject.getString("humidity"));

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {

                                }
                                /**
                                 *
                                 * Igual que con Post, uso este método para poder modificar controles del
                                 * hilo principal desde hilo secundario
                                 *
                                 */
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {

                                        /**
                                         *
                                         * Mostramos los datos en los controles de la vista
                                         *
                                         * TODO: colocarlos según necesidades reales
                                         *
                                         */
                                        txtvlongitud.setText("" +  longitud);
                                        txtvLatitud.setText("" + latitud);
                                        txtvTemperatura.setText("" + temperatura);
                                        txtvPresion.setText("" + presion);
                                        txtvHumedad.setText("" + humedad);
                                        /**
                                         *
                                         * Para agregar las coordenadas que se recojan desde la conexión al servidor web
                                         * TODO: descomentar y pasar como argumentos las variables correctas
                                         *
                                         */
                                        //agregarMarcador(temperatura, longitud);
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

    /**
     *
     * Método de la interfaz OnMapReadyCallback - Contiene lo que realizará cuando se cargue el mapa en la interfaz
     *
     * TODO: Ahora muestra una localización estática
     *
     * @param googleMap
     */
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        //Inicializamos mMap
        mMap = googleMap;

        //mMap.addMarker(new MarkerOptions()
                //.position(new LatLng(0, 0))
                //.title("Marker"));

        /**
         *
         * Pruebas para mostrar la ubicación del dispositvo
         * Obtiene ubicación por primera vez y llama al listener cada 15 segundos
         *
         * TODO: Eliminar. No es necesario para esta app finalmente
         *
         */
        //obtenerUbicacion();

        //Agregar marcador con las coordeandas por defecto. Luego se actualizarán llamando a este moismo método desde el timer
        agregarMarcador(lat, lon);

    }

    /**
     *
     * método que agregará un nuevo marcador al mapa con la latitud y longitud que le pasemos como parámetros
     *
     * @param lat - variable de tipo long con la latitud de la localización sobre la que mostrar marcador en el mapa
     * @param lon - variable de tipo long con la longitud de la localización sobre la que mostrar marcador en el mapa
     */
    //Método para agregar marcador al mapa
    public void agregarMarcador(double lat, double lon) {

        //creamos objeto LatLng con coordenadas pasadas como parámetro
        LatLng latLng = new LatLng(lat, lon);

        //Creamos objeto de tipo CameraUpdate para mover la cámaara a las coordendas especificadas.
        CameraUpdate ubicacion = CameraUpdateFactory.newLatLngZoom(latLng, 16);

        //Para formatear número decimal en dos decimales
        final DecimalFormat df = new DecimalFormat();
        df.setMaximumFractionDigits(2);

        //si marcador es diferente de null lo borramos
        if (marcador != null) marcador.remove();
        marcador = mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Posición actual - Lat: " + df.format(latLng.latitude) + " / Lon: " + df.format(latLng.longitude))
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ico_ubicacion48)));

        //Animamos el movimiento de cámara hacia la ubicación obtenida
        mMap.animateCamera(ubicacion);

    }


    /**
     *
     * A partir de aquí, son métodos que implementan el pintar la ubicación del dispositivo
     * en el mapa. Son pruebas que he estado haciendo. No son necesarios para la app
     *
     * TODO: ELIMINAR A PARTIR DE AQUÍ
     *
     */


    //Para obtener la localización de nuestro dispositivo
    private void actualizarLocalizacion(Location localizacion) {

        if (localizacion != null) {
            lat = localizacion.getLatitude();
            lon = localizacion.getLongitude();
            agregarMarcador(lat, lon);
        }
    }

    //Objeto de tipo LocationListener que estará a la escucha de cualquier cambio de localizcaión en el dispositvo
    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            //llamamos al método actualizarLocalizcion para actualizar los datos
            actualizarLocalizacion(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    };

    //Método que obtiene localizacion del dispositivo llamando al LocationListener
    private void obtenerUbicacion() {

        //Se necesitan añadir verificación de permisos de usuario
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //Se crea LocationManager que recoge el servicio del sistema de localización
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Se crea objeto de tipo Location al que le pasamos lo que devuelve el método getLasKnownLocation sobre el objeto de locationManager
        Location localizacion = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        //Se llama al método actualizarlocalizacoin
        actualizarLocalizacion(localizacion);

        //Sobre el LocationManager se hace la petición de actualizar la localización cada 15 segundos a través del LocationListener creado
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15000, 0, locationListener);

    }
}