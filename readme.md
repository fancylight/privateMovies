#### 说明
此项目是使用ssh重构之前nodeJs,目的是能够实现和EMBY大致的功能
##### 目前的功能
- 大致定向爬取电影数据的功能,有待改善
- 前端可以复用之前nodejs的
- 分卷使用partA...B
##### 问题
- 由于jdk不能指定remote dns,因此即使通过sock5代理依然无法完全访问
  -  解决方案:手动查询ip,并保存到hosts文件中,使用类似cloudflare的网站也可以如此
