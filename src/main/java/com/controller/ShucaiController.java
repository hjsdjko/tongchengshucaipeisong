
package com.controller;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.text.SimpleDateFormat;
import com.alibaba.fastjson.JSONObject;
import java.util.*;
import org.springframework.beans.BeanUtils;
import javax.servlet.http.HttpServletRequest;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import com.service.TokenService;
import com.utils.*;
import java.lang.reflect.InvocationTargetException;

import com.service.DictionaryService;
import org.apache.commons.lang3.StringUtils;
import com.annotation.IgnoreAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.entity.*;
import com.entity.view.*;
import com.service.*;
import com.utils.PageUtils;
import com.utils.R;
import com.alibaba.fastjson.*;

/**
 * 蔬菜
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/shucai")
public class ShucaiController {
    private static final Logger logger = LoggerFactory.getLogger(ShucaiController.class);

    @Autowired
    private ShucaiService shucaiService;


    @Autowired
    private TokenService tokenService;
    @Autowired
    private DictionaryService dictionaryService;

    //级联表service

    @Autowired
    private YonghuService yonghuService;
    @Autowired
    private YuangongService yuangongService;
    @Autowired
    private JingliService jingliService;


    /**
    * 后端列表
    */
    @RequestMapping("/page")
    public R page(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("page方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));
        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永不会进入");
        else if("用户".equals(role))
            params.put("yonghuId",request.getSession().getAttribute("userId"));
        else if("员工".equals(role))
            params.put("yuangongId",request.getSession().getAttribute("userId"));
        else if("经理".equals(role))
            params.put("jingliId",request.getSession().getAttribute("userId"));
        params.put("shucaiDeleteStart",1);params.put("shucaiDeleteEnd",1);
        if(params.get("orderBy")==null || params.get("orderBy")==""){
            params.put("orderBy","id");
        }
        PageUtils page = shucaiService.queryPage(params);

        //字典表数据转换
        List<ShucaiView> list =(List<ShucaiView>)page.getList();
        for(ShucaiView c:list){
            //修改对应字典表字段
            dictionaryService.dictionaryConvert(c, request);
        }
        return R.ok().put("data", page);
    }

    /**
    * 后端详情
    */
    @RequestMapping("/info/{id}")
    public R info(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("info方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ShucaiEntity shucai = shucaiService.selectById(id);
        if(shucai !=null){
            //entity转view
            ShucaiView view = new ShucaiView();
            BeanUtils.copyProperties( shucai , view );//把实体数据重构到view中

            //修改对应字典表字段
            dictionaryService.dictionaryConvert(view, request);
            return R.ok().put("data", view);
        }else {
            return R.error(511,"查不到数据");
        }

    }

    /**
    * 后端保存
    */
    @RequestMapping("/save")
    public R save(@RequestBody ShucaiEntity shucai, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,shucai:{}",this.getClass().getName(),shucai.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");

        Wrapper<ShucaiEntity> queryWrapper = new EntityWrapper<ShucaiEntity>()
            .eq("shucai_name", shucai.getShucaiName())
            .eq("shucai_uuid_number", shucai.getShucaiUuidNumber())
            .eq("shucai_types", shucai.getShucaiTypes())
            .eq("shucai_kucun_number", shucai.getShucaiKucunNumber())
            .eq("shucai_price", shucai.getShucaiPrice())
            .eq("shucai_clicknum", shucai.getShucaiClicknum())
            .eq("shangxia_types", shucai.getShangxiaTypes())
            .eq("shucai_delete", shucai.getShucaiDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShucaiEntity shucaiEntity = shucaiService.selectOne(queryWrapper);
        if(shucaiEntity==null){
            shucai.setShucaiClicknum(1);
            shucai.setShangxiaTypes(1);
            shucai.setShucaiDelete(1);
            shucai.setCreateTime(new Date());
            shucaiService.insert(shucai);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody ShucaiEntity shucai, HttpServletRequest request){
        logger.debug("update方法:,,Controller:{},,shucai:{}",this.getClass().getName(),shucai.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
        //根据字段查询是否有相同数据
        Wrapper<ShucaiEntity> queryWrapper = new EntityWrapper<ShucaiEntity>()
            .notIn("id",shucai.getId())
            .andNew()
            .eq("shucai_name", shucai.getShucaiName())
            .eq("shucai_uuid_number", shucai.getShucaiUuidNumber())
            .eq("shucai_types", shucai.getShucaiTypes())
            .eq("shucai_kucun_number", shucai.getShucaiKucunNumber())
            .eq("shucai_price", shucai.getShucaiPrice())
            .eq("shucai_clicknum", shucai.getShucaiClicknum())
            .eq("shangxia_types", shucai.getShangxiaTypes())
            .eq("shucai_delete", shucai.getShucaiDelete())
            ;

        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShucaiEntity shucaiEntity = shucaiService.selectOne(queryWrapper);
        if("".equals(shucai.getShucaiPhoto()) || "null".equals(shucai.getShucaiPhoto())){
                shucai.setShucaiPhoto(null);
        }
        if(shucaiEntity==null){
            shucaiService.updateById(shucai);//根据id更新
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        ArrayList<ShucaiEntity> list = new ArrayList<>();
        for(Integer id:ids){
            ShucaiEntity shucaiEntity = new ShucaiEntity();
            shucaiEntity.setId(id);
            shucaiEntity.setShucaiDelete(2);
            list.add(shucaiEntity);
        }
        if(list != null && list.size() >0){
            shucaiService.updateBatchById(list);
        }
        return R.ok();
    }


    /**
     * 批量上传
     */
    @RequestMapping("/batchInsert")
    public R save( String fileName, HttpServletRequest request){
        logger.debug("batchInsert方法:,,Controller:{},,fileName:{}",this.getClass().getName(),fileName);
        Integer yonghuId = Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId")));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            List<ShucaiEntity> shucaiList = new ArrayList<>();//上传的东西
            Map<String, List<String>> seachFields= new HashMap<>();//要查询的字段
            Date date = new Date();
            int lastIndexOf = fileName.lastIndexOf(".");
            if(lastIndexOf == -1){
                return R.error(511,"该文件没有后缀");
            }else{
                String suffix = fileName.substring(lastIndexOf);
                if(!".xls".equals(suffix)){
                    return R.error(511,"只支持后缀为xls的excel文件");
                }else{
                    URL resource = this.getClass().getClassLoader().getResource("../../upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            ShucaiEntity shucaiEntity = new ShucaiEntity();
//                            shucaiEntity.setShucaiName(data.get(0));                    //蔬菜名称 要改的
//                            shucaiEntity.setShucaiUuidNumber(data.get(0));                    //蔬菜编号 要改的
//                            shucaiEntity.setShucaiPhoto("");//详情和图片
//                            shucaiEntity.setShucaiTypes(Integer.valueOf(data.get(0)));   //蔬菜类型 要改的
//                            shucaiEntity.setShucaiKucunNumber(Integer.valueOf(data.get(0)));   //蔬菜库存 要改的
//                            shucaiEntity.setShucaiPrice(Integer.valueOf(data.get(0)));   //购买获得积分 要改的
//                            shucaiEntity.setShucaiOldMoney(data.get(0));                    //蔬菜原价 要改的
//                            shucaiEntity.setShucaiNewMoney(data.get(0));                    //现价 要改的
//                            shucaiEntity.setShucaiClicknum(Integer.valueOf(data.get(0)));   //蔬菜热度 要改的
//                            shucaiEntity.setShucaiContent("");//详情和图片
//                            shucaiEntity.setShangxiaTypes(Integer.valueOf(data.get(0)));   //是否上架 要改的
//                            shucaiEntity.setShucaiDelete(1);//逻辑删除字段
//                            shucaiEntity.setCreateTime(date);//时间
                            shucaiList.add(shucaiEntity);


                            //把要查询是否重复的字段放入map中
                                //蔬菜编号
                                if(seachFields.containsKey("shucaiUuidNumber")){
                                    List<String> shucaiUuidNumber = seachFields.get("shucaiUuidNumber");
                                    shucaiUuidNumber.add(data.get(0));//要改的
                                }else{
                                    List<String> shucaiUuidNumber = new ArrayList<>();
                                    shucaiUuidNumber.add(data.get(0));//要改的
                                    seachFields.put("shucaiUuidNumber",shucaiUuidNumber);
                                }
                        }

                        //查询是否重复
                         //蔬菜编号
                        List<ShucaiEntity> shucaiEntities_shucaiUuidNumber = shucaiService.selectList(new EntityWrapper<ShucaiEntity>().in("shucai_uuid_number", seachFields.get("shucaiUuidNumber")).eq("shucai_delete", 1));
                        if(shucaiEntities_shucaiUuidNumber.size() >0 ){
                            ArrayList<String> repeatFields = new ArrayList<>();
                            for(ShucaiEntity s:shucaiEntities_shucaiUuidNumber){
                                repeatFields.add(s.getShucaiUuidNumber());
                            }
                            return R.error(511,"数据库的该表中的 [蔬菜编号] 字段已经存在 存在数据为:"+repeatFields.toString());
                        }
                        shucaiService.insertBatch(shucaiList);
                        return R.ok();
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            return R.error(511,"批量插入数据异常，请联系管理员");
        }
    }





    /**
    * 前端列表
    */
    @IgnoreAuth
    @RequestMapping("/list")
    public R list(@RequestParam Map<String, Object> params, HttpServletRequest request){
        logger.debug("list方法:,,Controller:{},,params:{}",this.getClass().getName(),JSONObject.toJSONString(params));

        // 没有指定排序字段就默认id倒序
        if(StringUtil.isEmpty(String.valueOf(params.get("orderBy")))){
            params.put("orderBy","id");
        }
        PageUtils page = shucaiService.queryPage(params);

        //字典表数据转换
        List<ShucaiView> list =(List<ShucaiView>)page.getList();
        for(ShucaiView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段
        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        ShucaiEntity shucai = shucaiService.selectById(id);
            if(shucai !=null){

                //点击数量加1
                shucai.setShucaiClicknum(shucai.getShucaiClicknum()+1);
                shucaiService.updateById(shucai);

                //entity转view
                ShucaiView view = new ShucaiView();
                BeanUtils.copyProperties( shucai , view );//把实体数据重构到view中

                //修改对应字典表字段
                dictionaryService.dictionaryConvert(view, request);
                return R.ok().put("data", view);
            }else {
                return R.error(511,"查不到数据");
            }
    }


    /**
    * 前端保存
    */
    @RequestMapping("/add")
    public R add(@RequestBody ShucaiEntity shucai, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,shucai:{}",this.getClass().getName(),shucai.toString());
        Wrapper<ShucaiEntity> queryWrapper = new EntityWrapper<ShucaiEntity>()
            .eq("shucai_name", shucai.getShucaiName())
            .eq("shucai_uuid_number", shucai.getShucaiUuidNumber())
            .eq("shucai_types", shucai.getShucaiTypes())
            .eq("shucai_kucun_number", shucai.getShucaiKucunNumber())
            .eq("shucai_price", shucai.getShucaiPrice())
            .eq("shucai_clicknum", shucai.getShucaiClicknum())
            .eq("shangxia_types", shucai.getShangxiaTypes())
            .eq("shucai_delete", shucai.getShucaiDelete())
            ;
        logger.info("sql语句:"+queryWrapper.getSqlSegment());
        ShucaiEntity shucaiEntity = shucaiService.selectOne(queryWrapper);
        if(shucaiEntity==null){
            shucai.setShucaiDelete(1);
            shucai.setCreateTime(new Date());
        shucaiService.insert(shucai);
            return R.ok();
        }else {
            return R.error(511,"表中有相同数据");
        }
    }


}
