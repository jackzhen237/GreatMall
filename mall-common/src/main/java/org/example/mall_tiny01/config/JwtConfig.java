package org.example.mall_tiny01.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Date;

@Configuration
public class JwtConfig
{
    // 加长了末尾，确保超过 64 个字符
    public String secret = "sicvnfgoqw135dn67954asef243895hbgropqphfho1312bvc563oqh23kvb3467hk8sb123gknm54kf9abcdef";
    //先定义一个伸工程jwt令牌的方法
     public String getToken(String username)
     {
         //生成jwt令牌，secret是yml文件里面的，将username放入令牌中
          return Jwts.builder().setSubject(username)
                  //有效时间为三小时
                  .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*3))
                  .signWith(SignatureAlgorithm.HS512, secret)
                  .compact();
     }

     //验证jwt令牌
     public void validateToken(String token){
         Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
     }

     //读取jwt令牌中的数据
     public String getUserName(String token){
         return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody().getSubject();
     }

}
