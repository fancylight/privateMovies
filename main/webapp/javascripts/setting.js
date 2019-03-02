function ajaxUp(para) {
    $.ajax({
        url: "http://localhost:3000/"+para,
        success: function (d) {
            $("#tree").treeview({data: d});
        }
    });
    //
}

class Setting {
    upDirPath() {
        // p=p.substring(0,p.lastIndexOf("/"));
        ajaxUp("getSystemFile?"+"path="+setting.getPath());
    }

    scanDir() {
        debugger;
        var check = $("#tree").treeview("getSelected");
        var name=check[0].text;
        ajaxUp("scan?"+"path="+setting.getPath()+"&jsonName="+name);
    }
    getPath(){
        var tree = $("#tree");
        var check = tree.treeview("getSelected");
        var path = [];
        path.push(check[0].text);
        var p = "";
        while ($(check = tree.treeview("getParent", check)).attr("id") != "tree") {

            if (check.text != undefined)
                path.push(check.text);
            else if (check[0].text != undefined)
                path.push(check[0].text);
        }
        while (path.length > 0) {
            p += path.pop() + "/";
        }
        return p;
    }
}
var setting = new Setting();

window.onload = function () {
    ajaxUp("getSystemFile?"+"path="+"H:/");
    var tool = new changeDeal({"enterInput": ["#", "click", "upDirPath"], "scanDir": ["#", "click", "scanDir"]});
    tool.addEventListenByJson1(setting);
};
