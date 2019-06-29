class DynamicGen {

    constructor() {
        var arr = {};//数据
    }

//生成主页图片Div
    genIndexMovie(d) {
        dy.arr = d;
        dy.genIndexMovie0();
    }

    genIndexMovie0() {
        $(dy.arr).each(function (i, e) {
            var $node = $("<div class='contentInnerDiv' style='text-align: center;'></div>");
            $("#contentDiv").append($node);
            //图片位置
            var url = "url(" + urlTran(e["picPath"]) + ")";
            $node.append($("<div class='contentInnerDivImage' style='height: 75%;background:no-repeat " + url + ";background-size:contain'></div>"));
            $node.append($("<span class='navWord' style='color: #e3e3e3'>" + e["name"] + "</span>"));
            //跳转位置
            var movieUrl = window.location.href + "movies.html?moduleName=" + e["dataPath"];
            $node.append($("<span class='jsonPath' style='display: none'>" + movieUrl + "</span>"));
            $node.on("click", dy.jumpMovies)
        });
        dy.adjustByContent(document.body.clientWidth);
    }

    /**
     * 电影页面 movies.html
     * @param d
     */

    genMovies(d) {
        dy.arr = d;
        dy.genMovies0(1);
    }

    /**
     * 每页最多显示20张
     * @param page 表示当前页码
     */
    genMovies0(page) {
        //首先清除元素
        $(".contentInnerDiv").remove();
        $(".pageButton").remove();
        var start = 20 * (page - 1);
        var last = start + 20;
        for (var index = start; index < dy.arr.length && index < last; index++) {
            console.log(123);
            var e = dy.arr[index];
            var $node = $("<div class='contentInnerDiv' style='text-align: center;'></div>");
            $("#contentDiv").append($node);
            //图片位置
            var url = "url(" + urlTran(e["picPath"]) + ")";
            var imgNode = $("<div class='contentInnerDivImage' style='height: 75%;background:no-repeat " + url + ";background-size:contain'></div>");
            //跳转url
            var url = "http://" + window.location.host + "/movie.html?movieName=" + e["name"];
            var jumpNode = $("<span class='movieJumpUrl' style='display: none'>" + url + "</span>");
            imgNode.append(jumpNode);
            $node.append(imgNode);
            $node.append($("<span class='navWord' style='color: #e3e3e3'>" + e["name"] + "</span>"));
            //file位置
            var movieFilePath = e["filePath"];
            $node.append($("<span class='moviePath' style='display: none'>" + movieFilePath + "</span>"));

            imgNode.on("click", dy.detailJump);
        }
        //根据具体数量和当前页面生成正确的指示
        var pageDiv = $("#page");
        var current = $("<button  type='button' class='btn btn-primary pageButton' id='currentPage' style='color: red'>" + page + "</button>");
        var pre = null;
        var next = null;
        if (page > 1)
            pre = $(`<button type="button" class="btn btn-primary pageButton">上一页</button>`);
        var max = dy.arr.length;
        var remain = 0;
        if (max + 1 - last > 0)
            remain = Math.ceil((max + 1 - last) / 20);
        if (remain > 0)
            next = $(`<button type="button" class="btn btn-primary pageButton">下一页</button>`);
        $(pageDiv).append(pre);
        $(pageDiv).append(current);
        ;
        for (var index = page + 1; index <= remain + page && index < page + 4; index++) {
            $(pageDiv).append(`<button  type="button" class="btn btn-primary pageButton">${index}</button>`);
        }
        $(pageDiv).append(next);
        $(".pageButton").on("click", dy.pagefun);
        dy.adjustByContent(document.body.clientWidth);
    }

    pagefun() {
        var target = $(this).text();
        var now = $("#currentPage").text();
        if (target != "上一页" && target != "下一页") {
            dy.genMovies0(parseInt(target));
        } else if (target == "上一页") {
            dy.genMovies0(parseInt(now) - 1);
        } else if (target == "下一页") {
            dy.genMovies0(parseInt(now) + 1);
        }
    }


    /**
     * 详情页面
     */
    genMovie(d) {
        dy.arr = d;
        dy.genMovie0()
    }

    genMovie0() {
        $(dy.arr).each(function (i, e) {
            debugger;
            var $node = $("<div class='contentInnerDiv col-9' style='text-align: center;'></div>");
            $("#contentDiv").append($node);
            //图片位置
            var url = "url(" + urlTran(e["picPath"]) + ")";
            var conInnerNode = $("<div class='contentInnerDivImage' style='height: 75%;background:no-repeat " + url + ";background-size:contain'></div>");
            $node.append(conInnerNode);
            $node.append($("<span class='navWord ' style='color: #e3e3e3'>" + e["name"] + "</span>"));
            //file位置
            var movieFilePath = e["filePath"];//实际位置
            //todo:字幕处理
            var srtPath = e["subs"]; //字幕
            console.log(srtPath);
            var actors = e["actor"];//演员
            $node.append($("<span class='moviePath' style='display: none'>" + movieFilePath + "</span>"));
            $.each(srtPath, function (index, ele) {
                $node.append($("<span class='srtPath' style='display: none'>" + ele + "</span>"));
            });
            //todo:分p处理
            var parts = e["parts"];
            var partsNav = $("<div class='dropdown '><button class='btn btn-secondary dropdown-toggle'" +
                " type='button' id='dropdownMenuButton' data-toggle='dropdown' " +
                "aria-expanded='true'>分p</button></div>");
            var dropdown = $("<ul class=\"dropdown-menu\" aria-labelledby=\"dropdownMenuButton\"></ul>");
            $(partsNav).append(dropdown);
            $.each(parts, function (index, ele) {
                $(dropdown).append($("<li class='dropdown-item' onclick='dy.changePartMovie(this)'>" + ele + "</li>"));
            });
            $(dropdown).append($("<li class='dropdown-item' onclick='dy.changePartMovie(this)'>" + movieFilePath.substring(movieFilePath.lastIndexOf("/") + 1) + "</li>"));
            //其他信息
            dy.adjustByContentFiexd("col-9");//设置宽高
            //设计一个右键或者直接左键打开本地播放器
            conInnerNode.on("click", dy.playMovie);
            //网页浏览器
            var $node1 = $("<div class='contentInnerFill col-3' style='text-align: center;'></div>");
            var $node3 = $("<div class='contentInnerFillNoUse col-3' style='text-align: center;'></div>");
            $("#contentDiv").append($node3);//填充部分
            $("#contentDiv").append($node1);
            var $node2 = $("<div class='contentInnerDown col-9 row' style='text-align: center; height: 20%'></div>");
            var button = $("<button type=\"button\" class=\"btn btn-primary\">网页播放</button>");
            button.on("click", dy.showMovieWeb);
            $node1.append(button);
            //打开本地文件浏览器
            var buttonLocal = $("<button type=\"button\" class=\"btn btn-primary\">打开本地</button>");
            buttonLocal.on("click", dy.openLocalDir);
            $node1.append(buttonLocal);
            var buttonDelete = $("<button type=\"button\" class=\"btn btn-primary\">删除</button>");
            buttonDelete.on("click", dy.deleteLocal);
            $node1.append(buttonDelete);
            var buttonFavorite = $("<button type=\"button\" class=\"btn btn-primary\">最爱</button>");
            buttonFavorite.on("click", dy.favorite);
            $node1.append(buttonFavorite);
            //添加分p位置
            if (parts != null && parts.length != 0)
            //FIXME:此处存在不可修复的错误,原因是bootstrap 下拉将元素定位成fixed按理应该是absolute
                $node1.append(partsNav);
            //演员
            var host = window.location.host;
            if (actors != undefined && actors.length > 0) {
                for (var index = 0; index < actors.length; index++) {
                    //演员位置
                    var url = "url(" + urlTran(actors[index].picPath) + ")";
                    var divNode = $("<div class='actorDiv col-1' style='height: 100%;background:no-repeat " + url + ";background-size:contain'></div>");
                    var name = actors[index].name;
                    var nameNode = $("<span class='navWord ' style='color: #e3e3e3'>" + name + "</span>");
                    var actorUrl = 'http://"+ host+"/movies.html?dataTpe=actor&data=' + name;
                    //$("<span class='actorMovie' style='display: none'>" +actorUrl+ "</span>")
                    var jumpUrl = (`<span class='actorMovie' style='display: none'>http://${window.location.host}/movies.html?dataType=actor&data=${name}</span>`);
                    divNode.append(jumpUrl);
                    $node2.append(divNode);
                    divNode.on("click", dy.tagMovieJump);
                    divNode.append(nameNode);
                }
            }
            $("#contentDiv").append($node2);
        });
        dy.adjustByContentFiexd();
    }

    //排序,并分别调用对应的页面生成函数
    sortAndReGen(tag) { //默认实现按照内部是能够直接比较的数据,如数字,字符串,按照降序排列
        debugger;
        dy.arr.sort((i, i2) => {
            debugger;
            var d, d2;
            var desc = false;
            if (tag == "time") {
                d = parseFloat(i[tag]);
                d2 = parseFloat(i2[tag]);
            }
            else if (tag = "name") {
                d = i[tag];
                d2 = i2[tag];
                desc = true;
            }
            else
                return 0;
            if (desc) {
                var temp = d2;
                d2 = d;
                d = temp;
            }
            //默认降序
            if (d <= d2)
                return 1;
            else
                return -1;
        });
        console.log("重新排序");
        ;
        var now = parseInt($("#currentPage").text());
        dy.genMovies0(now);
    }

    /**
     * 删除
     */
    deleteLocal() {
        var movieName = $($(".moviePath")[0]).text();
        movieName = movieName.substring(movieName.lastIndexOf("/") + 1);
        const url = `http://${window.location.host}/deleteMovie/${movieName}`;
        $.ajax({
            url: url
        });
    }

    favorite() {
        var name = $(".contentInnerDiv span:first").text();
        var url = `http://${window.location.host}/favorite?movieName=${name}`;
        $.ajax({
            url: url,
            success: function (data) {
                console.log(name + "设置" + data);
            }
        })
    }

    //打开本地文件夹
    openLocalDir() {
        var path = $($(".moviePath")[0]).text();
        var url = `http://${window.location.host}/openDir${path.substring(0, path.lastIndexOf("/"))}`;
        $.ajax({
            url: url,
            method: "get"
        })
    }

    //在当前页面播放
    showMovieWeb() {
        var node = $(".contentInnerDivImage ")[0];
        var path = ($($(".moviePath")[0])).text();
        //todo:改为多个字幕
        var srt = ($($(".srtPath")));
        $("head title").text(path.substring(path.lastIndexOf("/") + 1));
        //处理为相对路径
        // path= path.substring(path.indexOf("/"));
        path = "http://" + window.location.host + path;
        var srts = "";
        $.each(srt, function (index, ele) {
            if (index == 0)
                srts += `<track srclang="zh" default label="${index}" kind="subtitles" src="http://${window.location.host}:/sub${$(ele).text()}"/>`
            else
                srts += `<track srclang="zh" label="${index}" kind="subtitles" src="http://${window.location.host}:/sub${$(ele).text()}"/>`
        });
        // srt = "http://" + window.location.host + srt;

        var maxWidth = document.body.clientWidth;
        var maxHeight = document.body.clientHeight;
        var width = "640px";
        if (maxWidth > maxHeight) { //手机
            width = maxWidth / 5 * 2 + "px";
        }
        $(node).empty();

        var video = $("<video src='" + path + "' controls='controls' style='width:" + width + "'>" +
            // "<track kind='subtitles' src='" + srt + "' label='x' default>" +
            srts +
            "</video>");

        $(node).append(video);
        video.on("canplay", function (e) {
            console.log(e);
            console.log("播放就绪");
            this.play();
        })
        video.on("click", function () {
            console.log(12312);
        })
    }

    //跳转到movies页面
    jumpMovies() {
        var url = ($($(this).children(".jsonPath")[0])).text();
        window.location.href = url;
    }

    //跳转到movie页面
    detailJump() {
        var url = ($($(this).children(".movieJumpUrl")[0])).text();
        window.open(url);
    }

    //处理跳转至特征页面
    tagMovieJump() {
        var url = ($($(this).children(".actorMovie")[0])).text();
        url = url.replace("amp;", "");
        window.location.href = url;

    }

    playMovie(filePath) {
        if ((navigator.userAgent.match(/(phone|pad|pod|iPhone|iPod|ios|iPad|Android|Mobile|BlackBerry|IEMobile|MQQBrowser|JUC|Fennec|wOSBrowser|BrowserNG|WebOS|Symbian|Windows Phone)/i))) {
            return;
        }
        filePath = ($($(this).parent().children(".moviePath")[0])).text();
        // ajax("showMovies", "", "moviePath=" + filePath);
        $.ajax({
            url: "showMovies?movieName=" + filePath,
            method: "get"
        })
    }

    //改变 分p信息
    changePartMovie(node) {
        var part = $(node).text();
        var now = $($(".moviePath")[0]).text();
        var newPath = now.substring(0, now.lastIndexOf("/") + 1) + part;
        $($(".moviePath")[0]).text(newPath);
    }

    /**
     * 一行最多显示6个
     * 当页面小于1300后如果大于6个就换行
     *    4: col-3
     *    3: col-4
     *    2: col-6
     * 还要计算每个区域的高
     */
    removeClass(_this, reg) {
        var cla;
        var classes = $(_this).attr("class").split(" ");
        $.each(classes, function (i, e) {
            if (e.match(reg))
                cla = e;
        });
        if (cla) {
            $(_this).removeClass(cla);
        }
    }

    adjustByContent(clientWidth) {
        // alert(clientWidth);
        var col = "col-2"; //6
        if (clientWidth <= 1200) {
            col = "col-3"  //4
        }
        if (clientWidth <= 880) {
            col = "col-4"  //3
        }
        if (clientWidth <= 500) {
            col = "col-6"
        }
        $(".contentInnerDiv").each(function () {
            dy.removeClass(this, /col-[0,12]{0,1}/);
            $(this).addClass(col);
            console.log($(this).attr("class"));
            var outWidth = $(this).width();
            $(this).height(outWidth);
        })
        //继续调整其他元素的问题,可以理解为使用的col的基本都有问题,问题出在我没有使用bootstrap的自适应标签
        //简单处理解决对于 S8: 360*740 : 1440*2990的情况

        this.s8(clientWidth);
    }

    /**
     * 非常粗暴的方式
     */
    s8(clientWidth) {
        if (clientWidth < 370) {
            dy.removeClass($("#topDivLeft"), /col-[0,12]{0,1}/);
            dy.removeClass($("#topDivRight"), /col-[0,12]{0,1}/);
            dy.removeClass($(".input-group-prepend"), /col-[0,12]{0,1}/);
            dy.removeClass($("#inputGroupFile01"), /col-[0,12]{0,1}/);
            dy.removeClass($("#inputGroupFile01"), /offset-[0,12]{0,1}/);
            $("#inputGroupFile01").addClass("col-8");
            $("#topDivRight").addClass("col-9");
            $("#topDivLeft a").each(function () {
                dy.removeClass(this, /col-[0,12]{0,1}/);
                dy.removeClass(this, /offset-[0,12]{0,1}/);
            });
            if ($("div div h4").length > 0)
                dy.removeClass($("div div h4").parent(), /col-[0,12]{0,1}/);
        }
    }

    //TODO设置一个固定高宽,此处要修正一下,将图片 宽固定为浏览器窗口的2/3或者某个比例,高为1/2
    adjustByContentFiexd() {
        var clientWidth = document.body.clientWidth;
        var clientHeight = window.innerHeight;
        var visialHieght = clientHeight - 100;
        $(".contentInnerDiv").each(function () {
            //设置背景div本身大小

            $(this).height(visialHieght * 0.75);
            if (clientWidth < 370)
                $(this).height(visialHieght * 0.45);
            // var picHight=visialHieght*;
        });
        dy.s8(document.body.clientWidth);
    }

    genOnChangeWindow() {
        dy.adjustByContent(document.body.clientWidth);
    }

}

var dy = new DynamicGen();