package com.ripple.library

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.AttributeSet
import android.view.View
import com.ripple.library.R
import java.lang.ref.WeakReference
import kotlin.math.min

/**
 *
 * @Description: 类注释
 * @Author: slong
 * @CreateDate: 2023/3/27 4:40 PM
 */
class RippleView : View {

    private val DEFAULT_INTERVAL_TIME = 16L

    private var mColor: Int // 颜色
    private var mStrokeWidth: Float // 画笔宽度
    private var mSpeedMultiple: Float // 速度 默认为1.0
    private var mIntervalStep: Float // 圆圈之间的间距
    private var mRadiusCenter: Float // 内部区域半径
    private var mAlpha: Boolean // 是否透明

    private val handler by lazy {
        RippleHandler(this)
    }

    private var mWidth: Int = 0
    private var mHeight: Int = 0
    private var mPaint: Paint

    // 最大半径
    private var mMaxRadius: Int = 0

    // 最小半径
    private var mMinRadius: Float = 0f

    // 当前正在展示的圆圈个数
    private var mRippleCount = 0

    // 当前移动的距离 在0-mIntervalStep之间循环
    private var mMoveStep = 1

    // 是否展示
    private var mIsShow = false

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int)
            : super(context, attrs, defStyleAttr) {
        // 获取用户配置属性
        val tya = context.obtainStyledAttributes(attrs, R.styleable.RippleView)
        mColor = tya.getColor(R.styleable.RippleView_ripple_color, Color.BLUE)
        mStrokeWidth = tya.getDimension(R.styleable.RippleView_ripple_stroke_width, 1.0f)
        mSpeedMultiple = tya.getFloat(R.styleable.RippleView_ripple_speed_multiple, 1.0f)
        mIntervalStep = tya.getDimension(R.styleable.RippleView_ripple_interval_step, 0f)
        mRadiusCenter = tya.getDimension(R.styleable.RippleView_ripple_radius_center, 0f)
        mAlpha = tya.getBoolean(R.styleable.RippleView_ripple_alpha, false)
        tya.recycle()

        if (mSpeedMultiple <= 0) {
            mSpeedMultiple = 1.0f
        }

        // 设置画笔样式
        mPaint = Paint().apply {
            color = mColor
            strokeWidth = mStrokeWidth
            style = Paint.Style.STROKE
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        // 计算相关参数
        mWidth = measuredWidth - paddingLeft - paddingRight
        mHeight = measuredHeight - paddingTop - paddingBottom

        mMaxRadius = min(mWidth, mHeight) / 2

        mMinRadius = if (mRadiusCenter < mMaxRadius) mRadiusCenter else 0f

        // 如果用户没有设置间距，则默认为mMaxRadius的1/4
        if (mIntervalStep == 0f) {
            mIntervalStep = mMaxRadius / 4f
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        if (!mIsShow) {
            return
        }

        for (i in 0 until mRippleCount) {
            // 当前圆圈的半径大小
            val r = mMinRadius + mIntervalStep * i + mMoveStep


            // 计算不透明的数值，这里有个小知识，就是如果不加上double的话，255除以一个任意比它大的数都将是0
            if (mAlpha) {
                val alpha: Double = 255 - r * (255 / mMaxRadius.toDouble())
                if (alpha < 0) { //小于0需要置0
                    mPaint.alpha = 0
                } else {
                    mPaint.alpha = alpha.toInt()
                }
            } else {
                mPaint.alpha = 255
            }

            canvas?.drawCircle(mWidth / 2f, mHeight / 2f, r, mPaint)
        }

        // 移动距离+1
        mMoveStep++
        // 循环判断，在0-mIntervalStep区间里面
        if (mMoveStep >= mIntervalStep) {
            // 重新置为0，但是mRippleCount需要加1
            mMoveStep = 0
            mRippleCount++
        }

        // 判断最大的圆圈是否超过view，超过的圆圈需要删除，mRippleCount--
        if (mIntervalStep * (mRippleCount - 1) + mMoveStep >= mMaxRadius - mMinRadius) {
            mRippleCount--
        }

        // 每个mSpeedMultiple * DEFAULT_INTERVAL_TIME毫秒，重新执行onDraw方法
        handler.sendMessageDelayed(
            handler.obtainMessage(),
            (DEFAULT_INTERVAL_TIME / mSpeedMultiple).toLong()
        )
    }

    fun start() {
        mIsShow = true
        postInvalidate()
    }

    fun stop() {
        mIsShow = false
        postInvalidate()
    }

    private class RippleHandler(rippleView: RippleView) : Handler(Looper.getMainLooper()) {
        private val mRef: WeakReference<RippleView> = WeakReference(rippleView)

        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mRef.get()?.run {
                postInvalidate()
            }
        }
    }
}
