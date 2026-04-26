package io.github.mobdev

import android.annotation.SuppressLint
import android.content.Context
import android.provider.ContactsContract

class ContactsRepository(private val context: Context) {

    @SuppressLint("Range")
    fun fetchContacts(): List<Contact> {
        val contactsMap = mutableMapOf<String, Contact>()
        val resolver = context.contentResolver

        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null, null, null, null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex =
                cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idIndex).takeIf { idIndex >= 0 } ?: continue
                val name = cursor.getString(nameIndex).takeIf { nameIndex >= 0 } ?: "Без имени"
                val phone = cursor.getString(phoneIndex).takeIf { phoneIndex >= 0 } ?: ""

                if (!contactsMap.containsKey(id)) {
                    contactsMap[id] = Contact(id = id, name = name, phoneNumber = phone)
                }
            }
        }

        resolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            null, null, null, null
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID)
            val emailIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS)

            while (cursor.moveToNext()) {
                val id = cursor.getString(idIndex).takeIf { idIndex >= 0 } ?: continue
                val email = cursor.getString(emailIndex).takeIf { emailIndex >= 0 } ?: continue
                contactsMap[id]?.email = email
            }
        }

        return contactsMap.values.sortedBy { it.name }
    }
}
