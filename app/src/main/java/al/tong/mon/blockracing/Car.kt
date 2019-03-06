package al.tong.mon.blockracing

import android.graphics.*

class Car {

    companion object {
        const val COLOR_MY_CAR = Colors.BLUE_GRAY_900
        const val COLOR_OBSTACLE_CAR = Colors.BLUE_GRAY_400
    }

    var blockSize: Int = 0
    var x: Int = 0
    var y: Int = 0
    var width: Int = 0
    var height: Int = 0
    var paint: Paint
    var boundary: Rect
    var rectF: RectF
    var last: Boolean

    constructor(blockSize: Int) : this(blockSize, 0, 0, COLOR_MY_CAR)

    constructor(blockSize: Int, x: Int, y: Int, color: Long) {
        this.blockSize = blockSize
        this.x = x
        this.y = y
        this.width = this.blockSize * 3 + RacingView.SPACING * 2
        this.width = this.blockSize * 4 + RacingView.SPACING * 3

        paint = Paint()
        paint.isAntiAlias = true
        paint.color = color.toInt()

        boundary = Rect()
        rectF = RectF()
        last = false
    }

    fun isLast(): Boolean {
        return last
    }

    fun setPosition(x: Int) {
        this.x = x
    }

    fun moveDown(speed: Int) {
        this.y += speed
    }

    fun moveLeft() {
        this.x = this.x - width - RacingView.SPACING
    }

    fun moveRight() {
        this.x = this.x + width + RacingView.SPACING
    }

    fun gettingBoundary(): Rect {
        this.boundary.left = x + blockSize
        this.boundary.top = y + blockSize
        this.boundary.right = x + width - blockSize
        this.boundary.bottom = y + height - blockSize
        return boundary
    }

    fun draw(c: Canvas) {
        drawBlock(c, 1, 0)

        drawBlock(c, 0, 1)
        drawBlock(c, 1, 1)
        drawBlock(c, 2, 1)


        drawBlock(c, 1, 2)


        drawBlock(c, 0, 3)
        drawBlock(c, 2, 3)
    }

    fun draw(c: Canvas, isCollision: Boolean) {
        var color: Int = Color.RED
        if (!isCollision) color = COLOR_MY_CAR.toInt()
        paint.color = color
        drawBlock(c, 1, 0)

        drawBlock(c, 0, 1)
        drawBlock(c, 1, 1)
        drawBlock(c, 2, 1)


        drawBlock(c, 1, 2)


        drawBlock(c, 0, 3)
        drawBlock(c, 2, 3)

    }

    fun drawBlock(c: Canvas, px: Int, py: Int) {
        rectF.left = (px * blockSize + px * RacingView.SPACING + x).toFloat()
        rectF.top = (py * blockSize + py * RacingView.SPACING + y).toFloat()
        rectF.right = rectF.left + blockSize
        rectF.bottom = rectF.top + blockSize
        c.drawRoundRect(rectF, 8f, 8f, paint)
    }


}