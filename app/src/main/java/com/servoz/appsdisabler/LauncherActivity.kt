package com.servoz.appsdisabler

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.*
import android.view.View.GONE
import android.view.animation.TranslateAnimation
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.*
import androidx.core.view.isVisible
import com.servoz.appsdisabler.tools.Db
import com.servoz.appsdisabler.tools.RunCommand
import kotlinx.android.synthetic.main.launcher_layout.*
import kotlinx.android.synthetic.main.tag_name.view.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.util.*


val Int.dp: Int
   get() = (this * Resources.getSystem().displayMetrics.density).toInt()


class LauncherActivity : AppCompatActivity() {


    private var prefFile: String = "com.servoz.appsdisabler.prefs"
    private var prefs: SharedPreferences? = null
    private var currentTag=""
    private var tabsIds= arrayListOf<TextView>()
    private var textColor=0
    private var textColor2=0
    private var iconsColors=0
    private var itemColor=0
    private var baseColor=0
    private var alpha=50

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launcher_layout)
        prefs = getSharedPreferences(prefFile, 0)
        val objCmd= RunCommand()
        if(prefs!!.getString("LABELS","-1")=="-1")
            prefs!!.edit().putString("LABELS", "ON").apply()

        setTheme()
        EmptyViewMain.setOnClickListener { finish() }
        buttonLauncherMenu.setOnClickListener{showConfigMenu(buttonLauncherMenu, objCmd)}
        setColors()

    }

    override fun onResume() {
        val objCmd= RunCommand()
        if(prefs!!.getString("SHOW_TABS","")=="ON"){
            listTabs(objCmd,currentTag)
            newTab(objCmd)
            setInitialTab()
        }else
            linearLayoutTags.isVisible=false
        getApps(objCmd,prefs!!.getInt("my_apps_columns",6),currentTag)
        super.onResume()
    }

    private fun setTheme(){
        when(prefs!!.getString("THEME","")){
            "Dark" -> {
                setDefaultNightMode(MODE_NIGHT_YES)
            }
            "Light" -> {
                setDefaultNightMode(MODE_NIGHT_NO)
            }
        }
    }

    private fun setColors(){
        baseColor = if(prefs!!.getString("BG","")=="")
            Color.BLACK
        else
            Color.parseColor(prefs!!.getString("BG",""))
        textColor = if(prefs!!.getString("TEXT","")=="")
            getColor(R.color.design_default_color_on_primary)
        else
            Color.parseColor(prefs!!.getString("TEXT",""))
        textColor2 = if(prefs!!.getString("TEXT2","")=="")
            getColor(R.color.colorAppDisabled)
        else
            Color.parseColor(prefs!!.getString("TEXT2",""))
        iconsColors = if(prefs!!.getString("ICONS_THEME","")=="")
            getColor(R.color.colorIcons)
        else
            Color.parseColor(prefs!!.getString("ICONS_THEME",""))
        itemColor = if(prefs!!.getString("ITEM_SEL","")=="")
            getColor(R.color.colorAppSelected)
        else
            Color.parseColor(prefs!!.getString("ITEM_SEL",""))

        alpha = (prefs!!.getInt("ALPHA",50) / 100f * 255).toInt()
        linearLayoutMain.setBackgroundColor(baseColor)
        buttonLayout.setBackgroundColor(baseColor)
        linearLayoutMain.background.alpha=alpha
        buttonLayout.background.alpha=alpha

        buttonLauncherMenu.backgroundTintList = ColorStateList.valueOf(getColor(R.color.colorAppSelected))
        textViewAllApps.setColorFilter(iconsColors)
        textViewAddTab.setColorFilter(iconsColors)
        buttonLauncherMenu.setColorFilter(iconsColors)
        buttonLauncherConfig.setColorFilter(iconsColors)
        buttonLauncherEditApps.setColorFilter(iconsColors)
        buttonLauncherDisableAll.setColorFilter(iconsColors)
        buttonLauncherEnableAll.setColorFilter(iconsColors)
    }

    private fun listTabs(objCmd: RunCommand, cTag:String=""){
        var columns=0
        gridTitlesMyApps.removeAllViews()
        for (i in 0 until 10){
            if(prefs!!.getString("TAG_$i","")!=""){
                columns++
            }
        }
        if(cTag==""){
            textViewAllApps.setBackgroundColor(itemColor)
        }
        tabsIds= arrayListOf()
        for (i in 0 until 10){
            val tabI=prefs!!.getString("TAG_$i","")
            val tab=TextView(this)
            if(tabI!=""){
                val tagId="TAG_$i"
                tab.id=i
                tab.textSize=24F
                tab.text=tabI
                tab.setTypeface(null, Typeface.BOLD)
                tab.setPadding(0,10,0,10)
                tab.gravity=Gravity.CENTER_HORIZONTAL
                if(cTag!="" && cTag==tagId){
                    tab.setBackgroundColor(itemColor)
                }
                tab.layoutParams=GridLayout.LayoutParams(LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT)).apply {
                    columnSpec=GridLayout.spec(i, 1f)
                    setMargins(0,0,0,0)
                }
                tab.setOnClickListener {
                    changeTab(tagId,objCmd,tab)
                }
                tab.setOnLongClickListener {
                    showTagTitleMenu(tab,objCmd,tagId)
                    true
                }
                gridTitlesMyApps.addView(tab)
            }else{
                tab.text=""
            }
            tabsIds.add(tab)
        }
    }

    private fun newTab(objCmd: RunCommand){
        textViewAddTab.setOnClickListener {
            var v=true
            for (i in 0 until 10){
                if(prefs!!.getString("TAG_$i","")==""){
                    showTagName(textViewAddTab,i)
                    v=false
                    break
                }
            }
            if(v){
                Toast.makeText(this, getString(R.string.NoMoreTabs), Toast.LENGTH_SHORT).show()
            }
        }
        textViewAllApps.setOnClickListener {
            changeTab("",objCmd,textViewAllApps)
        }
    }

    private fun setInitialTab(){
        currentTag=prefs!!.getString("CURRENT_TAG","")!!
        if(currentTag!=""){
            for (i in 0 until 10){
                //check TAG_# exits
                if(tabsIds[i].text!="" && prefs!!.getString(currentTag,"")==tabsIds[i].text){
                    setTitlesColors(tabsIds[i])
                    break
                }
            }
        }else
            setTitlesColors(textViewAllApps)
    }

    private fun showTagName(view: View, i:Int, rename:Boolean=false){
        val passWindow=PopupWindow(this)
        val windowView=LayoutInflater.from(this).inflate(R.layout.tag_name, gridMyApps, false)
        passWindow.contentView=windowView
        passWindow.isFocusable=true
        passWindow.width=(resources.displayMetrics.widthPixels-10.dp)/2
        passWindow.showAsDropDown(view)
        windowView.create_tag_cancel.setOnClickListener{
            passWindow.dismiss()
        }
        windowView.create_tag_save.setOnClickListener {
            val tagName=windowView.editText_tag_name.text.toString().trim()
            var createOk=true
            if(tagName!=""){
                for (pos in 0 until 10){
                    //check unique name, if renaming check no same position
                    if(prefs!!.getString("TAG_$pos","")==tagName && (!rename || pos!=i)){
                        createOk=false
                    }
                }
                if(createOk){
                    prefs!!.edit().putString("TAG_$i", tagName).apply()
                    passWindow.dismiss()
                    recreate()
                }
                else
                    Toast.makeText(this, getString(R.string.DuplicatedName), Toast.LENGTH_SHORT).show()
            }else
                Toast.makeText(this, getString(R.string.WrongName), Toast.LENGTH_SHORT).show()
        }
    }

    private fun changeTab(tagId:String, objCmd:RunCommand, tab:View){
        currentTag=tagId
        prefs!!.edit().putString("CURRENT_TAG", currentTag).apply()
        setTitlesColors(tab)
        getApps(objCmd,prefs!!.getInt("my_apps_columns",6),tagId)
    }

    private fun setTitlesColors(tab:View){
        textViewAllApps.background=gridTitlesMyApps.background
        if(tab==textViewAllApps)
            textViewAllApps.setBackgroundColor(itemColor)
        for (tabTile in tabsIds){
            if(tabTile.text=="")
                continue
            if(tab!=tabTile)
                tabTile.background=gridTitlesMyApps.background
            else{
                tab.setBackgroundColor(itemColor)
            }
        }
    }

    private fun getApps(objCmd:RunCommand, cols:Int, tag:String=""){
        val dbHandler = Db(this, null)
        var col=0
        var row=0
        val apps=checkUninstalledApps(tag, dbHandler)
        apps.sortBy{it[1].toLowerCase(Locale.ROOT)}
        val columns=if (apps.count() in 1 until cols) apps.count() else cols
        gridMyApps.removeAllViews()
        gridMyApps.columnCount=columns
        val cellSize = (resources.displayMetrics.widthPixels-10.dp)/cols
        for((c, app) in apps.withIndex()){
            createAppView(tag, app, cellSize, objCmd, row, col, dbHandler, c)
            col++
            if(col >0 && col.rem(columns)==0){
                col=0
                row++
            }
        }
        setLauncherSize(cellSize)
    }

    private fun checkUninstalledApps(tag: String, dbHandler: Db):ArrayList<ArrayList<String>>{
        val appsVal=dbHandler.getData("app", if(tag!="")"`tag` like '%|$tag|%'" else "")
        var valDel = false
        for(app in appsVal) {
            try {
                val pm = packageManager
                pm.getPackageInfo(app[0], PackageManager.GET_ACTIVITIES)
            } catch (e: PackageManager.NameNotFoundException) {
                dbHandler.deleteById("app",app[0])
                valDel=true
            }
        }
        return if(valDel)dbHandler.getData("app", if(tag!="")"`tag`like'%|$tag|%'" else "") else appsVal
    }

    private fun createAppView(tag:String, app: ArrayList<String>, cellSize: Int, objCmd: RunCommand, row:Int, col:Int, dbHandler: Db, c:Int){
        doAsync {
            val ll = LinearLayout(this@LauncherActivity)
            ll.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            ll.orientation = LinearLayout.VERTICAL

            val img = ImageView(this@LauncherActivity)
            img.setImageDrawable(packageManager.getApplicationIcon(app[0]))
            val imgSize=if(cellSize>80) 80 else cellSize
            img.layoutParams=LinearLayout.LayoutParams(imgSize, imgSize).apply {
                setMargins(0,10,0,10)
            }
            img.setPadding(0,0,0,0)

            val text = TextView(this@LauncherActivity)
            val dataTitle= if (app[1].length> 10) app[1].substring(0,10)+".." else app[1]
            if(prefs!!.getString("LABELS","")=="ON"){
                if(tag=="" && (app[3]=="||" || app[3]==""))
                    text.paintFlags= Paint.UNDERLINE_TEXT_FLAG
                text.text= dataTitle
                text.id= c
                text.setTextColor( if(packageManager.getApplicationInfo(app[0], 0).enabled)textColor else textColor2)
                text.maxLines=1
                text.layoutParams=LinearLayout.LayoutParams(
                    cellSize,LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                    setMargins(0,0,0,10)
                }
                text.gravity=Gravity.CENTER_HORIZONTAL
                text.setTypeface(null, Typeface.BOLD)
                text.setPadding(0,0,0,0)
            }
            ll.addView(img)
            ll.addView(text)
            ll.gravity=Gravity.CENTER
            ll.setPadding(0,0,0,0)
            ll.layoutParams=GridLayout.LayoutParams(ViewGroup.LayoutParams(cellSize, GridLayout.LayoutParams.WRAP_CONTENT)).apply {
                columnSpec=GridLayout.spec(col)
                rowSpec=GridLayout.spec(row)
                setMargins(0,0,0,0)
            }
            uiThread {
                gridMyApps.addView(ll)
                ll.setOnClickListener {
                    launchApp(objCmd, app)
                    text.setTextColor(textColor)
                }
                //menu
                ll.setOnLongClickListener {
                    showAppMenu(ll, objCmd, dbHandler, app, text)
                    true
                }

            }
        }
    }

    private fun setLauncherSize(cellSize:Int){
        val screenSize = resources.displayMetrics.heightPixels-150
        when(prefs!!.getString("HEIGHT","HALF")){
            "FULL" ->{
                EmptyViewMain.visibility=GONE
                mainMyAppsLayout.setBackgroundColor(baseColor)
                mainMyAppsLayout.background.alpha=alpha
                linearLayoutMain.setBackgroundColor(baseColor)
                linearLayoutMain.background.alpha=0
            }
            "HIGH" ->{ linearLayoutMain.layoutParams.height=(screenSize*0.75).toInt() }
            "BOTTOM" ->{ linearLayoutMain.layoutParams.height=(cellSize*2.4).toInt() }
            "MIN" -> {
                linearLayoutMain.layoutParams.height=LinearLayout.LayoutParams.WRAP_CONTENT
            }
            else -> {linearLayoutMain.layoutParams.height=(screenSize*0.5).toInt()}
        }
    }

    private fun launchApp(objCmd:RunCommand, app: ArrayList<String>) {
        objCmd.enableApp(baseContext, app)
        try{
            val intent: Intent = packageManager.getLaunchIntentForPackage(app[0])!!
            startActivity(intent)
        }catch (ex:KotlinNullPointerException){
            Toast.makeText(this, getString(R.string.root_needed), Toast.LENGTH_LONG).show()
        }
    }

    private fun changeAll(objCmd:RunCommand, enable:Boolean=false,tag:String=""){
        val dbHandler = Db(this, null)
        val apps=dbHandler.getData("app", if(tag!="")"`tag` like '%|$tag|%'" else "")
        apps.sortBy{it[1].toLowerCase(Locale.ROOT)}
        for((c,app) in apps.withIndex()){
            if(app[2] == "1")
                continue
            objCmd.sudoForResult("pm ${if (enable) "enable" else "disable" } ${app[0]}")
            try {
                findViewById<TextView>(c).setTextColor(if (enable) textColor else textColor2)
            }catch (ex:NullPointerException){}
        }
    }

    private fun showConfigMenu(view: View, objCmd: RunCommand) {
        animateMenu()

        buttonLauncherConfig.setOnClickListener {
            finish()
            val intent = Intent(this, AppsActivity::class.java)
            intent.putExtra("CONFIG", "1")
            startActivity(intent)
        }
        buttonLauncherEditApps.setOnClickListener {
            finish()
            val intent = Intent(this, AppsActivity::class.java)
            intent.putExtra("TAG", currentTag)
            startActivity(intent)
        }
        buttonLauncherDisableAll.setOnClickListener {
            doAsync {
                changeAll(objCmd)
                uiThread {
                    Toast.makeText(this@LauncherActivity, getString(R.string.AllAppsDisabled), Toast.LENGTH_SHORT).show()
                }
            }
            animateMenu()
        }
        buttonLauncherEnableAll.setOnClickListener {
            doAsync {
                changeAll(objCmd, true)
                uiThread {
                    Toast.makeText(this@LauncherActivity, getString(R.string.AllAppsEnabled), Toast.LENGTH_SHORT).show()
                }
            }
        }

    }

    private fun animateMenu(){
        val p1:Float
        val p2:Float
        if(!configLayout.isVisible){
            p1=configLayout.width.toFloat()
            p2=0f
            configLayout.isVisible=true
            buttonLauncherMenu.setImageDrawable(getDrawable(R.drawable.ic_arrow_right))
        }
        else{
            p1=0f
            p2=configLayout.width.toFloat()
            configLayout.isVisible=false
            buttonLauncherMenu.setImageDrawable(getDrawable(R.drawable.ic_arrow_left))
        }
        val animate = TranslateAnimation(p1, p2, 0f, 0f)
        animate.duration = 500
        //animate.fillAfter = true
        configLayout.startAnimation(animate)
    }

    private fun showAppMenu(view: View, objCmd: RunCommand, dbHandler:Db, app: ArrayList<String>, text:TextView) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.menu_app)
        if(app[2]!="1")
            popup.menu.findItem(R.id.menuAddApp).isVisible=false
        var hideMenu=true
        val menus= arrayListOf(R.id.tab0,R.id.tab1,R.id.tab2,R.id.tab3,R.id.tab4,R.id.tab5,R.id.tab6,R.id.tab7,R.id.tab8,R.id.tab9)
        for (i in 0 until 10){
            val tag=prefs!!.getString("TAG_$i","")
            val tagId="|TAG_$i|"
            val mm = popup.menu.findItem(menus[i])
            if(tag!=""){
                if(!app[3].isBlank() && app[3].contains(tagId)){
                    mm.title="$tag ✔"
                }else
                     mm.title = tag
                mm.setOnMenuItemClickListener {
                    val tags:String
                    val msg:String
                    if(!app[3].isBlank() && app[3].contains(tagId)){
                        tags=app[3].replace(tagId,"")
                        msg=getString(R.string.RemovedToTag, tag)
                    }else{
                        tags=app[3]+tagId
                        msg=getString(R.string.AddedToTag, tag)
                    }
                    recreate()
                    dbHandler.editData("app","`id`='${app[0]}'", hashMapOf("`tag`" to tags) )
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                    true
                }
                hideMenu=false
            }else
                mm.isVisible=false
        }
        if(hideMenu)
            popup.menu.findItem(R.id.menuTag).isVisible=false

        popup.setOnMenuItemClickListener{ item: MenuItem? ->
            when (item!!.itemId) {
                R.id.menuRemoveApp -> {
                    objCmd.enableApp(baseContext,app)
                    dbHandler.deleteById("app", app[0])
                    recreate()
                }
                R.id.menuAddApp -> {
                    objCmd.disableApp(baseContext,app)
                    dbHandler.addData("app", hashMapOf("id" to app[0], "name" to app[1]))
                    text.setTextColor(textColor2)
                }
                R.id.menuEnableApp -> {
                    objCmd.enableApp(baseContext, app)
                    text.setTextColor(textColor)
                }
                R.id.menuDisableApp -> {
                    objCmd.disableApp(baseContext, app)
                    text.setTextColor(textColor2)
                }
            }
            true
        }
        popup.show()
    }

    private fun showTagTitleMenu(view: View, objCmd: RunCommand,tagId:String) {
        val popupConfig = PopupMenu(this, view)
        popupConfig.inflate(R.menu.menu_tag_title)
        popupConfig.setOnMenuItemClickListener{ item: MenuItem? ->
            when (item!!.itemId) {
                R.id.menuTagRename -> {
                    showTagName(textViewAddTab,Integer.parseInt(tagId.replace("TAG_","")),true)
                }
                R.id.menuTagDelete -> {
                    val dbHandler = Db(this, null)
                    doAsync {
                        prefs!!.edit().remove(tagId).apply()
                        println("remove!!!!")
                        dbHandler.replaceFieldData("`app`","`tag`", "|$tagId|", "" )
                    }
                    prefs!!.edit().remove(tagId).apply()
                    prefs!!.edit().putString("CURRENT_TAG", "").apply()
                    Toast.makeText(this, tagId, Toast.LENGTH_SHORT).show()
                    recreate()
                }
                R.id.menuTagDisable -> {
                    changeAll(objCmd, tag=tagId)
                }
            }
            true
        }
        popupConfig.show()
    }

}
