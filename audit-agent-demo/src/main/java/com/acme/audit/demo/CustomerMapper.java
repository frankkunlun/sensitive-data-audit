package com.acme.audit.demo;
import org.apache.ibatis.annotations.*;
@Mapper public interface CustomerMapper {@Select("SELECT customer_id, customer_name, id_no, mobile FROM customer WHERE customer_id = #{id}")@Results({@Result(column="customer_id",property="customerId"),@Result(column="customer_name",property="customerName"),@Result(column="id_no",property="idNo"),@Result(column="mobile",property="mobile")})Customer find(@Param("id")long id);
 @Update("UPDATE customer SET mobile = #{mobile} WHERE customer_id = #{id}")int updateMobile(@Param("id")long id,@Param("mobile")String mobile);}
