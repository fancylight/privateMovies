class changeDeal {
    constructor(eventJson){
        this.evenJson=eventJson;
    }
    /**
     * 根据objectOther给对应的部分添加监听器
     * {"enterInput": ["#", "click", "upDirPath"], "scanDir": ["#", "click", "scanDir"]}
     * 解释为:
     * #enterInput onclick执行objectOther.upDirPath
     * @param objectOther
     */
    addEventListenByJson1(objectOther) {
        $.each(this.evenJson, (k, v) => {
            var key = k;
            var v0 = v[0];
            var target = v0 + key;
            var v1 = v[1];
            var v2 = v[2];
            var v3 = v[3];
            var v4 = v[4];
            //设置
            if (v1) {
                $(target).each(function () {
                    $(this).on(v1, eval("objectOther."+v2));
                });
            }
            if (v3) {
                $(target).each(function () {
                    $(this).on(v3, eval("objectOther."+v4));
                })
            }

        })
    }
};

function urlTran(url) {
    // console.log(url.replace(" ", "%20"));
    return url.replace(/ /g, "%20");
}