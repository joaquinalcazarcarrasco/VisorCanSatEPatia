package com.joaquin.visorcansatepatia;

import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.TimerTask;

/**
 * @author Joaquín Alcázar Carrasco
 *
 * Clase que hereda de AsyncTask para realizar tareas en segundo plano. En este caso realizará consultas a servidor web
 */
public class Conexion extends AsyncTask<String, String, String> {

    /**
     * Método que define qué realizará en segundo plano
     *
     * @param strings url de conexión, progreso y callback
     * @return los datos en formato String o null en caso de que haya algún error
     */
    @Override
    protected String doInBackground(String... strings) {

        //Definimos variables
        HttpURLConnection httpURLConnection = null;//Objeto clase HttpUrlConnection para establecer la conexión
        URL url = null;//Objeto Clase URL que contendrá la url a la que nos conectaremos

        try {

            //Almacenamos la url a la que nos conectaremos
            url = new URL(strings[0]);

        } catch (MalformedURLException e) {

            Log.e("ERROR", "URL mal formada");
            //mostramos error en caso de que la url de esta exepción por url mal formada
            e.printStackTrace();
        }


        try {

            //Abrimos conexión hacia esa url y la almacenamos en httpurlconnection
            httpURLConnection = (HttpURLConnection) url.openConnection();

            //Nos conectamos a dicha conexión abierta
            httpURLConnection.connect();

            //Almaceno el código de respuesta de la conexión
            int codigoRespuesta = httpURLConnection.getResponseCode();

            //Evaluamos el código de respuesta de la conexión
            if(codigoRespuesta == HttpURLConnection.HTTP_OK){

                //Si fue bien, creamos Instancia de InputStream y le pasamos datos de entrada de la conexión
                InputStream is = new BufferedInputStream(httpURLConnection.getInputStream());

                //Creamos buffer de lectura y le pasamos el canal de entreda
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String linea = "";//String para almacenar cada línea leída
                StringBuffer buffer = new StringBuffer();//Buffer para agregar todas las líneas que reciba

                //Vamos leyendo en bucle mientras haya líneas de datos
                while((linea = reader.readLine()) != null){

                    //Agrega al buffer cada línea hasta que sea null
                    buffer.append(linea);

                }

                //Devolvemos el contenido de buffer en formato string
                return buffer.toString();

            }

        } catch (IOException e) {

            Log.e("ERROR", "Entrada o salida");
            //capturamos si hay expeción de entrada/salida
            e.printStackTrace();
        }

        Log.e("ERROR", "Se retorna null");
        //se retorna null en caso de que algo vaya mal
        return null;
    }
}
