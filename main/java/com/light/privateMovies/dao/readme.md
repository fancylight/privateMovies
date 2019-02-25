#### hibernate
关于hibernate,将sql完全面向对象化,不去理会sql语句  
这里有几个问题,表之间有关系的情况,进行save等操作,若bean中未包含关系对象,则需要进行更新操作  
也就是说当且仅当,处理的bean含有关系bean时,自动更新关系表  
现在将关系设定为

- Movie_table 控制actor,movie_type,多对多关系更新
- 由movie_detail 控制 movie 多对一关系
- 由module 控制 movie_type 多对1关系
