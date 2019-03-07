package al.tong.mon.blockracing

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import java.util.*
import kotlin.collections.ArrayList

class RacingView : View {

    //게임판 사이즈 변경 시 MAX_COL_CNT, VERTICAL_CNT 비율로 변경해줘야함.
    // 첨에 MAX_COL_CNT = 3, VERTICAL_CNT = 20, HORIZONTAL_CNT = 13 이었음.

    companion object {
        const val SPACING = 2
        private const val MAX_COL_CNT = 5 // 차가 이동 가능한 칸 수
        const val VERTICAL_CNT = 29 // 블록의 사이즈를 관장
        const val HORIZONTAL_CNT = MAX_COL_CNT * 3 + 2 + 2

        const val MSG_SCORE = 1000
        const val MSG_COLLISION = 2000
        const val MSG_COMPLETE = 3000
    }

    enum class PlayState {
        Ready, Playing, Pause, LevelUp, Collision
    }
    var myHandler: Handler? = null
    var playState: PlayState? = null
    var boardWidth = 0
    var viewHeight = 0
    var blockSize = 0
    var boardLeft = 0
    var boardRight = 0
    var speed = 0

    lateinit var paint: Paint
    lateinit var random: Random

    var walls: ArrayList<RectF>? = null
    var obstacles: ArrayList<Car>? = null
    var myCar: Car? = null


    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr)


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        val height = bottom - top
        if (bottom - top > 0) {
            val blockSize = (height - (SPACING * (VERTICAL_CNT + 1))) / VERTICAL_CNT
            val w = blockSize * HORIZONTAL_CNT + SPACING * (HORIZONTAL_CNT - 1)
            val h = blockSize * VERTICAL_CNT + SPACING * (VERTICAL_CNT + 1)

            val viewWidth = right - left
            Log.e("blockSize", "$blockSize")
            Log.e("w", "$w")
            Log.e("h", "$h")
            Log.e("viewWidth", "$viewWidth")

            boardLeft = (viewWidth - w) / 2
            boardRight = (viewWidth) - (viewWidth - w) / 2
            initialize(w, h, blockSize)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas!!.drawColor(Colors.BLUE_GRAY_200.toInt())
        drawWall(canvas)

        if (playState != PlayState.Ready) {
            drawObstacles(canvas)
        }

        if (myCar != null) {
            myCar!!.draw(canvas, playState == PlayState.Collision)
        }

        if (myHandler != null && playState == PlayState.Playing) {
            myHandler!!.sendEmptyMessage(MSG_SCORE)
        }
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event!!.action == MotionEvent.ACTION_DOWN) {
            if (event.x > boardWidth / 2) {
                moveRight()
            } else {
                moveLeft()
            }
        }

        return super.onTouchEvent(event)
    }

    fun play(handler: Handler) {
        this.myHandler = handler
        playState = PlayState.Playing
    }

    fun resume() {
        playState = PlayState.Playing
    }

    fun pause() {
        playState = PlayState.Pause
    }

    fun reset() {
        playState = PlayState.Ready
        createObstacles()
    }

    fun initialize(w: Int, h: Int, blockSize: Int) {
        if (myCar != null) {
            return
        }

        playState = PlayState.Ready

        this.boardWidth = w
        this.viewHeight = h
        this.blockSize = blockSize

        setProperties()

        createWall()
        createObstacles()

        myCar = Car(blockSize)
        myCar!!.x = getLeftPositionX(random.nextInt(2))
        myCar!!.y = viewHeight - myCar!!.height - SPACING
    }

    fun setProperties() {
        paint = Paint()
        paint.isAntiAlias = true
        random = Random()
    }

    fun createWall() {
        walls = ArrayList()
        for (i in 0 until RacingView.VERTICAL_CNT) {
/*            if (i != 0 && i != RacingView.VERTICAL_CNT && i % 4 == 0) {
                continue
            }*/
            Log.e("create Wall $i", "ggg")

            val left = RectF()
            left.top = (i * blockSize + SPACING * (i + 1)).toFloat()
            left.bottom = left.top + blockSize
            left.left = boardLeft.toFloat()
            left.right = left.left + blockSize
            walls!!.add(left)

            val right = RectF()
            right.top = (i * blockSize + SPACING * (i + 1)).toFloat()
            right.bottom = right.top + blockSize
            right.left = (boardRight - blockSize).toFloat()
            right.right = boardRight.toFloat()
            walls!!.add(right)
            Log.e("walls.size", "${walls!!.size}")
        }

    }

    fun drawWall(canvas: Canvas?) {
        if (walls != null) {
            paint.color = Colors.BLUE_GRAY_600.toInt()
            for (rect in walls!!) {
                canvas!!.drawRoundRect(rect, 8f, 8f, paint)
            }
        }
    }
    fun createObstacles() {
        if (obstacles == null) {
            obstacles = ArrayList()
        } else {
            obstacles!!.clear()
        }

        val carHeight = blockSize * 4 + SPACING * 3
        // 차의 시작 위치를 계속 위로 쌓음.
        var startOffset = -carHeight
        var cnt = speed * (MAX_COL_CNT)

        if (speed >= 40) {
            cnt *= 4
        } else if (speed >= 30) {
            cnt *= 3
        } else if (speed >= 20) {
            cnt *= 2
        }
        Log.e("speed", "$speed")
        Log.e("MAX_COL_CNT", "$MAX_COL_CNT")
        Log.e("cnt", "$cnt")

        for (i in 0 until cnt) {
            // 1 ~ 4 사이, 가로가 5칸이므로 차가 최소 한 칸은 없어야 지니갈 수 있음.
            val r = random.nextInt(MAX_COL_CNT -1) + 1
            val posArray = ArrayList<Int>()
            posArray.add(0)
            posArray.add(1)
            posArray.add(2)
            posArray.add(3)
            posArray.add(4)
            Collections.shuffle(posArray)
            for (c in 0 until r) {
                val pos = posArray[c]
                val obstacle = Car(blockSize, getLeftPositionX(pos), startOffset, Car.COLOR_OBSTACLE_CAR)
                obstacles!!.add(obstacle)
            }

/*            if (i % 4 == 0) {
                val r1 = random.nextInt(MAX_COL_CNT)
                if (r != r1) {
                    val obstacle1 = Car(blockSize, getLeftPositionX(r1), startOffset, Car.COLOR_OBSTACLE_CAR)
                    obstacles!!.add(obstacle1)
                }
            }*/
            // 적 차 사이의 거리
            startOffset = (startOffset - ((carHeight + SPACING) * 2))
        }
        obstacles!![obstacles!!.size - 1].last = true
    }

    fun drawObstacles(canvas: Canvas?) {
        if (obstacles != null) {
            var isComplete = false
            val size = obstacles!!.size

            for (i in 0 until size) {
                val obstacle = obstacles!![i]
                if (playState == PlayState.Playing) {
                    obstacle.moveDown(speed)
                }
                obstacle.draw(canvas!!)

                if (playState == PlayState.Playing) {
                    if(isCollision(obstacle)) {
                        playState = PlayState.Collision
                        if (myHandler != null) {
                            myHandler!!.sendEmptyMessage(MSG_COLLISION)
                        }
                    }

                    if (obstacle.isLast() && obstacle.y >= viewHeight + obstacle.height + blockSize) {
                        isComplete = true
                    }
                }
            }
            if (isComplete) {
                playState = PlayState.LevelUp
                createObstacles()

                if (myHandler != null) {
                    myHandler!!.sendEmptyMessage(MSG_COMPLETE)
                }
            }
        }
    }

    fun isCollision(obstacle: Car): Boolean {
        if (myCar == null) {
            return false
        }
        return myCar!!.gettingBoundary().intersect(obstacle.gettingBoundary())
    }

    fun moveLeft() {
        if (playState != PlayState.Playing) {
            return
        }

        if (myCar != null) {
            val left = boardLeft + blockSize * 2 + SPACING * 2
            if (myCar!!.x > left) {
                myCar!!.moveLeft()
            }
            if (myCar!!.x <= left) {
                myCar!!.setPosition(left)
            }
        }
        Log.e("moveLeft point x=", "${myCar!!.x}")
    }

    fun moveRight() {
        if (playState != PlayState.Playing) {
            return
        }

        if (myCar != null) {
            val right = boardRight - blockSize * 2 - SPACING * 2

            if (myCar!!.x + myCar!!.width < right) {
                myCar!!.moveRight()
            }

            if (myCar!!.x + myCar!!.width >= right) {
                myCar!!.setPosition(right - myCar!!.width)
            }
        }
        Log.e("moveRight point x=", "${myCar!!.x}")
    }


    fun getLeftPositionX(r: Int): Int {
        return boardLeft+ blockSize+ SPACING+ blockSize+ SPACING * (r + 1)+ (blockSize * 3 + SPACING * 2)* r
    }

}