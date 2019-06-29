class movieEvent {

}

class movie {
    //生成类别标签
    genTypeTag(types) {
        var pNode = $("#tagDiv");
        $(types["types"]).each(function (index, tag) {
            var aNode = $(`<a href="/movies.html?type=type&data=${tag}"><button type="button" class="btn btn-link">${tag}</button> </a>`);
            $(pNode).append(aNode);
        });

    }
}

var mm = new movie();
window.onload = function () {
    //初始化事件
    var evenJson = {
        "oi-star": [".", "mouseover", "changeColor", "mouseout", "reColor"],
        "nav": ["#", "click", "showNavLeft"],
        "oi-home": [".", "click", "closeNavLeft"],
        "search": ["#", "click", "jumpToSearchMovie"],
        "inputGroupFile01": ["#", "keydown", "jumpToSearchMovieByEnter"]
    };
    window.onresize = dy.adjustByContentFiexd;//设置动态调整监听
    var s = window.location.search;
    //如果是多个&连接先用&分割
    // var ss= s.split("=");
    s = s.substr(1);
    $.ajax({
        url: "getMovie?" + s,
        method: "get",
        success: function (d) {
            dy.genMovie(d);
            mm.genTypeTag(d["typeData"])
            //TODO:这是暂时解决下来问题的方案
            var x= $(".dropdown-menu")[0];
            $(x).css("display","block");
        }
    })
};