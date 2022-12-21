package com.app.testwebviewforlink.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.TextView
import android.widget.Toast
import com.app.testwebviewforlink.R
import com.app.testwebviewforlink.adapter.CharacterAdapter
import com.app.testwebviewforlink.http.CharacterBean
import com.app.testwebviewforlink.utils.NetUtils
import com.app.testwebviewforlink.utils.toast
import okhttp3.OkHttpClient

class CharacterListFragment(val callBack: (abName: String) -> Unit) : BaseDialogFragment() {

    private val TAG = "CharacterListFragment"

    init {
        isNeedViewBg = false
        isNeedFullScreen = true
    }

    override fun getContentViewId() = R.layout.fragment_list_character

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(view) {
            findViewById<View>(R.id.viewOutSide).setOnClickListener {
                this@CharacterListFragment.dismiss()
            }
            val gridView = findViewById<GridView>(R.id.gridView)
            var pos = -1
            lateinit var list: List<CharacterBean>
            NetUtils.getCharacterList(object : NetUtils.FormatData<List<CharacterBean>> {
                override fun onResult(t: List<CharacterBean>) {
                    Log.e(TAG, "onResult: ${Thread.currentThread().name}")
                    if (t.isNotEmpty()) {
                        list = t
                        val characterAdapter = CharacterAdapter(requireContext(), t)
                        gridView.adapter = characterAdapter
                        characterAdapter.notifyDataSetChanged()
                        gridView.setOnItemClickListener { _, _, position, _ ->
                            pos = position
                            characterAdapter.choiceItem(position)
                        }
                    } else {
                        Log.e(TAG, "onResult: list is null")
                    }
                }

            })
            findViewById<TextView>(R.id.tvSure).setOnClickListener {
                if (pos <= 0) {
                    toast("请选择数字人")
                } else {
                    callBack.invoke(list[pos].abName)
                    this@CharacterListFragment.dismiss()
                }
            }
        }


    }
}