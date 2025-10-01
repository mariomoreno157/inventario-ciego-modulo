package com.example.modul_inventariociego

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.os.StrictMode
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.modul_inventariociego.Models.AdminSQLiteOpenHelper
import com.example.modul_inventariociego.Models.ProductoInfo
import com.example.modul_inventariociego.Models.RegistroInventarioMix
import es.dmoral.toasty.Toasty
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.text.SimpleDateFormat
import java.util.ArrayList
import java.util.Calendar
import java.util.Date
import kotlin.math.log

class MainActivity : AppCompatActivity() {
    var NameDevice = ""

    /**Varibles para Pantallas Flotantes**/
    var builder: android.app.AlertDialog.Builder? = null
    var alert: android.app.AlertDialog? = null

    var VersionApp: TextView?= null
    var indeterminateSwitch: ProgressBar? = null
    val bundle: Bundle get() = intent.extras!!

    var con: Connection? = null
    var conn: AdminSQLiteOpenHelper? = null

    /** Variables Globales Login**/
    var User_Login: String = ""
    var User_Id_Login: String = ""
    var almacenLoginId_Login: String = ""
    var lastLoginDate_Login: String = ""
    var IdEntrada_Entrada: String = ""

    var checkbandr : Boolean? = null

    var EdTxt_User: EditText?= null
    var Stg_User: String = ""
    var Txt_User : TextView?= null
    var EdTxt_Ubicacion: EditText?= null
    var Stg_Ubicacion: String = ""
    var Txt_Ubicacion : TextView?= null
    var EdTxt_CodConteo: EditText?= null
    var Stg_No_Conteo: String = ""
    var Txt_CodConteo : TextView?= null
    var EdTxt_Codigo: EditText?= null
    var EdTxt_Cantidad: EditText?= null
    var Stg_Cantidad: String = ""
    var Stg_Codigo: String = ""
    var Stg_Fecha: String = ""
    var Stg_CodigoProd: String = ""
    var Stg_DescripProd: String = ""

    var listaCodigos: ArrayList<RegistroInventarioMix> = ArrayList()

    /** Variables para mostrar la fecha y la hora del sistema**/
    var hora = 0
    var minuto = 0
    var segundo = 0
    var iniReloj: Thread? = null
    var r: Runnable? = null
    var isUpdate = false
    var sec: String? = null
    var min:String? = null
    var hor:String? = null
    var curTime: String? = null



    @SuppressLint("ClickableViewAccessibility", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContentView(R.layout.activity_listado_folios_embarque)

        /**Metodo de validacion de Equipos Validos GEM **/
        try {
            val modelo = Build.MODEL
            NameDevice = modelo
            Log.i("GEM"," - - NombreDevice: "+NameDevice.toString())
            if (NameDevice.contentEquals("MC3300x")){

                setContentView(R.layout.activity_inventario_mixto)

                // Toasty.success(applicationContext, "Tu si trabajas cool.", Toast.LENGTH_SHORT, true).show()
                Log.i("GEM"," - - Tu si trabajas cool.")
            }else if(NameDevice.contentEquals("EDA50K")){

                setContentView(R.layout.activity_inventario_mixto)

                //  Toasty.error(applicationContext, "Tu no deberias de trabajar.", Toast.LENGTH_SHORT, true).show()
                Log.i("GEM"," - - Tu no deberias de trabajar.")

                /*   Handler(Looper.getMainLooper()).postDelayed({
                       try {
                           Home()
                       }catch (E1 : Exception){
                           Log.e("-GEM-", "RESPONSE IS : "+E1.message.toString())
                           Toasty.error(this, "AtrasRecepcionEntradas: "+E1.message.toString(), Toast.LENGTH_LONG).show()
                       }
                   }, 500) */
            }else if(NameDevice.contentEquals("sdk_gphone64_x86_64")){

                setContentView(R.layout.activity_inventario_mixto)
            }else{
                setContentView(R.layout.activity_inventario_mixto)
            }
            hideSystemUI()

        }catch (e2:Exception){
            Log.e("GEM","validacion Equipo: "+e2.message.toString())
        }



        /**Metodo para optener la version de la app **/
        try {
            VersionApp = findViewById<EditText>(R.id.txt_version)

            val pInfo: PackageInfo = this.getPackageManager().getPackageInfo(this.getPackageName(), 0)
            val version = pInfo.versionName

            //VersionApp!!.text = BuildConfig.VERSION_NAME+" -Gemetytec" //asi se solicitaba antes
            VersionApp!!.text = "  Edición "+version +" – por Gemetytec"
        }catch (E1 :Exception){
            Log.e("VersionApp", " - "+E1.message.toString())
        }
        /****/
        val date = SimpleDateFormat("dd-MM-yyyy").format(Date())
        Stg_Fecha = date.toString()

        conn = AdminSQLiteOpenHelper(applicationContext, "WoMobilDB.db", null, 1)

        try {
            User_Login =  bundle!!.getString("L_User").toString()
            almacenLoginId_Login =  bundle!!.getString("L_IdAlmacen").toString()
            lastLoginDate_Login =  bundle!!.getString("L_LastDate").toString()
            User_Id_Login =  bundle!!.getString("L_IdUser").toString()
            println("datos bundle_Login= Usuario: $User_Login IdUser: $User_Id_Login  Id_Almacen: $almacenLoginId_Login Fecha: $lastLoginDate_Login")
        }catch (E1 : Exception){
            Log.e("Error_bundle_Envio: ",E1.message.toString())
        }

        indeterminateSwitch = findViewById(R.id.indeterminate_circular_indicator)
        EdTxt_Ubicacion = findViewById<EditText>(R.id.EdTxt_Ubicacion)
        EdTxt_User = findViewById<EditText>(R.id.EdTxt_User)
        Txt_User = findViewById<TextView>(R.id.txt_User)
        EdTxt_CodConteo = findViewById<EditText>(R.id.EdTxt_CodConteo)
        Txt_Ubicacion = findViewById<TextView>(R.id.txt_Ubicacion)
        Txt_CodConteo = findViewById<TextView>(R.id.txt_CodConteo)
        EdTxt_Codigo = findViewById<EditText>(R.id.EdTxt_CodProducto)
        EdTxt_Cantidad = findViewById<EditText>(R.id.EdTxt_cantidad)

        EdTxt_User!!.requestFocus()

        // en la línea de abajo obteniendo la vista actual.
        val view: View? = this.currentFocus
        // en la línea de abajo comprobando si la vista no es nula.
        if (view != null) {
            // en la línea de abajo estamos creando una variable
            // para el administrador de entrada e inicializándolo.
            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            // en la línea de abajo ocultando tu teclado.
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
        }

        r = RefreshClock()
        iniReloj = Thread(r)
        iniReloj!!.start()

        try {

            val release = java.lang.Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
            var codeName = "Unsupported"//below Jelly Bean
            if (release >= 4.1 && release < 4.4) {
                codeName = "Jelly Bean"
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                // Download/WoMobil/doc_export
                //com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db
                var ArchivoImportar1: File? = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/")
                if (!ArchivoImportar1!!.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar1")
                    ArchivoImportar1.mkdir()
                }

                val nuevaCarpeta = File(Environment.getExternalStorageDirectory(), "Android/data/com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(Environment.getExternalStorageDirectory().toString() +  "/Download/WoMobil/doc_export")
                DriverManager.println("doc_export:  $ArchivoExport")
                if (!ArchivoExport.exists()){
                    DriverManager.println("existe/download/ingunsa/doc_export--")
                    ArchivoExport.mkdir()
                }

            }
            else if (release < 5){

                // Download/WoMobil/doc_export
                //com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db
                codeName = "Kit Kat"
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)
                var ArchivoImportar1: File? = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/")
                if (!ArchivoImportar1!!.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar1")
                    ArchivoImportar1.mkdir()
                }
                val nuevaCarpeta = File(Environment.getExternalStorageDirectory(), "Android/data/com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(Environment.getExternalStorageDirectory().toString() +  "/Download/WoMobil/doc_export")
                DriverManager.println("doc_export:  $ArchivoExport")
                if (!ArchivoExport.exists()){
                    DriverManager.println("existe - - /download/WoMobil/doc_export --")
                    ArchivoExport.mkdir()
                }
            }
            else if (release < 6) {
                codeName = "Lollipop"
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)
                var ArchivoImportar1: File? = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/")
                if (!ArchivoImportar1!!.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar1")
                    ArchivoImportar1.mkdir()
                }
                val nuevaCarpeta = File(Environment.getExternalStorageDirectory(), "Android/data/com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(Environment.getExternalStorageDirectory().toString() +  "/Download/WoMobil/doc_export")
                DriverManager.println("doc_export:  $ArchivoExport")
                if (!ArchivoExport.exists()){
                    DriverManager.println("existe - - /download/WoMobil/doc_export --")
                    ArchivoExport.mkdir()
                }

            }
            else if (release < 7) {
                // Download/WoMobil/doc_export
                //com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db
                codeName = "Marshmallow"
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)
                var ArchivoImportar1: File? = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/")
                if (!ArchivoImportar1!!.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar1")
                    ArchivoImportar1.mkdir()
                }
                val nuevaCarpeta = File(Environment.getExternalStorageDirectory(), "Android/data/com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(Environment.getExternalStorageDirectory().toString() +  "/Download/WoMobil/doc_export")
                DriverManager.println("doc_export:  $ArchivoExport")
                if (!ArchivoExport.exists()){
                    DriverManager.println("existe - - /download/WoMobil/doc_export --")
                    ArchivoExport.mkdir()
                }
            }
            else if (release < 8) {
                codeName = "Nougat"
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)
                var ArchivoImportar1: File? = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/")
                if (!ArchivoImportar1!!.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar1")
                    ArchivoImportar1.mkdir()
                }
                val nuevaCarpeta = File(Environment.getExternalStorageDirectory(), "Android/data/com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(Environment.getExternalStorageDirectory().toString() +  "/Download/WoMobil/doc_export")
                DriverManager.println("doc_export:  $ArchivoExport")
                if (!ArchivoExport.exists()){
                    DriverManager.println("existe - - /download/WoMobil/doc_export --")
                    ArchivoExport.mkdir()
                }

            }

            else if (release < 9) {
                codeName = "Oreo"
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)
                var ArchivoImportar1: File? = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/")
                if (!ArchivoImportar1!!.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar1")
                    ArchivoImportar1.mkdir()
                }
                val nuevaCarpeta = File(Environment.getExternalStorageDirectory(), "Android/data/com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(Environment.getExternalStorageDirectory().toString() +  "/Download/WoMobil/doc_export")
                if (!ArchivoExport.exists()){
                    DriverManager.println("doc_export:-- $ArchivoExport")
                    ArchivoExport.mkdir()
                }

            }
            else if (release < 10) {
                codeName = "Pie"
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)
                var ArchivoImportar = File(getExternalFilesDir(null).toString(),  "/Download/WoMobil")
                if (!ArchivoImportar.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar")
                    ArchivoImportar.mkdirs()
                }
                val nuevaCarpeta = File(getExternalFilesDir(null).toString(), "/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(getExternalFilesDir(null).toString(),  "/Download/WoMobil/doc_export")
                if (!ArchivoExport.exists()){
                    DriverManager.println("doc_export:-- $ArchivoExport")
                    ArchivoExport.mkdir()
                }

            }
            else if (release >= 10) {
                // Download/WoMobil/doc_export
                //com.gemetytec.mmoreno.wobasic/files/WoMobilDB.db
                codeName = "Android "+(release.toInt())
                DriverManager.println(codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)
                var ArchivoImportar = File(getExternalFilesDir(null).toString(),  "/Download/WoMobil")
                if (!ArchivoImportar.exists()){
                    DriverManager.println("Folder:-- $ArchivoImportar")
                    ArchivoImportar.mkdirs()
                }
                val nuevaCarpeta = File(getExternalFilesDir(null).toString(), "/WoMobilDB.db")
                if (!nuevaCarpeta.exists()) {
                    DriverManager.println("database: $nuevaCarpeta")
                    nuevaCarpeta.mkdir()
                }
                var ArchivoExport = File(getExternalFilesDir(null).toString(),  "/Download/WoMobil/doc_export")
                if (!ArchivoExport.exists()){
                    DriverManager.println("doc_export:-- $ArchivoExport")
                    ArchivoExport.mkdir()
                }

            }//since API 29 no more candy code names



        }catch (Es1 : Exception){
            DriverManager.println("Es1: $Es1")
        }

        /** Metodo para el Enter en la caja de Usuario **/
        try {
            EdTxt_User!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                    // en la línea de abajo obteniendo la vista actual.
                    val view: View? = this.currentFocus
                    // en la línea de abajo comprobando si la vista no es nula.
                    if (view != null) {
                        // en la línea de abajo estamos creando una variable
                        // para el administrador de entrada e inicializándolo.
                        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        // en la línea de abajo ocultando tu teclado.
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                    }

                    //  indeterminateSwitch!!.visibility = View.VISIBLE
                    Stg_User = EdTxt_User!!.editableText.toString()

                    if (Stg_User.isNotEmpty() && Stg_User != null) {
                        // Si está marcado, bloqueamos la edición y pasamos el valor al TextView
                        Txt_User!!.text = Stg_User
                        EdTxt_User!!.visibility = View.INVISIBLE
                        Txt_User!!.visibility = View.VISIBLE

                        println("Ubicación check: "+Stg_User)
                        EdTxt_Ubicacion!!.requestFocus()

                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }

                    }else{
                        println("Ubicación: "+Stg_User)
                        EdTxt_User!!.requestFocus()

                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }
                    }


                    return@OnKeyListener true
                }
                false
            })
        }catch (E2 :Exception){
            Log.e("-EdTxt_Ubicacion-", "ERROR: "+E2.message.toString())
        }


        /** Metodo para el Enter en la caja de Ubicación **/
        try {
            EdTxt_Ubicacion!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                    // en la línea de abajo obteniendo la vista actual.
                    val view: View? = this.currentFocus
                    // en la línea de abajo comprobando si la vista no es nula.
                    if (view != null) {
                        // en la línea de abajo estamos creando una variable
                        // para el administrador de entrada e inicializándolo.
                        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        // en la línea de abajo ocultando tu teclado.
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                    }

                    //  indeterminateSwitch!!.visibility = View.VISIBLE
                    Stg_Ubicacion = EdTxt_Ubicacion!!.editableText.toString()

                    if (Stg_Ubicacion.isNotEmpty() && Stg_Ubicacion != null) {
                        // Si está marcado, bloqueamos la edición y pasamos el valor al TextView
                        Txt_Ubicacion!!.text = Stg_Ubicacion
                        EdTxt_Ubicacion!!.visibility = View.INVISIBLE
                        Txt_Ubicacion!!.visibility = View.VISIBLE

                        println("Ubicación check: "+Stg_Ubicacion)
                        EdTxt_CodConteo!!.requestFocus()

                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }

                    }else{
                        println("Ubicación: "+Stg_Ubicacion)
                        EdTxt_Ubicacion!!.requestFocus()

                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }
                    }


                    return@OnKeyListener true
                }
                false
            })
        }catch (E2 :Exception){
            Log.e("-EdTxt_Ubicacion-", "ERROR: "+E2.message.toString())
        }

        /** Metodo para el Enter en la caja de Ubicación **/
        try {
            EdTxt_CodConteo!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                    // en la línea de abajo obteniendo la vista actual.
                    val view: View? = this.currentFocus
                    // en la línea de abajo comprobando si la vista no es nula.
                    if (view != null) {
                        // en la línea de abajo estamos creando una variable
                        // para el administrador de entrada e inicializándolo.
                        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        // en la línea de abajo ocultando tu teclado.
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                    }

                    Stg_No_Conteo = EdTxt_CodConteo!!.editableText.toString()

                    if (Stg_No_Conteo.isNotEmpty() && Stg_No_Conteo != null) {
                        Txt_CodConteo!!.text = Stg_No_Conteo
                        EdTxt_CodConteo!!.visibility = View.INVISIBLE
                        Txt_CodConteo!!.visibility = View.VISIBLE

                        println("Ubicación check: "+Stg_No_Conteo)
                        EdTxt_Codigo!!.requestFocus()

                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }
                    }else{
                        println("Ubicación: "+Stg_No_Conteo)
                        EdTxt_CodConteo!!.requestFocus()

                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }
                    }


                    return@OnKeyListener true
                }
                false
            })
        }catch (E2 :Exception){
            Log.e("-EdTxt_Ubicacion-", "ERROR: "+E2.message.toString())
        }


        /** Metodo para el Enter en la caja de CodigoUnico **/
        try {
            EdTxt_Codigo!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {
                  //  indeterminateSwitch!!.visibility = View.VISIBLE

                    // en la línea de abajo obteniendo la vista actual.
                    val view: View? = this.currentFocus
                    // en la línea de abajo comprobando si la vista no es nula.
                    if (view != null) {
                        // en la línea de abajo estamos creando una variable
                        // para el administrador de entrada e inicializándolo.
                        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        // en la línea de abajo ocultando tu teclado.
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                    }

                    Stg_Codigo = EdTxt_Codigo!!.editableText.toString()
                    Stg_Ubicacion = EdTxt_Ubicacion!!.editableText.toString()
                    Stg_No_Conteo = EdTxt_CodConteo!!.editableText.toString()

                    println("CodigoUnico: "+Stg_Codigo)

                    if (Stg_No_Conteo.isNotEmpty() && Stg_No_Conteo != null) {

                    }else{
                        println("Ubicación: "+Stg_No_Conteo)
                        EdTxt_CodConteo!!.requestFocus()
                    }

                    if ((Stg_Ubicacion.isNotEmpty() && Stg_Ubicacion!=null) && (Stg_Codigo.isNotEmpty() && Stg_Codigo!=null) && (Stg_No_Conteo.isNotEmpty() && Stg_No_Conteo !=null) && (Stg_Fecha.isNotEmpty() && Stg_Fecha!=null) ) {

                        Txt_CodConteo!!.text = Stg_No_Conteo
                        EdTxt_CodConteo!!.visibility = View.INVISIBLE
                        Txt_CodConteo!!.visibility = View.VISIBLE


                        Txt_Ubicacion!!.text = EdTxt_Ubicacion!!.text.toString()
                        EdTxt_Ubicacion!!.visibility = View.INVISIBLE
                        Txt_Ubicacion!!.visibility = View.VISIBLE


                        Stg_Ubicacion = Txt_Ubicacion!!.text.toString()

                        checkbandr = true



                        /* if (productoInfo != null) {
                             println("Código: ${productoInfo.codigo}")
                             println("Descripción: ${productoInfo.descripcion}")

                             InsertCodeScan(Stg_Ubicacion,Stg_Codigo,Stg_No_Conteo,Stg_Fecha,User_Login,productoInfo.codigo,productoInfo.descripcion)

                         } else {
                             indeterminateSwitch?.visibility = View.INVISIBLE
                             println("No se encontró el producto.")
                         } */

                        /**
                         * Ejecuta la consulta de producto en segundo plano y actualiza la UI según el resultado.
                         */
                      /*  lifecycleScope.launch {
                            showProgress(true)

                            val producto = try {
                                obtenerProductoDesdeCodigoUnicoAsync(Stg_Codigo)
                            } catch (e: Exception) {
                                Log.e("Consulta", "Error al obtener producto: ${e.message}", e)
                                null
                            } finally {
                                showProgress(false)
                            }

                            if (producto != null) {
                                Log.i("Consulta", "Código: ${producto.codigo} - Descripción: ${producto.descripcion}")

                                Stg_CodigoProd = producto.codigo

                                Stg_DescripProd = producto.descripcion

                                EdTxt_Cantidad!!.requestFocus()


                            } else {
                                Toasty.error(this@MainActivity, "Producto no encontrado.", Toast.LENGTH_SHORT).show()

                                EdTxt_Codigo?.apply {
                                    text?.clear()
                                    requestFocus()
                                }
                            }
                        } */

                        EdTxt_Cantidad!!.requestFocus()

                    }else{

                        Toasty.error(this, "Ingrese datos faltantes.", Toast.LENGTH_SHORT).show()


                    }

                    return@OnKeyListener true
                }
                false
            })
        }catch (E2 :Exception){
            indeterminateSwitch!!.visibility = View.INVISIBLE
            Log.e("-EdTxt_NewUbicacion-", "ERROR: "+E2.message.toString())
        }

        // probar nuevo edit text para que registre la cantidad de productos ingresados en la tabla nueva

        /** Metodo para el Enter en la caja de Cantidad **/
        try {
            EdTxt_Cantidad!!.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP) {

                    // en la línea de abajo obteniendo la vista actual.
                    val view: View? = this.currentFocus
                    // en la línea de abajo comprobando si la vista no es nula.
                    if (view != null) {
                        // en la línea de abajo estamos creando una variable
                        // para el administrador de entrada e inicializándolo.
                        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        // en la línea de abajo ocultando tu teclado.
                        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                    }

                    Stg_Cantidad = EdTxt_Cantidad!!.editableText.toString()

                    if (Stg_Cantidad.isNotEmpty() && Stg_Cantidad != null) {

                        println("Cantidad check: "+Stg_Cantidad)

                        InsertCodeScan(Stg_Ubicacion,Stg_Codigo,Stg_Cantidad,Stg_No_Conteo,Stg_Fecha,Stg_User,"","")


                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }
                    }else{
                        println("Ubicación: "+Stg_Cantidad)
                        EdTxt_Cantidad!!.requestFocus()

                        // en la línea de abajo obteniendo la vista actual.
                        val view: View? = this.currentFocus
                        // en la línea de abajo comprobando si la vista no es nula.
                        if (view != null) {
                            // en la línea de abajo estamos creando una variable
                            // para el administrador de entrada e inicializándolo.
                            val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                            // en la línea de abajo ocultando tu teclado.
                            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
                        }
                    }


                    return@OnKeyListener true
                }
                false
            })
        }catch (E2 :Exception){
            Log.e("-EdTxt_Ubicacion-", "ERROR: "+E2.message.toString())
        }


        if(isConnected(this)){
            Toasty.success(this, "Existe conexión a Internet", Toast.LENGTH_SHORT).show()
            //   ListadoEmbarcarFolioApi(almacenLoginId_Login)

        }else{
            EncenderWifi()
        }




    }// final onCreate

    // inventoryciego(Ubicacion text,bardCode text,NoConteo text,FechaCaptura text,Usuario text,CodigoProducto text, Descripcion text

    fun InsertCodeScan(Ubicacion:String, bardCode:String, Cantidad:String,NoConteo:String, FechaCaptura:String, Usuario:String, CodigoProducto:String,Descripcion:String ){

        //   limpiarTablas("usuarios")


        try {
            val admin = AdminSQLiteOpenHelper(this, "WoMobilDB.db", null, 1)
            //val db = conn!!.readableDatabase
            val db: SQLiteDatabase = admin!!.getWritableDatabase()

            val registro = ContentValues()

            registro.put("Ubicacion", Ubicacion)
            registro.put("bardCode", bardCode)
            registro.put("Cantidad", Cantidad)
            registro.put("NoConteo", NoConteo)

            registro.put("FechaCaptura", FechaCaptura)
            registro.put("Usuario", Usuario)
           // registro.put("CodigoProducto", CodigoProducto)
           // registro.put("Descripcion", Descripcion)

            println("Lo que tiene la variable Registro Codigos es: $registro")

            // los inserto en la base de datos
            db.insert("inventorymix", null, registro)
            db.close()


            indeterminateSwitch!!.visibility = View.INVISIBLE

        } catch (EIO1: Exception) {
            indeterminateSwitch!!.visibility = View.INVISIBLE
            println("error ei01: $EIO1")
        } finally {

            EdTxt_Codigo?.apply {
                text?.clear()
                requestFocus()
            }
            EdTxt_Cantidad?.apply {
                text?.clear()
            }
            Toasty.success(this, "Se Registro Correctamente el producto.", Toast.LENGTH_LONG).show()
        }

    }


    /** Metodo para llenar el Spiner **/
    fun ConsultaDatosTabla(view: View) {
        try {
            if (listaCodigos == null) {
                listaCodigos = ArrayList()
            }
            val admin = AdminSQLiteOpenHelper(this, "WoMobilDB.db", null, 1)
            val db: SQLiteDatabase = admin.readableDatabase

            val cursor = db.rawQuery("SELECT * FROM inventorymix", null)
            listaCodigos!!.clear()

            while (cursor.moveToNext()) {
                val registro = RegistroInventarioMix(
                    cursor.getString(0), // Ubicacion
                    cursor.getString(1), // BarCode
                    cursor.getString(2), // Cantidad
                    cursor.getString(3), // NoConteo
                    cursor.getString(4), // FechaCaptura
                    cursor.getString(5) // Usuario
                )
                listaCodigos!!.add(registro)
            }
            cursor.close()
            db.close()

        } catch (EIO1: Exception) {
            indeterminateSwitch?.visibility = View.INVISIBLE
            Log.e("ConsultaDatosTabla", EIO1.message.toString())
        }

        try {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!listaCodigos.isNullOrEmpty()) {
                    val builder = StringBuilder()
                    for ((i, item) in listaCodigos!!.withIndex()) {
                        builder.append("[$i] Ubicación: ${item.ubicacion}\n")
                        builder.append("BarCode: ${item.barCode}\n")
                        builder.append("Cantidad: ${item.Cantidad}\n")
                        builder.append("No Conteo: ${item.noConteo}\n")
                        builder.append("Usuario: ${item.usuario}\n")
                        builder.append("---------------\n")
                    }

                    val textView = TextView(this)
                    textView.text = builder.toString()
                    textView.setPadding(30, 30, 30, 30)
                    textView.movementMethod = ScrollingMovementMethod()
                    textView.textSize = 14f

                    val dialog = android.app.AlertDialog.Builder(this)
                        .setTitle("Datos en la Tabla")
                        .setView(textView)
                        .setPositiveButton("Cerrar", null)
                        .setNegativeButton("Exportar", null) // Agregado botón Exportar
                        .create()

                    dialog.setOnShowListener {
                        val btnExportar = dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE)
                        btnExportar.setOnClickListener {
                            exportarDatos {
                                dialog.dismiss()
                            }
                        }
                    }

                    dialog.show()

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
            }, 300)

        } catch (EIO1: Exception) {
            indeterminateSwitch?.visibility = View.INVISIBLE
            Log.e("ConsultaDatosTabla1", EIO1.message.toString())
        }
    }

    fun exportarDatos(onFinish: () -> Unit) {
        // Aquí haces lo que necesites (por ejemplo, exportar a CSV)
        // ...
        Log.d("Exportar", "Exportación realizada correctamente")

        Exportardatos()

        // Llamas al callback para cerrar el diálogo
        onFinish()
    }



    /**
     * Obtiene la información del producto desde la base de datos SQL Server
     * a partir del código único proporcionado.
     *
     * @param codigoUnico El código del producto escaneado o ingresado por el usuario.
     * @return Un objeto ProductoInfo con el código y la descripción del producto si se encuentra, o null si no existe.
     */
    suspend fun obtenerProductoDesdeCodigoUnicoAsync(codigoUnico: String): ProductoInfo? {
        var productoInfo: ProductoInfo? = null

        return withContext(Dispatchers.IO) {
            try {
                // Se extraen los primeros 4 caracteres del código, que identifican el ID del producto
                val productoId = codigoUnico.substring(0, 4).toInt()

                // Se establece la conexión con SQL Server
                val con = conectarSQLServer("192.168.0.103", "WO_Almacen", "g3m3tyt3c.90", "sa")

                if (con == null) {
                    // Si la conexión falla, mostrar error en el hilo principal (UI)
                    withContext(Dispatchers.Main) {
                        showProgress(false)
                        Toasty.error(this@MainActivity, "No se pudo realizar la conexión a la base de datos.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Consulta SQL para obtener el producto desde su ID
                    val query = "SELECT Codigo, Descripcion FROM cat_Productos WHERE Id = '$productoId'"
                    val resultSet = con.createStatement().executeQuery(query)

                    // Si hay resultado, se crea el objeto ProductoInfo
                    if (resultSet.next()) {
                        val codigo = resultSet.getString("Codigo")
                        val descripcion = resultSet.getString("Descripcion")
                        productoInfo = ProductoInfo(codigo, descripcion)
                    }
                }

            } catch (e: Exception) {
                // Log de error técnico
                Log.e("obtenerProductoDesdeCodigoUnico", "error: ${e.message}")
                e.printStackTrace()

                // Ocultar progress bar en la UI si hay error
                withContext(Dispatchers.Main) {
                    showProgress(false)
                }
            } finally {
                // Siempre ocultar el progress bar al finalizar
                withContext(Dispatchers.Main) {
                    showProgress(false)
                }
            }

            productoInfo
        }
    }

    fun conectarSQLServer(ip_server: String?,name_data: String?,pass_user: String?,user_name: String?): Connection? {
        var direccion:String? = ip_server
        var basedatos:String? = name_data
        var usuario:String? = user_name
        var contrasena: String? = pass_user

        try {
            val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance()
            con = DriverManager.getConnection("jdbc:jtds:sqlserver://$direccion;databaseName=$basedatos;user=$usuario;password=$contrasena;integratedSecurity=true;")
            println("-conn1- : "+con.toString()+" --- "+direccion+" - "+basedatos+" - "+usuario+" - "+contrasena)
            //  indeterminateSwitch!!.visibility = View.INVISIBLE
            Log.i("conectarSQLServer", "Conexión establecida correctamente Servidor")


            this.runOnUiThread(Runnable {
                indeterminateSwitch?.visibility = View.INVISIBLE
                Toasty.success(this@MainActivity, "Conexión establecida correctamente Servidor!!", Toast.LENGTH_LONG).show()
            })


        } catch (se: SQLException) {
            //   indeterminateSwitch!!.visibility = View.INVISIBLE
            Log.e("SQL_Connection", "Error SQL: ${se.message}")
            ValidaConexion(con)

            this.runOnUiThread(Runnable {
                indeterminateSwitch?.visibility = View.INVISIBLE
            })
        } catch (e: ClassNotFoundException) {

            Log.e("SQL_Connection", "Driver JDBC no encontrado: ${e.message}")
            this.runOnUiThread(Runnable {
                indeterminateSwitch?.visibility = View.INVISIBLE
            })
        } catch (e: Exception) {
            Log.e("SQL_Connection", "Error general: ${e.message}")
            this.runOnUiThread(Runnable {
                indeterminateSwitch?.visibility = View.INVISIBLE
            })
        }
        return con
    }


    /**
     * Muestra u oculta el ProgressBar (indeterminateSwitch) de forma segura desde el hilo principal.
     *
     * @param visible true para mostrarlo, false para ocultarlo.
     */
    private fun showProgress(visible: Boolean) {
        indeterminateSwitch?.visibility = if (visible) View.VISIBLE else View.INVISIBLE
    }


    private fun ValidaConexion(conn: Connection?) {
        try {
            if(conn == null){

                Toasty.error(this, "Los datos de la conexion son invalidos.", Toast.LENGTH_LONG).show()
            }
        }catch (EIO:Exception){}
    }




    fun isConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_CELLULAR")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_WIFI")
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    Log.i("Internet", "NetworkCapabilities.TRANSPORT_ETHERNET")
                    return true
                }
            }
        }
        return false
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // Android 10 o inferior
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    )
        }
    }

    fun EncenderWifi(){
        try {
            builder = android.app.AlertDialog.Builder(this)

            builder!!.setTitle("Conexion Internet")
            builder!!.setMessage("Su conexion a internet no existe \n por favor habilite su wifi\n")

            builder!!.setPositiveButton("ENCENDER") { dialog, which ->
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS ))
                Toasty.info(this, "Encendiendo Wifi", Toast.LENGTH_LONG).show()
                alert!!.cancel()

            }

            builder!!.setCancelable(false)
                .setNeutralButton("CERRAR",
                    DialogInterface.OnClickListener { dialog, id ->
                        try {
                            Toasty.error(this, "No encendido", Toast.LENGTH_LONG).show()

                        } catch (e: java.lang.Exception) {
                            println(" ErrorVend: $e")
                        }

                    })

            alert = builder!!.create()
            // alert!!.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            alert!!.show()
            alert!!.setCanceledOnTouchOutside(false) // limitar el salir de pantalla al tocar fuera de ella
            alert!!.getWindow()!!.setGravity(Gravity.BOTTOM) //mover pantalla flotante

        }catch (E:Exception){
            Log.e("EncenderWifi",E.message.toString())
        }
    }

    fun Exportardatos(){
        try {

            var CarpetaIngusa: File? = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil")

            val QueryCapacidad = "InventarioConteo"

            val date = SimpleDateFormat("dd-MM-yyyy").format(Date())
            Stg_Fecha = date.toString()

            println("Entro a impresos")
            var isCreate = false
            if (!CarpetaIngusa!!.exists()) {
                //isCreate = CarpetaIngusa!!.mkdir()
                println("no existe, crear")
            }

            var CarpetIngusa: File?
            val archivoExportador: String

            /**Verificador de Nivel de API para almacenamiento**/
            val release = java.lang.Double.parseDouble(java.lang.String(Build.VERSION.RELEASE).replaceAll("(\\d+[.]\\d+)(.*)", "$1"))
            var codeName = "Unsupported"//below Jelly Bean
            if (release >= 4.1 && release < 4.4) {
                codeName = "Jelly Bean"
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")
                // println("El valor de la variable es $fila")
                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")


                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario
                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }

                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()

            }
            else if (release < 5){
                codeName = "Kit Kat"
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")
                // println("El valor de la variable es $fila")
                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")

                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario

                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()
            }
            else if (release < 6) {
                codeName = "Lollipop"
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")
                // println("El valor de la variable es $fila")
                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")

                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario

                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()
            }
            else if (release < 7) {
                codeName = "Marshmallow"
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")

                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")

                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario

                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()
            }
            else if (release < 8) {
                codeName = "Nougat"
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")

                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")

                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario

                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()

            }
            else if (release < 9) {
                codeName = "Oreo"
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(Environment.getExternalStorageDirectory().toString() + "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")

                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")

                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario

                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()

            }
            else if (release < 10) {
                codeName = "Pie"
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(getExternalFilesDir(null).toString(), "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")

                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")

                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario

                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()
            }
            else if (release >= 10) {
                codeName = "Android "+(release.toInt())
                println( codeName + " v" + release + ", API Level: " + Build.VERSION.SDK_INT)

                CarpetIngusa = File(getExternalFilesDir(null).toString(), "/Download/WoMobil/doc_export")
                archivoExportador = CarpetIngusa.toString() + "/AEXP $QueryCapacidad " +Stg_Fecha+"_$curTime.csv"
                val fileWriter = FileWriter(archivoExportador)

                println("El valor de la variable es $QueryCapacidad")

                println("CarpetIngusa $CarpetIngusa")
                println("archivoExportador $archivoExportador")

                if (!listaCodigos.isNullOrEmpty()) {
                    // Formar documento de los datos
                    for ((i, item) in listaCodigos!!.withIndex()) {

                        fileWriter.append(i.toString())//clave
                        fileWriter.append(",")
                        fileWriter.append(item.ubicacion)//Capacidad
                        fileWriter.append(",")
                        fileWriter.append(item.barCode)//BarCode
                        fileWriter.append(",")
                        fileWriter.append(item.Cantidad)//Cantidad
                        fileWriter.append(",")
                        fileWriter.append(item.noConteo)//NoConteo
                        fileWriter.append(",")
                        fileWriter.append(item.fechaCaptura)//FechaCaptura
                        fileWriter.append(",")
                        fileWriter.append(item.usuario)//Usuario

                        fileWriter.append("\n")

                    }

                } else {
                    Toasty.warning(this, "No hay datos en la tabla.", Toast.LENGTH_SHORT).show()
                }
                fileWriter.close()
                Toasty.success(this,"SE CREO EXITOSAMENTE EL ARCHIVO CSV", Toast.LENGTH_LONG).show()

            }//since API 29 no more candy code names

        }catch (E:Exception){
            Log.e("Exportardatos",E.message.toString())
        }
    }

    fun limpiar(view: View){
        try {

            EdTxt_Ubicacion?.text!!.clear()
            EdTxt_CodConteo?.text!!.clear()
            EdTxt_User?.text!!.clear()
            Txt_Ubicacion?.text = ""
            Txt_CodConteo?.text = ""
            Txt_User?.text = ""
            Stg_User = ""
            Stg_Ubicacion = ""
            Stg_No_Conteo = ""
            Stg_Codigo = ""

            EdTxt_User!!.visibility = View.VISIBLE
            Txt_User!!.visibility = View.INVISIBLE
            Txt_Ubicacion!!.visibility = View.INVISIBLE
            EdTxt_Ubicacion!!.visibility = View.VISIBLE
            Txt_CodConteo!!.visibility = View.INVISIBLE
            EdTxt_CodConteo!!.visibility = View.VISIBLE

            EdTxt_Ubicacion!!.requestFocus()

            // en la línea de abajo obteniendo la vista actual.
            val view: View? = this.currentFocus
            // en la línea de abajo comprobando si la vista no es nula.
            if (view != null) {
                // en la línea de abajo estamos creando una variable
                // para el administrador de entrada e inicializándolo.
                val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                // en la línea de abajo ocultando tu teclado.
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0)
            }


        }catch (E1 : Exception){
            Log.e("limpiar",E1.message.toString())
            Toasty.error(this, "limpiar: "+E1.message.toString(), Toast.LENGTH_LONG).show()
        }
    }

    /** Funciones para mostrar la Fecha y la hora del sistema ***/
    /**Funcion que inicia el reloj que se muestra en el login. **/
    private fun initClock() {
        runOnUiThread {
            try {

                if (isUpdate) {
                    settingNewClock()
                } else {
                    updateTime()
                }
                curTime = hor + hora + min + minuto + sec + segundo
                // Hora?.setText(curTime)

            } catch (e: Exception) {
            }
        }
    }
    /**Funcion que hace la actualizacion del reloj**/
    internal inner class RefreshClock : Runnable {
        // @Override
        override fun run() {
            while (!Thread.currentThread().isInterrupted) {
                try {
                    initClock()
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    Thread.currentThread().interrupt()
                } catch (e: Exception) {
                }

            }
        }
    }

    /** Funcion para la actualzacion de la fecha **/
    private fun updateTime() {

        val c = Calendar.getInstance()
        hora = c.get(Calendar.HOUR_OF_DAY)
        minuto = c.get(Calendar.MINUTE)
        segundo = c.get(Calendar.SECOND)
        setZeroClock()

    }
    /** Funcion para la sincronicacion de la hora **/
    private fun setZeroClock() {
        if ((hora >= 0) and (hora <= 9)) {
            hor = "0"
        } else {
            hor = ""
        }

        if ((minuto >= 0) and (minuto <= 9)) {
            min = ":0"
        } else {
            min = ":"
        }

        if ((segundo >= 0) and (segundo <= 9)) {
            sec = ":0"

        } else {
            sec = ":"
        }
    }
    /**Funcion para refrehs el metodo de la hora **/
    private fun settingNewClock() {
        segundo += 1

        setZeroClock()

        if ((segundo >= 0) and (segundo <= 59)) {

        } else {
            segundo = 0
            minuto += 1
        }
        if ((minuto >= 0) and (minuto <= 59)) {

        } else {
            minuto = 0
            hora += 1
        }
        if ((hora >= 0) and (hora <= 24)) {

        } else {
            hora = 0
        }

    }




    fun atras(view: View){

        try {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.addCategory(Intent.CATEGORY_HOME)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)


        }catch (E1 : Exception){
            Log.e("Home",E1.message.toString())
            Toasty.error(this, "Home: "+E1.message.toString(), Toast.LENGTH_LONG).show()
        }

    }




}