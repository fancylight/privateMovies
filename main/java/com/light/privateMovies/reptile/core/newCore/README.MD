#### 关于新的爬虫框架使用
#### 原理
ConnectionBlock 和 TaskBlock 同时作为消费者和生产者线程
前者消费连接任务,创建处理任务
后者消费处理任务,然后根据任务的深度将权限abstarctDataResult的实现类去调用,用户要自行实现abstarctDataResult来
确定每次任务处理情况,并按照需求创建新的连接任务