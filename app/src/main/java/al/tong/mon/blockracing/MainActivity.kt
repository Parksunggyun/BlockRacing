package al.tong.mon.blockracing

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    companion object {
        const val INIT_SPEED = 12
        const val SPEED_INTERVAL = 2
        const val MAX_SPEED = 40
    }


    /*    lateinit var contLabel: View
        lateinit var tvNotfiy: TextView
        lateinit var tvScore: TextView
        lateinit var tvLevel: TextView
        lateinit var tvBest: TextView
        lateinit var ivCenter: ImageView
        lateinit var racingView: RacingView*/
    var score = 0
    var level = 0
    var bestScore = 0
    var playCount = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        imgCenter.setOnClickListener { play() }
        playCount = 0
        initialize()
    }

    override fun onResume() {
        if (racingView != null && racingView!!.playState == RacingView.PlayState.Pause) {
            racingView.pause()
        }
        super.onResume()
    }

    override fun onPause() {
        if(racingView != null && racingView.playState == RacingView.PlayState.Playing) {
            racingView.pause()
        }
        super.onPause()
    }

    override fun onDestroy() {
        racingView!!.reset()
        super.onDestroy()
    }

    private var myHandler = object : Handler() {
        override fun handleMessage(msg: Message?) {
            when(msg!!.what) {
                RacingView.MSG_SCORE -> {
                    score += level
                    tvScore.text = score.toString()
                }
                RacingView.MSG_COLLISION -> {
                    var achieveBest = false
                    if (bestScore < score) {
                        tvBest.text = score.toString()
                        bestScore = score
                        achieveBest = true
                    }
                    collision(achieveBest)
                }
                RacingView.MSG_COMPLETE -> {
                    level++

                    if (racingView!!.speed < MAX_SPEED) {
                        racingView!!.speed += SPEED_INTERVAL
                    }

                    tvLevel.text = level.toString()
                    prepare()
                }
            }
        }
    }

    fun initialize() {
        reset()
        prepare()
    }

    fun reset() {
        score = 0
        level = 1
        bestScore = loadBestScore()

        racingView!!.speed = INIT_SPEED
        racingView!!.playState = RacingView.PlayState.Ready

        tvScore.text = score.toString()
        tvLevel.text = level.toString()
        tvBest.text = bestScore.toString()
    }

    fun prepare() {
        tvLevel.text = level.toString()
        tvNotify.text = "LEVEL $level"
        showLabelContainer()

        imgCenter.setImageResource(R.drawable.ic_play)
    }

    fun play() {
        if (racingView!!.playState == RacingView.PlayState.Collision) {
            initialize()

            racingView!!.reset()
            return
        }

        if (racingView!!.playState == RacingView.PlayState.Playing) {
            pause()
        } else {
            imgCenter.setImageResource(R.drawable.ic_pause)

            showArrowToast()

            if (racingView!!.playState == RacingView.PlayState.Pause) {
                racingView!!.resume()
            } else if (racingView!!.playState == RacingView.PlayState.LevelUp) {
                racingView!!.resume()
                hideLabelContainer()
            } else {
                playCount++
                if (playCount > 5) {
                    playCount = 0
                    imgCenter.setImageResource(R.drawable.ic_play)
                    return
                }

                hideLabelContainer()
                racingView!!.play(myHandler)
            }
        }
    }

    fun pause() {
        imgCenter.setImageResource(R.drawable.ic_play)
        racingView!!.pause()
    }

    fun collision(achieveBest: Boolean) {
        if (achieveBest) {
            tvNotify.text = "Congratulation! \n You're the Best!"
        } else {
            tvNotify.text = "Try again..."
        }
        contNotify.visibility = View.VISIBLE
        contNotify.startAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top))
        imgCenter.setImageResource(R.drawable.ic_retry)
    }

    fun showLabelContainer() {
        contNotify.visibility = View.VISIBLE
        contNotify.startAnimation(AnimationUtils.loadAnimation(this, R.anim.abc_slide_in_top))
    }

    fun hideLabelContainer() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.abc_slide_out_bottom)
        anim.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                contNotify.visibility = View.GONE
            }

            override fun onAnimationStart(animation: Animation?) {
            }
        })
        contNotify.startAnimation(anim)
    }

    fun showArrowToast() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.abc_fade_in)
        anim.setAnimationListener(object: Animation.AnimationListener {
            override fun onAnimationRepeat(animation: Animation?) {
            }

            override fun onAnimationEnd(animation: Animation?) {
                toast.postDelayed({
                    hideArrowToast()
                },1000)
            }

            override fun onAnimationStart(animation: Animation?) {
                toast.visibility = View.VISIBLE
            }
        })
        toast.startAnimation(anim)
    }

    fun hideArrowToast() {
        val anim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {

            }

            override fun onAnimationEnd(animation: Animation) {
                toast.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation) {

            }
        })
        toast.startAnimation(anim)
    }
    fun loadBestScore(): Int {
        return 10000
    }

    fun saveBestScore() {

    }
}
