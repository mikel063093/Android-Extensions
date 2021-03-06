package com.tunjid.androidx.fragments

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import androidx.annotation.IdRes
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.tunjid.androidx.MutedColors
import com.tunjid.androidx.R
import com.tunjid.androidx.baseclasses.AppBaseFragment
import com.tunjid.androidx.core.components.args
import com.tunjid.androidx.core.content.colorAt
import com.tunjid.androidx.core.text.color
import com.tunjid.androidx.core.text.formatSpanned
import com.tunjid.androidx.core.text.scale
import com.tunjid.androidx.isDarkTheme
import com.tunjid.androidx.navigation.Navigator
import com.tunjid.androidx.navigation.StackNavigator
import com.tunjid.androidx.navigation.addOnBackPressedCallback
import com.tunjid.androidx.navigation.childStackNavigationController
import com.tunjid.androidx.uidrivers.crossFade
import com.tunjid.androidx.view.util.InsetFlags
import com.tunjid.androidx.viewmodels.routeName
import java.util.*


class IndependentStacksFragment : AppBaseFragment(R.layout.fragment_independent_stack) {

    private val navigators = mutableMapOf<Int, StackNavigator>()
    private val visitOrder = ArrayDeque<Int>()

    override val insetFlags = InsetFlags.NO_TOP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        savedInstanceState?.getIntArray(ORDER)?.apply { visitOrder.addAll(this.asList()) }

        addOnBackPressedCallback {
            isEnabled =
                    if (navigator.current !== this@IndependentStacksFragment) false
                    else visitOrder.asSequence()
                            .map(::navigatorFor)
                            .map(StackNavigator::pop)
                            .firstOrNull { it } ?: false

            if (!isEnabled) activity?.onBackPressed()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (navigators.isEmpty()) for (id in CONTAINER_IDS) navigatorFor(id).apply {
            if (current == null) push(IndependentStackChildFragment.newInstance(name(containerId), 1))
        }

        uiState = uiState.copy(
                toolbarTitle = this::class.java.routeName.color(Color.WHITE),
                toolBarMenu = 0,
                toolbarShows = true,
                fabShows = false,
                fabClickListener = View.OnClickListener {},
                showsBottomNav = true,
                lightStatusBar = false,
                navBarColor = requireContext().colorAt(R.color.transparent)
        )
    }

    override fun onResume() {
        super.onResume()
        uiState = uiState.copy(backgroundColor = MutedColors.colorAt(requireContext().isDarkTheme, 0))
    }

    override fun onPause() {
        super.onPause()
        uiState = uiState.copy(backgroundColor = Color.TRANSPARENT)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putIntArray(ORDER, visitOrder.toIntArray())
        super.onSaveInstanceState(outState)
    }

    private fun navigatorFor(id: Int) = navigators.getOrPut(id) {
        val stackNavigator by childStackNavigationController(id)
        stackNavigator.apply { transactionModifier = { crossFade() } }
    }

    internal fun addTosStack(id: Int, depth: Int, name: String) {
        visitOrder.remove(id)
        visitOrder.addFirst(id)
        navigatorFor(id).push(IndependentStackChildFragment.newInstance(name, depth))
    }

    private fun name(@IdRes containerId: Int) =
            resources.getResourceEntryName(containerId).replace("_", " ")

    companion object {
        fun newInstance(): IndependentStacksFragment = IndependentStacksFragment().apply { arguments = Bundle() }
    }

}

class IndependentStackChildFragment : Fragment(), Navigator.TagProvider {

    override val stableTag: String
        get() = "${javaClass.simpleName}-$name-$depth"

    private var name: String by args()

    var depth: Int by args()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? = MaterialButton(inflater.context).apply {
        val spacing = context.resources.getDimensionPixelSize(R.dimen.single_margin)

        backgroundTintList = ColorStateList.valueOf(MutedColors.colorAt(inflater.context.isDarkTheme, 1))
        strokeColor = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
        strokeWidth = context.resources.getDimensionPixelSize(R.dimen.eigth_margin)
        textSize = resources.getDimensionPixelSize(R.dimen.small_text).toFloat()
        transformationMethod = null
        gravity = Gravity.CENTER
        cornerRadius = spacing
        text = getString(R.string.double_line_format).formatSpanned(
                name,
                resources.getQuantityString(R.plurals.stack_depth, depth, depth).scale(0.5F)
        )
        layoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT).apply {
            gravity = Gravity.CENTER
            leftMargin = spacing
            topMargin = spacing
            rightMargin = spacing
            bottomMargin = spacing
        }

        setPadding(spacing)
        setTextColor(ContextCompat.getColor(context, R.color.white))
        setOnClickListener {
            val parent = parentFragment as? IndependentStacksFragment
            parent?.addTosStack(this@IndependentStackChildFragment.id, depth + 1, name)
        }
    }

    companion object {
        fun newInstance(name: String, depth: Int): IndependentStackChildFragment = IndependentStackChildFragment().apply {
            this.name = name
            this.depth = depth
        }
    }
}

private const val ORDER = "ORDER"
private val CONTAINER_IDS = intArrayOf(R.id.stack_1, R.id.stack_2, R.id.stack_3, R.id.stack_4)