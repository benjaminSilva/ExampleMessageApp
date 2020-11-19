package com.bsoftwares.chatexample.ui

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bsoftwares.chatexample.viewModel.LoginFragmentViewModel
import com.bsoftwares.chatexample.R
import com.bsoftwares.chatexample.utils.createString
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.android.synthetic.main.fragment_login.*

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class LoginFragment : Fragment() {

    private lateinit var auth : FirebaseAuth

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        auth = Firebase.auth
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_login.setOnClickListener {
            val email = createString(editTextTextPersonName)
            val password = createString(editTextTextPassword)
            if (email == "")
                til_email_login.error = getString(R.string.error_email_required)

            if (password == "")
                til_password_login.error = getString(R.string.error_password_required)
            if (email.isNotEmpty() || password.isNotEmpty())
                signIn(email, password)
        }

        txt_register.setOnClickListener {
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment)
        }

        editTextTextPassword.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til_password_login.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
        editTextTextPersonName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til_email_login.error = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        })
    }

    private fun signIn(email : String, password: String) {
        auth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
            findNavController().navigate(R.id.action_FirstFragment_to_homeActivity)
            requireActivity().finish()
        }.addOnFailureListener { error ->
            when (error.message){
                getString(R.string.error_email_login) -> til_email_login.error = getString(R.string.error_email_not_registered)
                getString(R.string.error_password) -> til_password_login.error = getString(R.string.error_wrong_password)
            }
        }
    }
}