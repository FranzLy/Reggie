package com.franz.reggie.dto;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.franz.reggie.entity.SetMeal;
import com.franz.reggie.entity.SetMealDish;
import lombok.Data;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Data
public class SetMealDto extends SetMeal {

    //与前端变量名绑定
    @JsonProperty("setmealDishes")
    private List<SetMealDish> setMealDishes;

    private String categoryName;
}
