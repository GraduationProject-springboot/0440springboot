
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
 * 果园留言
 * 后端接口
 * @author
 * @email
*/
@RestController
@Controller
@RequestMapping("/guoyuanLiuyan")
public class GuoyuanLiuyanController {
    private static final Logger logger = LoggerFactory.getLogger(GuoyuanLiuyanController.class);

    private static final String TABLE_NAME = "guoyuanLiuyan";

    @Autowired
    private GuoyuanLiuyanService guoyuanLiuyanService;


    @Autowired
    private TokenService tokenService;

    @Autowired
    private AddressService addressService;//收货地址
    @Autowired
    private DictionaryService dictionaryService;//字典
    @Autowired
    private ForumService forumService;//论坛
    @Autowired
    private GonggaoService gonggaoService;//公告
    @Autowired
    private GuoshuService guoshuService;//果树
    @Autowired
    private GuoshuCollectionService guoshuCollectionService;//果树收藏
    @Autowired
    private GuoshuOrderService guoshuOrderService;//果树订单
    @Autowired
    private GuoyuanService guoyuanService;//果园
    @Autowired
    private GuoyuanYuyueService guoyuanYuyueService;//果园预约
    @Autowired
    private NewsService newsService;//水果资讯
    @Autowired
    private ShuiguoService shuiguoService;//水果预售
    @Autowired
    private ShuiguoCollectionService shuiguoCollectionService;//水果收藏
    @Autowired
    private ShuiguoOrderService shuiguoOrderService;//水果预售订单
    @Autowired
    private YonghuService yonghuService;//用户
    @Autowired
    private UsersService usersService;//管理员


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
        CommonUtil.checkMap(params);
        PageUtils page = guoyuanLiuyanService.queryPage(params);

        //字典表数据转换
        List<GuoyuanLiuyanView> list =(List<GuoyuanLiuyanView>)page.getList();
        for(GuoyuanLiuyanView c:list){
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
        GuoyuanLiuyanEntity guoyuanLiuyan = guoyuanLiuyanService.selectById(id);
        if(guoyuanLiuyan !=null){
            //entity转view
            GuoyuanLiuyanView view = new GuoyuanLiuyanView();
            BeanUtils.copyProperties( guoyuanLiuyan , view );//把实体数据重构到view中
            //级联表 果园
            //级联表
            GuoyuanEntity guoyuan = guoyuanService.selectById(guoyuanLiuyan.getGuoyuanId());
            if(guoyuan != null){
            BeanUtils.copyProperties( guoyuan , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setGuoyuanId(guoyuan.getId());
            }
            //级联表 用户
            //级联表
            YonghuEntity yonghu = yonghuService.selectById(guoyuanLiuyan.getYonghuId());
            if(yonghu != null){
            BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createTime", "insertTime", "updateTime", "yonghuId"});//把级联的数据添加到view中,并排除id和创建时间字段,当前表的级联注册表
            view.setYonghuId(yonghu.getId());
            }
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
    public R save(@RequestBody GuoyuanLiuyanEntity guoyuanLiuyan, HttpServletRequest request){
        logger.debug("save方法:,,Controller:{},,guoyuanLiuyan:{}",this.getClass().getName(),guoyuanLiuyan.toString());

        String role = String.valueOf(request.getSession().getAttribute("role"));
        if(false)
            return R.error(511,"永远不会进入");
        else if("用户".equals(role))
            guoyuanLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));

        guoyuanLiuyan.setCreateTime(new Date());
        guoyuanLiuyan.setInsertTime(new Date());
        guoyuanLiuyanService.insert(guoyuanLiuyan);

        return R.ok();
    }

    /**
    * 后端修改
    */
    @RequestMapping("/update")
    public R update(@RequestBody GuoyuanLiuyanEntity guoyuanLiuyan, HttpServletRequest request) throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        logger.debug("update方法:,,Controller:{},,guoyuanLiuyan:{}",this.getClass().getName(),guoyuanLiuyan.toString());
        GuoyuanLiuyanEntity oldGuoyuanLiuyanEntity = guoyuanLiuyanService.selectById(guoyuanLiuyan.getId());//查询原先数据

        String role = String.valueOf(request.getSession().getAttribute("role"));
//        if(false)
//            return R.error(511,"永远不会进入");
//        else if("用户".equals(role))
//            guoyuanLiuyan.setYonghuId(Integer.valueOf(String.valueOf(request.getSession().getAttribute("userId"))));
        guoyuanLiuyan.setUpdateTime(new Date());

            guoyuanLiuyanService.updateById(guoyuanLiuyan);//根据id更新
            return R.ok();
    }



    /**
    * 删除
    */
    @RequestMapping("/delete")
    public R delete(@RequestBody Integer[] ids, HttpServletRequest request){
        logger.debug("delete:,,Controller:{},,ids:{}",this.getClass().getName(),ids.toString());
        List<GuoyuanLiuyanEntity> oldGuoyuanLiuyanList =guoyuanLiuyanService.selectBatchIds(Arrays.asList(ids));//要删除的数据
        guoyuanLiuyanService.deleteBatchIds(Arrays.asList(ids));

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
            List<GuoyuanLiuyanEntity> guoyuanLiuyanList = new ArrayList<>();//上传的东西
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
                    URL resource = this.getClass().getClassLoader().getResource("static/upload/" + fileName);//获取文件路径
                    File file = new File(resource.getFile());
                    if(!file.exists()){
                        return R.error(511,"找不到上传文件，请联系管理员");
                    }else{
                        List<List<String>> dataList = PoiUtil.poiImport(file.getPath());//读取xls文件
                        dataList.remove(0);//删除第一行，因为第一行是提示
                        for(List<String> data:dataList){
                            //循环
                            GuoyuanLiuyanEntity guoyuanLiuyanEntity = new GuoyuanLiuyanEntity();
//                            guoyuanLiuyanEntity.setGuoyuanId(Integer.valueOf(data.get(0)));   //商品 要改的
//                            guoyuanLiuyanEntity.setYonghuId(Integer.valueOf(data.get(0)));   //用户 要改的
//                            guoyuanLiuyanEntity.setGuoyuanLiuyanText(data.get(0));                    //留言内容 要改的
//                            guoyuanLiuyanEntity.setInsertTime(date);//时间
//                            guoyuanLiuyanEntity.setReplyText(data.get(0));                    //回复内容 要改的
//                            guoyuanLiuyanEntity.setUpdateTime(sdf.parse(data.get(0)));          //回复时间 要改的
//                            guoyuanLiuyanEntity.setCreateTime(date);//时间
                            guoyuanLiuyanList.add(guoyuanLiuyanEntity);


                            //把要查询是否重复的字段放入map中
                        }

                        //查询是否重复
                        guoyuanLiuyanService.insertBatch(guoyuanLiuyanList);
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

        CommonUtil.checkMap(params);
        PageUtils page = guoyuanLiuyanService.queryPage(params);

        //字典表数据转换
        List<GuoyuanLiuyanView> list =(List<GuoyuanLiuyanView>)page.getList();
        for(GuoyuanLiuyanView c:list)
            dictionaryService.dictionaryConvert(c, request); //修改对应字典表字段

        return R.ok().put("data", page);
    }

    /**
    * 前端详情
    */
    @RequestMapping("/detail/{id}")
    public R detail(@PathVariable("id") Long id, HttpServletRequest request){
        logger.debug("detail方法:,,Controller:{},,id:{}",this.getClass().getName(),id);
        GuoyuanLiuyanEntity guoyuanLiuyan = guoyuanLiuyanService.selectById(id);
            if(guoyuanLiuyan !=null){


                //entity转view
                GuoyuanLiuyanView view = new GuoyuanLiuyanView();
                BeanUtils.copyProperties( guoyuanLiuyan , view );//把实体数据重构到view中

                //级联表
                    GuoyuanEntity guoyuan = guoyuanService.selectById(guoyuanLiuyan.getGuoyuanId());
                if(guoyuan != null){
                    BeanUtils.copyProperties( guoyuan , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setGuoyuanId(guoyuan.getId());
                }
                //级联表
                    YonghuEntity yonghu = yonghuService.selectById(guoyuanLiuyan.getYonghuId());
                if(yonghu != null){
                    BeanUtils.copyProperties( yonghu , view ,new String[]{ "id", "createDate"});//把级联的数据添加到view中,并排除id和创建时间字段
                    view.setYonghuId(yonghu.getId());
                }
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
    public R add(@RequestBody GuoyuanLiuyanEntity guoyuanLiuyan, HttpServletRequest request){
        logger.debug("add方法:,,Controller:{},,guoyuanLiuyan:{}",this.getClass().getName(),guoyuanLiuyan.toString());
        guoyuanLiuyan.setCreateTime(new Date());
        guoyuanLiuyan.setInsertTime(new Date());
        guoyuanLiuyanService.insert(guoyuanLiuyan);

            return R.ok();
        }

}

