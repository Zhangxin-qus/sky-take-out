package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 新增套餐
     * @param setmealDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增套餐")
    public Result save(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐：{}", setmealDTO);
        setmealService.save(setmealDTO);

        return Result.success();
    }

    /**
     * 套餐分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("套餐分页查询")
    public Result<PageResult> page(SetmealPageQueryDTO setmealPageQueryDTO){
        log.info("套餐分页查询: {}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.page(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 批量删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("批量删除套餐")
    // RequestParam springMVC框架动态解析字符串集合
    public Result delete(@RequestParam List<Long> ids) {
        log.info("批量删除套餐：{}", ids);
        setmealService.delete(ids);
        return Result.success();
    }

    /**
     * 启售停售套餐
     * @param status
     * @return
     */
    @PostMapping("//status/{status}")
    @ApiOperation("启售停售套餐")
    //                                                       使用setmealId获取请求参数中的 id
    public Result startOrStop(@PathVariable Integer status, @RequestParam("id") Long setmealId){
        log.info("启售停售套餐：{}，id：{}", status, setmealId);
        setmealService.startOrStop(status, setmealId);

        return Result.success();
    }

}
