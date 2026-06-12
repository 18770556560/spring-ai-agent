package com.cc.springaiagent.agent;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 定义执行步骤的内容
 */
@EqualsAndHashCode(callSuper = true)
@Data
abstract class ReactAgent extends BaseAgent{

    @Override
    String step() {
        try {
            //先思考要不要执行动作
            Boolean ifAct=think();
            if(ifAct){
                return act();
            }else{
                return "思考完成，无需下一步";
            }
        } catch (Exception e) {
            throw new RuntimeException(String.format("Step error:【%s】", e.getMessage()));
        }
    }

    protected  abstract Boolean think();
    protected abstract String act();
}
