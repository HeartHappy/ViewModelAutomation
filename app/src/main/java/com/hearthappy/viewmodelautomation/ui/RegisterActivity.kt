package com.hearthappy.viewmodelautomation.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.hearthappy.annotations.AndroidViewModel
import com.hearthappy.annotations.BindStateFlow
import com.hearthappy.compiler.viewmodel.RegisterViewModel
import com.hearthappy.ktorexpand.code.network.RequestState
import com.hearthappy.viewmodelautomation.databinding.ActivityRegisterBinding
import com.hearthappy.viewmodelautomation.model.request.ReRegister
import com.hearthappy.viewmodelautomation.model.request.ReSendVerificationCode
import com.hearthappy.viewmodelautomation.model.response.ResRegister
import com.hearthappy.viewmodelautomation.model.response.ResVerificationCode
import com.hearthappy.viewmodelautomation.ui.base.BaseActivity


@AndroidViewModel

@BindStateFlow("getVerificationCode", ReSendVerificationCode::class, ResVerificationCode::class)
@BindStateFlow("register", ReRegister::class, ResRegister::class)
class RegisterActivity: BaseActivity() {

    private lateinit var viewBinding: ActivityRegisterBinding

    private val viewModel by viewModels<RegisterViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        viewBinding.apply {

            viewModelListener()

            btnGetVerification.setOnClickListener {
                val email = etEmail.text.toString()
                if (email.isNotEmpty()) {
                    viewModel.getVerificationCode(ReSendVerificationCode(email))
                } else toast("邮箱不能为空")
            }

            btnRegister.setOnClickListener {
                val email = etEmail.text.toString()
                val verificationCode = etVerification.text.toString()
                if (email.isNotEmpty() && verificationCode.isNotEmpty()) {
                    viewModel.register(ReRegister(email, verificationCode, "123456"))
                } else toast("邮箱或验证码不能为空！")
            }

            btnGoToLogin.setOnClickListener { startActivity(LoginActivity::class.java) }
        }
    }

    private fun ActivityRegisterBinding.viewModelListener() {
        lifecycleScope.launchWhenCreated {
            viewModel.registerStateFlow.collect {
                when (it) {
                    is RequestState.LOADING -> progress.show()
                    is RequestState.SUCCEED -> tvResult.showSucceedMsg(it.body.toString(), progress)
                    is RequestState.FAILED -> tvResult.showFailedMsg(it, progress)
                    is RequestState.Throwable -> tvResult.showThrowableMsg(it, progress)
                    else -> progress.hide()
                }
            }
        }
        lifecycleScope.launchWhenCreated {
            viewModel.getVerificationCodeStateFlow.collect {
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


