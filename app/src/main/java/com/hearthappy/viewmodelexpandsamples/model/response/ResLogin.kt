package com.hearthappy.viewmodelexpandsamples.model.response

/**
 * Created Date 2020/11/30.
 *
 * @author ChenRui
 * ClassDescription:new login intface
 */
class ResLogin {
    /**
     * group_id : 6726339965796917248
     * header :
     * id : 6726346783147335680
     * is_initial : false
     * refresh_expires : 1606328248
     * refresh_token : eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ2ZXN5c3RlbSIsInN1YiI6InJlZnJlc2hfdG9rZW4iLCJhdWQiOiJodHRwczovL3d3dy52ZXN5c3RlbS5jb20vIiwiZXhwIjoxNjA2MzI4MjQ4LCJuYmYiOjE2MDYzMDEyNDgsImlhdCI6MTYwNjI5OTQ0OCwianRpIjoiNjcyNjM0Njc4MzE0NzMzNTY4MCIsImlkIjoiNjcyNjM0Njc4MzE0NzMzNTY4MCIsInVzZXJuYW1lIjoidTEiLCJhY2Nlc3MiOnt9fQ.QmWbCnthrhExpdTcgyaOnBbHSq9lBpTlmeZEqq4UwehXFwncVvHSeeli3mPVAn_F713O2dg6YlVdQ9twMmfVPFsOMMheefFzjekUK13LayMnZwzG5-z-6QHr9uVmtR5zRTNEhF041s1cJPq5Segz2KoO_HXei7Z2M-FoqPOSC7zrqiHca-0KobtKA32kA8lpLgf7Rx_osoYDsULrFYFX8IN3uX4nW37empvFZOUPkv3M8RYRfoyAY5T30GuuK__rJfzT3Pj7Y5BV1GhMBhfDgYygRWCJ19h3KqFB9bWB04UALcQ31wzQ7xNHYas_JdGNFDEDX8mMG_zHL8hFDfovIg
     * token : eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJ2ZXN5c3RlbSIsInN1YiI6ImF1dGhfdG9rZW4iLCJhdWQiOiJodHRwczovL3d3dy52ZXN5c3RlbS5jb20vIiwiZXhwIjoxNjA2MzAzMDQ4LCJuYmYiOjE2MDYzMDEyNDgsImlhdCI6MTYwNjI5OTQ0OCwianRpIjoiNjcyNjM0Njc4MzE0NzMzNTY4MCIsImlkIjoiNjcyNjM0Njc4MzE0NzMzNTY4MCIsInVzZXJuYW1lIjoidTEiLCJhY2Nlc3MiOnt9fQ.jLzYaHKLM0BlOlubZCmzcIhLsSya8SysNaH7vNI84GapRuVZNoZSKCZB5Nz1rs8eaXs3G4WbhDevM7uYF1uDoLDXjyc-6JYsMUo2lP1Jx8g_lwB1dr9rzOaGVOb2F9jFoOxXfKA0pRknRSAW4AKuPqYhAeL0TYqMpY7gKngUuszrd5aBbTyWZA9_hxcVjRb3c5zPXpnSs7M2DVpcUV8fQSUE_9MOo0EqZ3l1JcEZr6NKFDLH06zv4LRcFLDDRQVZxk4k1PDYtnXB9F8XtM4E4p6Hxlr03yUKrIZPdG9SzJc65nEQi3I1JrA9o2PYX9TyC4JG3Y1-a9-zpTGrKABV3Q
     * token_expires : 1606303048
     */
    var group_id: String? = null
    var header: String? = null
    var id: String? = null
    var isIs_initial = false
        private set
    var refresh_expires = 0
    var refresh_token: String? = null
    var token: String? = null
    var token_expires = 0
    fun setIs_initial(is_initial: Boolean) {
        isIs_initial = is_initial
    }

    override fun toString(): String {
        return "ResLogin(group_id=$group_id, header=$header, id=$id, isIs_initial=$isIs_initial, refresh_expires=$refresh_expires, refresh_token=$refresh_token, token=$token, token_expires=$token_expires)"
    }

}