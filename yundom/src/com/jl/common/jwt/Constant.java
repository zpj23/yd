package com.jl.common.jwt;/*
 * @Author chengpunan
 * @Description //TODO $end$
 * @Date $time$ $date$
 * @Param $param$
 * @return $return$
 **/

import java.util.UUID;

public class Constant {
    public static final String JWT_ID = UUID.randomUUID().toString();
    /**
     * 加密密文
     */
    public static final String JWT_SECRET = "woyebuzhidaoxiediansha";
    public static final int JWT_TTL = 60*60*1000*24*30;  //millisecond
}
