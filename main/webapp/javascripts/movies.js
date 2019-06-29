class moviesEvnet {

}

class movies {

}

window.onload = function () {
    //初始化事件
    var evenJson = {
        "oi-star": [".", "mouseover", "changeColor", "mouseout", "reColor"],
        "nav": ["#", "click", "showNavLeft"],
        "oi-home": [".", "click", "closeNavLeft"],
        "search": ["#", "click", "jumpToSearchMovie"],
        "inputGroupFile01": ["#", "keydown", "jumpToSearchMovieByEnter"],
        "sort": ["#", "click", "sortMenu"],
        "sortLabel": [".", "click", "sort"],
        "randomPlay":["#","click","playRandom"]
    };
    new changeDeal(evenJson).addEventListenByJson1(xx);
    window.onresize = dy.genOnChangeWindow;//设置动态调整监听
    //获取请求参数
    var s = window.location.search;
    //如果是多个&连接先用&分割
    // var ss= s.split("=");
    s = s.substr(1);
    var ss = s.split("&");
    if (ss.length == 1) {
        $.ajax({
            url: "/getMovies?" + s,
            method: "get",
            success: function (d) {
                dy.genMovies(d);
            }
        })
    }
    else {
        var type = ss[0].split("=")[1];
        var data = ss[1].split("=")[1];
        var url="/getMoviesByType?type=" + type + "&data=" + data;
        $.ajax({
            url: url,
            method: "get",
            success: function (d) {
                console.log(d);
                dy.genMovies(d);
            }
        })
    }
};