package com.atguigu.yygh.order.mapper;


import com.atguigu.model.order.OrderInfo;
import com.atguigu.vo.order.OrderCountQueryVo;
import com.atguigu.vo.order.OrderCountVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * <p>
 * 订单表 Mapper 接口
 * </p>
 *
 * @author atguigu
 * @since 2022-04-27
 */
@Repository
public interface OrderInfoMapper extends BaseMapper<OrderInfo> {

    List<OrderCountVo> selectOrderCount(OrderCountQueryVo orderCountQueryVo);

}
