package hu.bme.hit.messages

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MessageLayoutManager(context: Context?) : LinearLayoutManager(context){

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams {
        val params = RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT)
        params.marginStart = 300
        return params
    }

    override fun checkLayoutParams(lp: RecyclerView.LayoutParams?): Boolean {
        return super.checkLayoutParams(lp)
    }

    override fun generateLayoutParams(lp: ViewGroup.LayoutParams?): RecyclerView.LayoutParams {
        lp?.width = ViewGroup.LayoutParams.WRAP_CONTENT
        return super.generateLayoutParams(lp)
    }

    override fun generateLayoutParams(
        c: Context?,
        attrs: AttributeSet?
    ): RecyclerView.LayoutParams {
        return super.generateLayoutParams(c, attrs)
    }
}