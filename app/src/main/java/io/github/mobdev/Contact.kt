package io.github.mobdev

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    var email: String? = null
)
