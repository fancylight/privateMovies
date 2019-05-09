package com.light.privateMovies.reptile.core.newCore.datasub;

import com.light.privateMovies.reptile.core.newCore.ConnectionTarget;
import com.light.privateMovies.reptile.core.newCore.data.AbstractDataResult;
import com.light.privateMovies.reptile.core.newCore.data.StepTask;
import org.jsoup.Connection;
import java.io.IOException;

public class DataSub extends AbstractDataResult<String> {
    public DataSub(ConnectionTarget target) {
        super(target);
        this.addNewStepTask(new BIndex());
    }

    //测试百度首页
    class BIndex implements StepTask{

        @Override
        public void deal(Connection.Response response) {
            try {
                data=response.parse().title();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //停止
            stop();
        }
    }
}
