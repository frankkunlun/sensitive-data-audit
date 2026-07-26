package com.acme.audit.demo;
import org.springframework.http.ResponseEntity;import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/customers") public class CustomerController {private final CustomerMapper mapper;public CustomerController(CustomerMapper mapper){this.mapper=mapper;}
 @GetMapping("/{id}")public ResponseEntity<Customer> find(@PathVariable long id){Customer c=mapper.find(id);return c==null?ResponseEntity.notFound().build():ResponseEntity.ok(c);}
 @PutMapping("/{id}/mobile")public int update(@PathVariable long id,@RequestBody MobileUpdate body){return mapper.updateMobile(id,body.getMobile());}
 public static class MobileUpdate{private String mobile;public String getMobile(){return mobile;}public void setMobile(String mobile){this.mobile=mobile;}}
}
