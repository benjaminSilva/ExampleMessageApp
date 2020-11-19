package com.bsoftwares.chatexample.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bsoftwares.chatexample.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_register.*
import java.util.*

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class RegisterFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txt_login.setOnClickListener {
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }
        btn_register.setOnClickListener {
            regUser(et_email_register.text.toString(), et_password_register.text.toString(),et_user_register.text.toString())
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

    var selectPhotoURI : Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Activity.RESULT_OK == resultCode && data != null)
            when (requestCode) {
                0 -> {
                    selectPhotoURI = data.data!!
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver,selectPhotoURI)
                    civ_photo_selected.setImageBitmap(bitmap)
                    btn_selectPhoto.alpha = 0f
                }
            }
    }

    private fun regUser(email: String, password: String,user:String) {
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

    private fun uploadImageToFireStorage(user:String) {
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectPhotoURI!!).addOnSuccessListener {
            saveUserToFirebaseDatabase(it.toString(),user)
        }
    }

    private fun saveUserToFirebaseDatabase(profileImageUrl: String,user:String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.setValue(User(uid,user,profileImageUrl)).addOnSuccessListener {
            findNavController().navigate(R.id.action_SecondFragment_to_homeActivity)
            requireActivity().finish()
        }
    }

    class User(val uid: String,val username: String,val profileImageUrl:String)

}