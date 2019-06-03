package com.seven.accumulate.redishelper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @classDesc: 功能描述:
 * @Author: S_even_77
 * @createTime: Created in 10:08 2018/1/18
 * @version: v1.0
 * @copyright: 北京辰森
 * @email: wq@choicesoft.com.cn
 */
@Aspect
@Component
public class RedisCacheAspect {

    private static final Logger logger = LoggerFactory.getLogger(RedisCacheAspect.class);

    /**
     * spring-redishelper.xml配置连接池、连接工厂、Redis模板
     **/
    @Autowired
    @Qualifier("stringRedisTemplate")
    private StringRedisTemplate srt;

    /**
     * Service层切点 使用到了我们定义的 RedisCache 作为切点表达式。
     * 而且我们可以看出此表达式基于 annotation。
     * 并且用于内建属性为查询的方法之上
     */
    @Pointcut("@annotation(com.choice.cloud.basicinfo.common.redishelper.RedisCache)")
    public void redisCacheAspect() {
    }

    /**
     * Service层切点 使用到了我们定义的 RedisEvict 作为切点表达式。
     * 而且我们可以看出此表达式是基于 annotation 的。
     * 并且用于内建属性为非查询的方法之上，用于更新表
     */
    @Pointcut("@annotation(com.choice.cloud.basicinfo.common.redishelper.RedisEvict)")
    public void redisCacheEvict() {
    }


    /**
     * Service层切点 使用到了我们定义的 RedisMapCache 作为切点表达式。
     *
     */
    @Pointcut("@annotation(com.choice.cloud.basicinfo.common.redishelper.RedisMapCache)")
    public void redisMapCache() {

    }

    @Around("redisCacheAspect()")
    public Object cache(ProceedingJoinPoint joinPoint) {

        logger.info("==========查询缓存=======");
        // 得到参数
        Object[] args = joinPoint.getArgs();

        // 得到被代理的方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 得到被代理的方法上的注解
        RedisCache redisCacheAnnotation =  method.getAnnotation(RedisCache.class);
        Type returnType = method.getGenericReturnType();
        Class modelType = null;

        int index =  redisCacheAnnotation.getIndex();
        String values = method.toString();
        String keyName = redisCacheAnnotation.keyGetName();//id
        String tableName = redisCacheAnnotation.tableName();


        if(returnType instanceof ParameterizedType && args[index] instanceof List){
            Type[] types = ((ParameterizedType)returnType).getActualTypeArguments();
            if(types ==null||types.length==0){
                modelType = Object.class;
            }else{
                modelType = (Class)types[0];
            }
        }else{
            modelType =  method.getReturnType();
        }


        ObjectMapper objectMapper = new ObjectMapper();
        StringBuffer otherArgsStr = new StringBuffer();
        for(int i = 0;i<args.length;i++){
            if(i!=index) {
                if(args[i]!=null) {
                    otherArgsStr.append("_").append(args[i].toString());
                }
            }
        }
        //1.判断参数是否list
        if (args[index] instanceof List) {
            //2.如果是list，则去redis中判断哪些是没有的list，哪些是已经存在的，得到两组id集合；
            logger.info("key是列表");
            List<Object> resultList = new ArrayList<>();
            List<String> notRedis = new ArrayList<>();
            splitArgsFromCache(args[index], modelType, values, objectMapper, resultList, notRedis,otherArgsStr.toString());
            try {
                if (notRedis.size() <= 0) {
                    return resultList;
                }
                //3.执行方法体，查询出结果；
                List<Object> objectMySql = doQuery(joinPoint, args, index, notRedis);
                if(objectMySql != null && objectMySql.size() != 0) {
                    //4.整理方法结果，判断方法返回值类型，将返回值类型_id对应的映射中判断是否存在方法名+参数，没有添加；并且新增values-key值的映射；
                    addCollectionToRedis(modelType, values, keyName, objectMapper, objectMySql, tableName, otherArgsStr.toString());
                    //5.整合存在的list和3中结果的list，返回结果
                    mergeToResult(resultList, objectMySql);
                }
                return resultList;
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                return new ArrayList<>();
            }finally {
                logger.info("==========查询缓存结束=======");
            }

        } else {
            //6.如果不是list，是单独的Object值，则判断是否redis中存在此key（方法名+Object值）
            logger.info("key不是列表");
            String key = args[index].toString();
            // 检查Redis中是否有缓存

            String value = (String) queryFromRedis(values, key + otherArgsStr.toString());

            // result是方法的最终返回结果
            Object result = null;
            try {
                if (null == value) {
                    //7.如果不存在，执行方法体，
                    result = doQuerySingleResult(joinPoint, args);
                    if(result != null) {
                        //8.放入redis ，借鉴4步骤的修改
                        addToRedis(values, objectMapper, key + otherArgsStr.toString(), result);
                        //8_,将返回值类型_id值对应的映射中判断是否存在方法名+参数，没有添加
                        addToIdMap(modelType, values, tableName, key + otherArgsStr.toString(), result);
                    }
                } else {
                    //9.如果存在，返回结果
                    result = unSerializerObject(objectMapper, value, modelType);
                }
                return result;
            } catch (Throwable e) {
                logger.error("解析异常", e);
                return null;
            } finally {
                logger.info("==========查询缓存结束=======");
            }
        }
    }

    /**
     *      * 在方法调用前清除缓存，然后调用业务方法
     *      * @param joinPoint
     *      * @return
     *      * @throws Throwable
     *
     */
    @Around("redisCacheEvict()")
    public Object evictCache(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("==========开始清除缓存=============");
        // 得到被代理的方法
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        // 得到被代理的方法上的注解
        RedisEvict redisEvictAnnotation = method.getAnnotation(RedisEvict.class);
        String tableName = redisEvictAnnotation.tableName();
        int index = redisEvictAnnotation.getIndex();

        Object[] args = joinPoint.getArgs();
        //1.判断小key是不是列表
        if (args[index] instanceof List) {
            //2：循环，包含3、4、5、6步骤
            logger.info("key为列表");
            //3：获取下一个id
            //4：判断是否遍历结束
            for (Object object : (List) args[index]) {
               deleteRedis(tableName,(String)object);
            }
        }else {
            //7：判断是否对象
            String id = null;
            if (args[index] instanceof String) {
                id = (String) args[index];
            }else{
                //8：从对象获取id
                Object arg = args[index];
                Method getIdMethod = arg.getClass().getDeclaredMethod("getId", null);
                id = (String) getIdMethod.invoke(arg, null);
            }
            //5,6
            if (id != null) {
                deleteRedis(tableName, id);
            }

        }

        logger.info("==========清除缓存结束=============");
        return joinPoint.proceed(joinPoint.getArgs());
    }



    @Around("redisMapCache()")
    public Object mapCache(ProceedingJoinPoint joinPoint) {
        logger.info("====查询缓存(map)====");
        Object[] objectArgs = joinPoint.getArgs();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        ObjectMapper objectMapper = new ObjectMapper();
        RedisMapCache redisMapCacheAnnotation = method.getAnnotation(RedisMapCache.class);
        Class type = redisMapCacheAnnotation.type();
        int index = redisMapCacheAnnotation.getIndex();
        String values = method.toString();
        String tableName = redisMapCacheAnnotation.tableName();


        //不在缓存中的key
        List<String> notRedis = new ArrayList<>();
        Map returnMap = new HashMap();

        try {

            //1.判断list是否遍历完
            List keys = (List) objectArgs[index];
            StringBuffer sb = new StringBuffer();
            for(int i = 0;i<objectArgs.length;i++){
               if(i!=index) {
                   sb.append("_").append(objectArgs[i].toString());
               }
            }
            //从缓存中查询
            for (Object key : keys) {
                String keyString = key.toString()+sb.toString();
                //2.缓存中是否存在，key为(field) : list.get(i)
                String value = (String) srt.opsForHash().get(values, key);

                if (value == null) {
                    //4.list.get(i)加入notRedis中
                    notRedis.add(key.toString());
                }else {
                    //3.加入returnMap中
                    logger.info("缓存命中 value = {}", value);
                    returnMap.put(key.toString(), objectMapper.readValue(value, type));
                }
            }


            if (notRedis.size() == 0) {
                logger.info("======查询缓存结束======");
                return returnMap;
            }

            //6.notRedis执行原方法
            objectArgs[index] = notRedis;

            logger.info("=======调用原方法========");

            Object proceedReturn = joinPoint.proceed(objectArgs);
            Map<String, Object> proceedMap = (Map<String, Object>) proceedReturn;
            if (proceedMap == null || proceedMap.size() == 0) {
                return returnMap;
            }
            //7.判断methodMap是否遍历完
            for (Object key : proceedMap.keySet()) {
                Object object = proceedMap.get(key);
                //8.键值对加入returnMap
                returnMap.put(key, object);
                //9.加入缓存tableName_id: field = key_组合其他参数 (id为键值对中的key)
                logger.info("把{} :{} = {} 放入缓存", tableName+"_"+key, values,key+sb.toString());
                srt.opsForList().rightPush(tableName+"_"+key, values+"+"+key+sb.toString());
                //10.加入缓存 field : key_组合其他参数= value
                logger.info("把{} :{} = {} 放入缓存", values, key+sb.toString(), objectMapper.writeValueAsString(object));
                srt.opsForHash().put(values, key+sb.toString(), objectMapper.writeValueAsString(object));
            }
            logger.info("=======查询缓存结束=======");
            return returnMap;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return returnMap;
    }


    private void deleteRedis(String tableName, String id) {
        //5：查询出tablename_id->下的所指向的缓存（其中小key是所指向缓存的大key, value是所指向缓存的小key），进行删除，
        List<String> list = srt.opsForList().range(tableName + "_" + id,0,srt.opsForList().size(tableName + "_" + id));
        for(String key:list){
            String[] methodArgs = key.split("\\+");
            logger.info("根据id去清除{}: {}", (String) methodArgs[0], methodArgs[1]);
            srt.opsForHash().delete((String) methodArgs[0], methodArgs[1]);
        }
        //6：删除tablename_id对应的缓存
        srt.delete(tableName + "_" + id);
    }

    private void addToIdMap(Class modelType, String values, String tableName, String key, Object result) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        if (modelType.getName().equals("java.util.List")) {
            for(Object object : (List) result) {
                Method getIdMethod =  object.getClass().getMethod("getId",null);
                String id = (String) getIdMethod.invoke(object, null);
                srt.opsForList().rightPush(tableName + "_" + id,values+"+"+key);
            }
        }else {
            Method getIdMethod = modelType.getDeclaredMethod("getId", null);
            String id = (String) getIdMethod.invoke(result, null);
            srt.opsForList().rightPush(tableName + "_" + id,values+"+"+key);
        }



    }

    private Object unSerializerObject(ObjectMapper objectMapper, String value, Class returnType) throws IOException {
        Object result;// 缓存命中
        if (logger.isInfoEnabled()) {
            logger.info("缓存命中, value = " + value);
        }
        // 反序列化 从缓存中拿到的json字符串
        result = objectMapper.readValue(value, returnType);
        return result;
    }

    private void addToRedis(String values, ObjectMapper objectMapper, String key, Object result) throws JsonProcessingException {
        // 序列化查询结果
        String json = objectMapper.writeValueAsString(result);
        // 序列化结果放入缓存
        logger.info("把{}: {}, {} 放入缓存", values, key, json);
        srt.opsForHash().put(values, key, json);
    }

    private Object doQuerySingleResult(ProceedingJoinPoint joinPoint, Object[] args) throws Throwable {
        Object result;// 调用数据库查询方法
        result = joinPoint.proceed(args);
        return result;
    }

    private String queryFromRedis(String values, String key) {
        return (String) srt.opsForHash().get(values, key);
    }

    private void mergeToResult(List<Object> resultList, List<Object> objectMySql) {
        for (Object object : objectMySql) {
            resultList.add(object);
        }
    }


    private void addCollectionToRedis(Class modelType, String values, String keyName, ObjectMapper objectMapper,
                                      List<Object> objectMySql,String tableName,String otherArgs)
            throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, JsonProcessingException {
        for (int i = 0; i < objectMySql.size(); i++){

            Object post =  objectMySql.get(i);

            Method getKeyMethod = modelType.getDeclaredMethod(keyName, null);

            String key = (String) getKeyMethod.invoke(post, null);

            String json = objectMapper.writeValueAsString(objectMySql.get(i));
            // 序列化结果放入缓存
            String multiKey  = key+otherArgs;
            logger.info("把{}: {}, {} 放入缓存", values, multiKey, json);
            srt.opsForHash().put(values, multiKey, json);
            addToIdMap(modelType, values, tableName, multiKey, post);
        }
    }

    private List<Object> doQuery(ProceedingJoinPoint joinPoint, Object[] args, int index, List<String> notRedis) throws Throwable {
        args[index] = notRedis;

        logger.info("查询数据库");
        Object object = joinPoint.proceed(args);

        return (List<Object>) object;
    }

    private void splitArgsFromCache(Object arg, Class modelType, String values, ObjectMapper objectMapper, List<Object> resultList, List<String> notRedis,String otherArgs) {
        for (Object object : (List) arg) {
            String key = object.toString();
            // 检查Redis中是否有缓存
            String value = queryFromRedis(values,key+otherArgs);


            try {
                if (null == value) {

                    notRedis.add(key);

                } else {

                    // 缓存命中
                    if (logger.isInfoEnabled()) {
                        logger.info("缓存命中, value = " + value);
                    }

                    // 反序列化 从缓存中拿到的json字符串

                    resultList.add(objectMapper.readValue(value, modelType));

                }
            } catch (Throwable e) {
                logger.error("解析异常",e);
            }
        }
    }

}
