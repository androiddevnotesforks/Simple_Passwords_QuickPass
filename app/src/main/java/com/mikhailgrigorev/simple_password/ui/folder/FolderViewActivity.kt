package com.mikhailgrigorev.simple_password.ui.folder

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.mikhailgrigorev.simple_password.common.base.MyBaseActivity
import com.mikhailgrigorev.simple_password.common.utils.Utils
import com.mikhailgrigorev.simple_password.data.dbo.PasswordCard
import com.mikhailgrigorev.simple_password.databinding.ActivityFolderViewBinding
import com.mikhailgrigorev.simple_password.di.component.DaggerApplicationComponent
import com.mikhailgrigorev.simple_password.di.modules.RoomModule
import com.mikhailgrigorev.simple_password.di.modules.viewModel.injectViewModel
import com.mikhailgrigorev.simple_password.ui.main_activity.adapters.PasswordAdapter
import com.mikhailgrigorev.simple_password.ui.password_card.view.PasswordViewActivity
import javax.inject.Inject

class FolderViewActivity : MyBaseActivity() {
    private lateinit var binding: ActivityFolderViewBinding
    private lateinit var passwordCards: List<PasswordCard>
    private lateinit var viewModel: FolderViewModel

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFolderViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DaggerApplicationComponent.builder()
                .roomModule(Utils.getApplication()?.let { RoomModule(it) })
                .build().inject(this)

        initLayout()
        initViewModel()
        setListeners()
        setObservers()

    }

    private fun initLayout() {
        binding.rvPasswordRecycler.setHasFixedSize(true)
        binding.rvPasswordRecycler.layoutManager = LinearLayoutManager(
                this,
                LinearLayoutManager.HORIZONTAL,
                false
        )
    }

    private fun setListeners() {
        binding.ivBackButton.setOnClickListener {
            finish()
        }
    }

    private fun setObservers() {
        val args: Bundle? = intent.extras
        val folderId = args?.get("folder_id").toString().toInt()
        val folderName = args?.get("folder_name").toString()
        binding.tvFolderName.text = folderName

        viewModel.getPasswordsFromFolder(folderId).observe(this) { passwords ->
            passwordCards = passwords
            if(passwords.isEmpty()){
                binding.tvNoPasswordsInFolder.visibility = View.VISIBLE
                binding.rvPasswordRecycler.visibility = View.GONE
            }
            binding.rvPasswordRecycler.adapter = PasswordAdapter(
                    passwords,
                    this,
                    clickListener = {
                        passClickListener(it)
                    },
                    longClickListener = { i: Int, view: View ->
                        passLongClickListener(
                                i,
                                view
                        )
                    }
            ) {
                tagSearchClicker(it)
            }
        }
    }

    private fun tagSearchClicker(string: String) {}

    private fun passLongClickListener(i: Int, view: View) {}

    private fun passClickListener(position: Int) {
        val intent = Intent(this, PasswordViewActivity::class.java)
        intent.putExtra("password_id", passwordCards[position]._id)
        startActivity(intent)
    }

    private fun initViewModel() {
        viewModel = this.injectViewModel<FolderViewModel>(viewModelFactory)
    }
}