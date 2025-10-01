package com.example.modul_inventariociego.Models

data class RegistroInventarioMix(
    val ubicacion: String,
    val barCode: String,
    val Cantidad: String,
    val noConteo: String,
    val fechaCaptura: String,
    val usuario: String
)