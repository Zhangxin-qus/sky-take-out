package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
    // 一般查询套餐是“根据分类查套餐列表”，缓存的就是 列表。后续新增的数据无法插入此类列表中，所以全删了再重新加载
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
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
    @CacheEvict(cacheNames = "setmealCache", allEntries = true) // 清除所有的缓存数据
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
    @PostMapping("/status/{status}")
    @ApiOperation("启售停售套餐")
    /*
        如果某套餐停售了 → 它不应该再出现在缓存的套餐列表里
        如果某套餐起售了 → 它需要被加进套餐列表
        这都会影响到该分类下的套餐列表缓存。
        但这里不是删除该分类下的套餐列表，而是所有分类下的，
        原因在于要根据套餐id查询出分类id，再删除指定的分类列表缓存，过于不便
    */
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    //                                                       使用setmealId获取请求参数中的 id
    public Result startOrStop(@PathVariable Integer status, @RequestParam("id") Long setmealId){
        log.info("启售停售套餐：{}，id：{}", status, setmealId);
        setmealService.startOrStop(status, setmealId);
        return Result.success();
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询套餐")
    public Result<SetmealVO> getById(@PathVariable Long id){
        log.info("根据id查询套餐:{}", id);
        SetmealVO setmealVO = setmealService.getById(id);
        return Result.success(setmealVO);
    }

    /**
     * 修改套餐
     * @param setmealDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改套餐")
    // 修改套餐可能会修改到套餐分类，会影响到多条数据，所以直接删除所有缓存
    @CacheEvict(cacheNames = "setmealCache", allEntries = true)
    public Result update(@RequestBody SetmealDTO setmealDTO){
        log.info("修改套餐: {}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }


}
