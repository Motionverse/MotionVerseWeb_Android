package com.app.testwebviewforlink.http

import org.json.JSONArray

open class HttpResponse @JvmOverloads constructor(
    var errorCode: String = "",
    var errorMsg: String = ""
) {
    var time = 0L

    init {
        time = System.currentTimeMillis()
    }
}

class ListCharacterBean(
    var data: MutableList<CharacterBean>
) : HttpResponse()

class CharacterBean(
    var abName: String = "",
    var img: String = "",
    var name: String = ""
) : HttpResponse()