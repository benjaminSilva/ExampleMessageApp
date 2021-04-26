package com.bsoftwares.chatexample.ui

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.services.MyFirebaseMessagingService
import com.bsoftwares.chatexample.utils.ImageResizer
import com.bsoftwares.chatexample.utils.getImageUri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*

class RegisterFragment : Fragment() {

    val permissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ActivityCompat.requestPermissions(requireActivity(),permissions,0)

        txt_login.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        btn_register.setOnClickListener {
            regUser(
                et_email_register.text.toString(),
                et_password_register.text.toString(),
                et_user_register.text.toString()
            )
        }
        btn_selectPhoto.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }
        et_email_register.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til_email_register.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        et_password_register.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, total: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, total: Int, p2: Int, p3: Int) {
                if (total >= 5)
                    til_password.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })


    }

    var selectPhotoURI: Uri? = null
    var bitmapReduced: Bitmap? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK == resultCode && data != null)
            when (requestCode) {
                0 -> {
                    selectPhotoURI = data.data!!
                    val bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().contentResolver,
                        selectPhotoURI
                    )
                    bitmapReduced = ImageResizer.reduceBitmapSize(bitmap, 100000)
                    civ_photo_selected.setImageBitmap(bitmapReduced)
                    btn_selectPhoto.alpha = 0f
                }
            }
    }

    private fun regUser(email: String, password: String, user: String) {
        if (email.isEmpty() || password.isEmpty() || et_user_register.text.toString().isEmpty())
            return
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(
            email,
            password
        ).addOnCompleteListener {
            if (!it.isSuccessful) {
                when (val errorMessage = it.exception!!.message) {
                    getString(R.string.error_email) -> til_email_register.error = errorMessage
                    getString(R.string.error_characters) -> til_password.error = errorMessage
                }
                return@addOnCompleteListener
            }
            uploadImageToFireStorage(user)
        }
    }

    private fun uploadImageToFireStorage(user: String) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(getImageUri(requireContext(), bitmapReduced!!)!!).addOnSuccessListener {
            ref.downloadUrl.addOnSuccessListener {
                saveUserToFirebaseDatabase(it.toString(), user)
            }
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String,user:String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.setValue(User(uid,user,profileImageUrl,FirebaseMessaging.getInstance().token.toString())).addOnSuccessListener {
            findNavController().navigate(R.id.action_SecondFragment_to_homeActivity)
            requireActivity().finish()
        }
    }

    class User(
        val uid: String,
        val username: String,
        val profileImageUrl: String,
        val token: String
    )

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

    }

}