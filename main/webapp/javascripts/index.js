
class index {

}

window.onload = function () {
    //初始化事件
    var evenJson = {
        "oi-star": [".", "mouseover", "changeColor", "mouseout", "reColor"],
        "nav": ["#", "click", "showNavLeft"],
        "oi-home": [".", "click", "closeNavLeft"],
        "search": ["#", "click", "jumpToSearchMovie"],
        "inputGroupFile01": ["#", "keydown", "jumpToSearchMovieByEnter"]
    };
    new changeDeal(evenJson).addEventListenByJson1(xx);
    //设置动态处理
    window.onresize = dy.genOnChangeWindow;//设置动态调整监听
    //发送请求获取数据
    $.ajax({
        url: "/modulesData/all",
        method: "get",
        success: function (d) {
            dy.genIndexMovie(d);
        }
    })
};