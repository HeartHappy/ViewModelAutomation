package com.hearthappy.viewmodelautomation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.compiler.config.defaultConfig
import com.hearthappy.compiler.viewmodel.LoginViewModel
import com.hearthappy.ktorexpand.code.network.RequestState
import com.hearthappy.viewmodelautomation.databinding.ActivityLoginBinding
import com.hearthappy.viewmodelautomation.model.request.LoginBody
import com.hearthappy.viewmodelautomation.model.request.ReLogin
import com.hearthappy.viewmodelautomation.model.response.ResLogin
import com.hearthappy.viewmodelautomation.ui.base.BaseActivity

@AndroidViewModel

@BindStateFlow("login", ReLogin::class, ResLogin::class)
class LoginActivity: BaseActivity() {

    private lateinit var viewBinding: ActivityLoginBinding

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        
        viewBinding.apply {
            viewModelListener()
            btnLogin.setOnClickListener {
                val email = etEmail.text.toString()
                if (email.isNotEmpty()) {
                    viewModel.login(LoginBody(email, "123456"))
                } else toast("邮箱不能为空")
            }

            btnGoToRegister.setOnClickListener { startActivity(RegisterActivity::class.java) }

            btnGoToOther.setOnClickListener { startActivity(MainActivity::class.java) }
        }
    }

    private fun ActivityLoginBinding.viewModelListener() {
        lifecycleScope.launchWhenCreated {
            viewModel.loginStateFlow.collect {
                when (it) {
                    is RequestState.LOADING -> progress.show()
                    is RequestState.SUCCEED -> tvResult.showSucceedMsg(it.body.toString(), progress)
                    is RequestState.FAILED -> tvResult.showFailedMsg(it, progress)
                    is RequestState.Throwable -> tvResult.showThrowableMsg(it, progress)
                    else -> progress.hide()
                }
            }
        }
    }
}