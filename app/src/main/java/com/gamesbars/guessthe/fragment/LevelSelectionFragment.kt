package com.gamesbars.guessthe.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gamesbars.guessthe.R
import com.gamesbars.guessthe.Storage
import com.gamesbars.guessthe.adapter.LevelSelectionAdapter
import com.gamesbars.guessthe.ads.AdsUtils
import com.gamesbars.guessthe.databinding.FragmentLevelselectionBinding
import com.gamesbars.guessthe.playSound

class LevelSelectionFragment : Fragment() {

    companion object {
        fun newInstance(pack: String): LevelSelectionFragment {
            val args = Bundle()
            args.putString("pack", pack)
            val fragment = LevelSelectionFragment()
            fragment.arguments = args
            return fragment
        }
    }

    lateinit var pack: String

    var isClickable = true

    lateinit var recycler: RecyclerView
    private var _binding: FragmentLevelselectionBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pack = arguments!!.getString("pack")!!
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        AdsUtils.fixDensity(resources)
        _binding = FragmentLevelselectionBinding.inflate(inflater, container, false)
        val saves = activity!!.getSharedPreferences("saves", Context.MODE_PRIVATE)

        binding.backIv.setOnClickListener {
            if (isClickable) {
                playSound(context!!, R.raw.button)
                activity!!.onBackPressed()
            }
        }

        recycler = binding.levelsRv
        recycler.layoutManager = GridLayoutManager(binding.root.context, 4)
        val completedLevelCount = Storage.getCompletedLevels(pack)
        val adapter = LevelSelectionAdapter(pack, completedLevelCount)
        adapter.onItemClickListener = { level ->
            if (level <= completedLevelCount + 1) {
                saves.edit().apply {
                    putInt(pack, level)
                    apply()
                }
                fragmentManager?.popBackStack()
                fragmentManager?.beginTransaction()!!
                    .replace(R.id.fragmentFl, LevelFragment.newInstance(pack), resources.getString(R.string.level_fragment_tag))
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit()
            }
        }
        recycler.adapter = adapter

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        isClickable = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}