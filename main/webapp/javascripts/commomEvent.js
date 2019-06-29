class Event {

    constructor() {
        this.changeColorJson = {
            "oi-star": ['.', "#52b54b", ""]
        };
    }

    /**
     * 当该事件触发时将对应的颜色转换成xx
     */
    changeColor() {
        //获取当前数据
        var array = xx.getClassAndId($(this));
        //获取当前颜色
        var pro = $(this).css("color");
        $.each(xx.changeColorJson, function (k, v) {
            v[2] = pro;
        });
        //改变颜色
        var $node = $(this)
        $.each(xx.changeColorJson, function (k, v) {
            var key = k;
            var color = v[1];
            if (array.indexOf(key)) {
                $node.css("color", color);
                return false;
            }

        })
    }

    //还原颜色
    reColor() {
        var $node = $(this);
        var array = xx.getClassAndId($(this));
        $.each(xx.changeColorJson, function (k, v) {
            var color = v[2];
            if (array.indexOf(k)) {
                $node.css("color", color);
                return false;
            }
        })
    }

    //获取节点的所有class以及id
    getClassAndId($node) {
        var reArray = [];
        if ($node.attr("id"))
            reArray.push($node.attr("id"));
        var classes = $node.attr("class");
        $.each(classes.split(" "), function (i, d) {
            reArray.push(d);
        });
        return reArray;

    }

    //展示侧栏
    showNavLeft() {
        $(".navLeft").each(function () {
            $(this).css("transform", "translateX(300px)");
        });

    }

    //关闭侧栏
    closeNavLeft() {
        $(".navLeft").each(function () {
            $(this).css("transform", "none");
        })
    }

    jumpToSearchMovie() {
        //[0]获取搜索关键词
        var text = $("#inputGroupFile01").val();
        window.location.href = "http://" + window.location.host + "/movies.html?type=all&data=" + text;
    }

    jumpToSearchMovieByEnter(e) {
        if (e.keyCode == 13) {
            xx.jumpToSearchMovie();
        }
    }

    //设置tag
    setTag() {
        var text = $("#inputGroupFile01").val();
        var movieName = $($("#contentDiv span")[0]).text();
        var url = `http://${window.location.host}/setTag?`;
        ajax(url, null, "tag=" + text + "&movieName=" + movieName);
    }

    //打开排序菜单
    sortMenu() {
        $(this).parent().children("ul").show();
        $("#sort").unbind("click", xx.sortMenu);
        $("#sort").on("click", xx.closeMenu);
    }

    closeMenu() {
        $("#sort").parent().children("ul").hide();
        $("#sort").unbind("click", xx.closeMenu);
        $("#sort").on("click", xx.sortMenu);

    }

    sort() {
        var tag = $(this).text();
        dy.sortAndReGen(tag);
    }

    playRandom() {
      var sum= dy.arr.length;
      function randon(min,max) {
          return Math.floor(Math.random()*(max-min+1)+min);
      }
      var ran=randon(0,sum-1);
      var target=dy.arr[ran];
      window.location.href=`http://${window.location.host}/movie.html?movieName=${target.name}`;
    }
}

var xx = new Event();