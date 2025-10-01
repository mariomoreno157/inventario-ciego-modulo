package com.example.modul_inventariociego.Models

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AdminSQLiteOpenHelper(context: Context?, s: String, nothing: Nothing?, i: Int) : SQLiteOpenHelper(context, BASE_NOMBRE, null, BASE_VERSION) {

    companion object {
        private val BASE_NOMBRE = "WoMobilDB.db"
        private val BASE_VERSION = 1

    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE ConexionDB (ssid text,ssidPass text,servidor text,nameDB text,UserServer text, UserPass text,Clave text)")

        db.execSQL("CREATE TABLE comprarticulo(codigoArtic text, nombreArtic text, precioArtic text, descuentoArtic text, cantidadArtic text, totalcostoArtic text, idCabecero text,folioCabecero text)")

        db.execSQL("CREATE TABLE cabeceropedidos (codigo text, nombre text)")

        db.execSQL("CREATE TABLE clientes(Clave text,nombreCliente text,NoTelefono text,Email text)")

        db.execSQL("CREATE TABLE inventoryciego(Ubicacion text,bardCode text,NoConteo text,FechaCaptura text,Usuario text,CodigoProducto text, Descripcion text)")

        db.execSQL("CREATE TABLE inventorymix(Ubicacion text,bardCode text,Cantidad text,NoConteo text,FechaCaptura text,Usuario text,CodigoProducto text, Descripcion text)")

        db.execSQL("CREATE TABLE domicilios(Clave text,nombreCliente text,Direccion text,NoDirecc text,Tipo text,Telefono text,Correo text)")

    }

    override fun onUpgrade(db: SQLiteDatabase, i: Int, i1: Int) {

    }

    fun borrarRegistros(tabla: String, db: SQLiteDatabase) {
        if(tabla == "usuarios"){
            db.execSQL("DROP TABLE IF EXISTS usuarios")
            db.execSQL("CREATE TABLE usuarios(codigo text,password text,opciones text,status text)")
        }else if(tabla == "clientes"){
            db.execSQL("DROP TABLE IF EXISTS clientes")
            db.execSQL("CREATE TABLE clientes(codigo text, nombre text)")
        }else if(tabla == "cilindros"){
            db.execSQL("DROP TABLE IF EXISTS cilindros")
            db.execSQL("CREATE TABLE cilindros(capacidad text)")
        }else if(tabla == "cilindrosenci"){
            db.execSQL("DROP TABLE IF EXISTS cilindrosenci")
            db.execSQL("CREATE TABLE cilindrosenci(capcil text,totimp text)")
        }else if(tabla == "cilindrosence"){
            db.execSQL("DROP TABLE IF EXISTS cilindrosence")
            db.execSQL("CREATE TABLE cilindrosence(lote text,cliente text,numcont text,capcil text,numemb text,totemb text,sttemb text)")
        }else if(tabla == "cilindrosence"){
            db.execSQL("DROP TABLE IF EXISTS cilindrosence")
            db.execSQL("CREATE TABLE cilindrosence(lote text,cliente text,numcont text,capcil text,numemb text,totemb text,sttemb text)")
        }else if(tabla == "cilindrosence"){
            db.execSQL("DROP TABLE IF EXISTS cilindrosdet")
            db.execSQL("CREATE TABLE cilindrosdet(id int primary key,capcil text,nif text,fechaimp text,lote text, cliente text,sttemb text,sttexp text,imp text)")
        }

    }

    fun BorrarImpresos(tabla: String, db: SQLiteDatabase){
        db.execSQL("DROP TABLE IF EXISTS cilindrosdet")
        db.execSQL("CREATE TABLE cilindrosdet(id int primary key,capcil text,nif text,fechaimp text,lote text, cliente text,sttemb text,sttexp text,imp text)")
    }

    fun VaciarTablasBD(db: SQLiteDatabase){
        /**Tabla de Usuarios**/
        db.execSQL("DROP TABLE IF EXISTS usuarios")
        db.execSQL("CREATE TABLE usuarios(codigo text,password text,opciones text,status text)")
        /**Tabla de Clientes**/
        db.execSQL("DROP TABLE IF EXISTS clientes")
        db.execSQL("CREATE TABLE clientes(codigo text, nombre text)")
        /**Tabla de Cilindros**/
        db.execSQL("DROP TABLE IF EXISTS cilindros")
        db.execSQL("CREATE TABLE cilindros(capacidad text)")
        /**Tabla de Cilindrosenci**/
        db.execSQL("DROP TABLE IF EXISTS cilindrosenci")
        db.execSQL("CREATE TABLE cilindrosenci(capcil text,totimp text)")
        /**Tabla de Cilindorsence**/
        db.execSQL("DROP TABLE IF EXISTS cilindrosence")
        db.execSQL("CREATE TABLE cilindrosence(lote text,cliente text,numcont text,capcil text,numemb text,totemb text,sttemb text)")
        /**Tabla de Cilindrosdet**/
        db.execSQL("DROP TABLE IF EXISTS cilindrosdet")
        db.execSQL("CREATE TABLE cilindrosdet(id int primary key,capcil text,nif text,fechaimp text,lote text, cliente text,sttemb text,sttexp text,imp text)")
    }



}