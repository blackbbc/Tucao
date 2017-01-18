package me.sweetll.tucao.business.home.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import me.sweetll.tucao.R
import me.sweetll.tucao.business.home.adapter.GameAdapter
import me.sweetll.tucao.business.home.viewmodel.GameViewModel
import me.sweetll.tucao.business.video.VideoActivity
import me.sweetll.tucao.databinding.FragmentGameBinding
import me.sweetll.tucao.model.raw.Game

class GameFragment : Fragment() {
    lateinit var binding: FragmentGameBinding

    val viewModel = GameViewModel(this)

    val gameAdapter = GameAdapter(null)

    var isLoad = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_game, container, false)
        return rootView
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadData()
        }
        setupRecyclerView()
    }

    fun setupRecyclerView() {
        binding.gameRecycler.layoutManager = LinearLayoutManager(activity)
        binding.gameRecycler.adapter = gameAdapter

        binding.gameRecycler.addOnItemTouchListener(object: OnItemChildClickListener() {
            override fun onSimpleItemChildClick(adapter: BaseQuickAdapter<*, *>, view: View, position: Int) {
                when (view.id) {
                    R.id.linear1, R.id.linear2, R.id.linear3, R.id.linear4 -> {
                        VideoActivity.intentTo(activity, view.tag as String)
                    }
                }
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden && !isLoad && !binding.swipeRefresh.isRefreshing) {
            viewModel.loadData()
        }
    }

    fun loadGame(game: Game) {
        isLoad = true
        gameAdapter.setNewData(game.recommends)
    }

    fun setRefreshing(isRefreshing: Boolean) {
        binding.swipeRefresh.isRefreshing = isRefreshing
    }
}