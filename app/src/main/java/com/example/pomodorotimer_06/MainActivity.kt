package com.example.pomodorotimer_06

import android.annotation.SuppressLint
import android.media.SoundPool
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.SeekBar
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    // Mark -Properties/////////////////////////////////////////
    private val remainMinTv: TextView by lazy { findViewById(R.id.remainMin_Tv) }
    private val remainSecTv: TextView by lazy { findViewById(R.id.remainSec_Tv) }
    private val timeSb: SeekBar by lazy { findViewById(R.id.time_Sb) }

    private val soundPool = SoundPool.Builder().build()
    private var tickingSoundId: Int? = null
    private var bellSoundId: Int? = null
    private var currentCountDownTimer: CountDownTimer? = null

    // Mark -LifeCycle/////////////////////////////////////////
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bindViews()
        initSounds()
    }

    override fun onResume() {
        super.onResume()
        soundPool.autoResume()
    }

    override fun onPause() {
        super.onPause()
        soundPool.autoPause()
    }

    // 마지막으로 정리할 것. 사운드 파일은 메모리를 많이 먹기 때문에 완전히 사용되지 않는다는 것이 명확할 때
    // 메모리를 해제하ㄷ자
    override fun onDestroy() {
        super.onDestroy()
        soundPool.release()
    }

    // Mark -타이머/////////////////////////////////////////
    private fun bindViews() {
        timeSb.setOnSeekBarChangeListener(
            object: SeekBar.OnSeekBarChangeListener{
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    // progressChanged 가 실제로 사용자에 의해서 변경되었는지 혹은 코드 상으로 변경되었는지를 구분하지 못해서
                    // 시간 초가 조금씩 이상하게 움직인다.
                    // 이를 해결하기 위해 실제로 사용자가 Seekbar 을 건드렸을 때만 변경될 때 업데이트를 하도록 설정해준다.
                    if(fromUser){
                        updateRemainTimes(progress * 60 * 1000L)
                    }

                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                    stopCountDown()
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    // ?:은 엘비스operater (좌측에 있는 값이 null일 경우 우측에 있는 값을 리턴한다.)
                    seekBar ?: return

                    if(seekBar.progress == 0){
                        stopCountDown()
                    }else{
                        startCountDown(seekBar)
                    }


                }

            }
        )
    }

    private fun startCountDown(seekBar: SeekBar) {
        currentCountDownTimer = createCountDownTimer(seekBar.progress * 60 * 1000L)
        Log.d("MainActivity", currentCountDownTimer.toString())
        currentCountDownTimer?.start()

        // 이 방식은 인자로 전달해야 할 프로퍼티가 nullable 할 경우 null 이 아닐  let으로 soundId 로 인자로 전달.
        // 널 이 아닐 경우에만 전달.
        tickingSoundId?.let { soundId ->
            soundPool.play(soundId, 1F, 1F, 0, -1, 1F)
        }
    }

    private fun stopCountDown() {
        currentCountDownTimer?.cancel()
        // 실제로 이 값이 null 이면 카운트다운타이머가 취소되어 없다는 것이므로 취소 후 실제로 null을 넣어주는 것이 코드 상에서 로그를 확인하기도 깔끔해진다.
        currentCountDownTimer = null
        soundPool.autoPause()

    }

    // 자바의 경우 리턴을 해주어서 카운트다운 타이머를 반환해주는 형식.
    /*
    private fun createCountDownTimer(initialMillis: Long) : CountDownTimer {
        return object: CountDownTimer(initialMillis, 1000L){
            override fun onTick(millisUntilFinished: Long) {
//                TODO("Not yet implemented")
            }
            override fun onFinish() {
//                TODO("Not yet implemented")
            }
        }
    }
     */
    // 코틀린의 경우 - 리턴 타입과 return 을 생략할 수 잇다.

    private fun createCountDownTimer(initialMillis: Long) =
        object: CountDownTimer(initialMillis, 1000L){
            override fun onTick(millisUntilFinished: Long) {
                updateRemainTimes(millisUntilFinished)
                updateSeekBar(millisUntilFinished)
            }

            override fun onFinish() {
                completeCountDown()
            }
    }

    private fun completeCountDown(){
        updateRemainTimes(0)
        updateSeekBar(0)
        soundPool.autoPause()
        // 이전에 let 을 ㅅ용한 것과 같은 (it) 인수가 하나여서 it 으로 전달.
        bellSoundId?.let { soundPool.play(it, 1F, 1F, 0, 0, 1F) }
    }



    @SuppressLint("SetTextI18n")
    private fun updateRemainTimes(remainMillis: Long) {
        val remainSecs = remainMillis / 1000

        remainMinTv.text = "%02d'".format(remainSecs / 60)
        remainSecTv.text = "%02d".format(remainSecs % 60)
    }

    private fun updateSeekBar(remainMillis: Long){
        timeSb.progress = (remainMillis / 1000 / 60).toInt() // remainMillis 자체가 Log 이기 때문에 Int로 나누면 Int로 바꾸어 주어야 함.


    }

    // Mark -효과음/////////////////////////////////////////
    private fun initSounds(){
        // 로드를 하게 되면 로드된 사운드의 아이디를 반환하기 때문에 프로퍼티를 저장해야 함. 변수에 저장
        tickingSoundId = soundPool.load(this, R.raw.timer_ticking, 1)
        bellSoundId = soundPool.load(this, R.raw.timer_bell, 1)
    }
}