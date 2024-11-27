package com.xiaobo.proxysocks5

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gyf.immersionbar.ktx.immersionBar

class SelectLineActivity : AppCompatActivity() {
    private val TAG:String = "SelectLineActivity"
    private val dataList:ArrayList<Line> by lazy { arrayListOf() }
    private val rvView:RecyclerView by lazy { findViewById(R.id.rv_line) }
    private val tvRefresh: TextView by lazy { findViewById(R.id.tv_refresh) }
    private val btnConfirm: View by lazy { findViewById(R.id.btn_confirm) }
    private val tvLineUpdate: TextView by lazy { findViewById(R.id.tv_line_update) }
    private val tvLang: TextView by lazy { findViewById(R.id.tv_lang) }
    private var currentLine:Line? = null
    private lateinit var adapter:LineAdapter
    private lateinit var sharedPreferences:SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_line)
        immersionBar {
            statusBarColor(R.color.bg_color)
            fitsSystemWindows(true)
            statusBarDarkFont(true)
        }
        sharedPreferences = getSharedPreferences("user",MODE_PRIVATE)


        rvView.layoutManager = LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false)
        adapter = LineAdapter(dataList = dataList)
        adapter.listener = object: LineAdapter.MyOnClickListener{
            override fun click(position: Int) {
                for(item:Line in dataList){
                    item.isCheck = false
                }
                dataList[position].isCheck = true
                currentLine = dataList[position]

                adapter.notifyDataSetChanged()
            }
        }
        rvView.adapter = adapter

        initLineConf()

        tvRefresh.setOnClickListener {
            ping()
        }
        btnConfirm.setOnClickListener{
            if(currentLine==null){
                Toast.makeText(this,"请至少选择一条线路!",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val editor = sharedPreferences.edit()
            editor.putString("lineIp",currentLine!!.ip)
            editor.apply()
            val intent = Intent(this,MainActivity::class.java)
            intent.putExtra("ip",currentLine!!.ip)
            startActivity(intent)
        }

        tvLineUpdate.setOnClickListener {
            pullRemoteLines()
        }

        val tvLineUpdateDrawable = ContextCompat.getDrawable(this,R.mipmap.ic_refresh_line)
        tvLineUpdateDrawable?.bounds = Rect(0,0,DensityUtils.dpToPx(this,20.0f),DensityUtils.dpToPx(this,20.0f))
        tvLineUpdate.setCompoundDrawablesRelative(tvLineUpdateDrawable,null,null,null)

        val tvRefreshDrawable = ContextCompat.getDrawable(this,R.mipmap.ic_test_speed)
        tvRefreshDrawable?.bounds = Rect(0,0,DensityUtils.dpToPx(this,20.0f),DensityUtils.dpToPx(this,20.0f))
        tvRefresh.setCompoundDrawablesRelative(tvRefreshDrawable,null,null,null)

        val tvLangDrawable = ContextCompat.getDrawable(this,R.mipmap.ic_language)
        tvLangDrawable?.bounds = Rect(0,0,DensityUtils.dpToPx(this,20.0f),DensityUtils.dpToPx(this,20.0f))
        tvLang.setCompoundDrawablesRelative(tvLangDrawable,null,null,null)
    }


    /*
    *   curl -x socks5://8.134.178.58:7891 http://baidu.com

        curl -x socks5://47.122.49.115:7891 http://baidu.com

        curl -x socks5://47.122.52.242:7891 http://baidu.com

        curl -x socks5://118.178.194.115:7891 http://baidu.com

        curl -x socks5://8.134.174.221:7891 http://baidu.com
    * */
    fun initLineConf(lines:ArrayList<Line>?=null){

        dataList.clear()
        if(lines!=null){
            dataList.addAll(lines)
        }else{
            val lineJson = sharedPreferences.getString("lineStr","")
            if(lineJson.isNullOrEmpty()){
                dataList.add(Line(0,"线路1","8.134.178.58"))//8.134.178.58
                dataList.add(Line(1,"线路2","47.122.49.115"))
                dataList.add(Line(2,"线路3","47.122.52.242"))
                dataList.add(Line(3,"线路4","118.178.194.115"))
                dataList.add(Line(4,"线路5","8.134.174.221"))
            }else{
                val lineList = PingUtils.parseRemoteLineStr(lineJson)
                dataList.addAll(lineList)
            }
        }

        dataList.add(Line(-1,"线路6","",speed = "30"))

        reSetLine()
        ping()
    }

    fun reSetLine(){
        val prevLine = sharedPreferences.getString("lineIp",null)
        selectLine(prevLine?:dataList[0].ip)
    }

    fun selectLine(ip:String){
        for(itm in dataList.withIndex()){
            val item = itm.value
            if(item.ip.equals(ip)){
                item.isCheck = true
                currentLine = item
                adapter.currentPosition = itm.index
            }else{
                item.isCheck = false
            }
        }
        runOnUiThread { adapter.notifyDataSetChanged() }

    }

    fun ping(){
        if(dataList.isEmpty()) return
        Thread(Runnable {
            synchronized(dataList){
                for(item:Line in dataList){
                    if("".equals(item.ip)) {
                        continue
                    }else{
                        val speed = PingUtils.executePingAndGetDelay(item.ip)
                        Log.d(TAG,"ping result ${item.name}>>>$speed")
                        item.speed = speed
                    }

                }
                runOnUiThread{
                    adapter.notifyDataSetChanged()
                }
            }
        }).start()
    }

    fun pullRemoteLines(){
        Thread(Runnable {
            synchronized(dataList){
                val pair = PingUtils.getRemoteLines()
                val lines = pair.first
                val lineJsonStr = pair.second
                if(lines.size<=0 || "".equals(lineJsonStr)){
                    initLineConf()
                }else{
                    sharedPreferences.edit().putString("lineStr",lineJsonStr).apply()
                    initLineConf(lines)
                }
            }
        }).start()

    }

}



data class Line(
    val idx:Int,
    val name:String,
    val ip:String,
    var speed:String? = null,
    var isCheck:Boolean = false
)